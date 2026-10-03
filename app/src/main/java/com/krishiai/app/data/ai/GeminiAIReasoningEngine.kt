package com.krishiai.app.data.ai

import com.google.ai.client.generativeai.Chat
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.*
import com.krishiai.app.BuildConfig
import com.krishiai.app.domain.ai.AIContextEngine
import com.krishiai.app.domain.ai.AIIntentRouter
import com.krishiai.app.domain.ai.AIReasoningEngine
import com.krishiai.app.domain.ai.AIResponse
import com.google.firebase.auth.FirebaseAuth
import com.krishiai.app.data.local.dao.APMCPriceDao
import com.krishiai.app.domain.repository.WeatherRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.firstOrNull

@Singleton
class GeminiAIReasoningEngine @Inject constructor(
    private val contextEngine: AIContextEngine,
    private val intentRouter: AIIntentRouter,
    private val weatherRepository: WeatherRepository,
    private val apmcPriceDao: APMCPriceDao,
    private val cropListingDao: com.krishiai.app.data.local.dao.CropListingDao,
    private val auth: FirebaseAuth
) : AIReasoningEngine {

    private var currentChat: Chat? = null
    
    private val weatherTool = Tool(
        listOf(
            defineFunction(
                name = "get_weather_forecast",
                description = "Get the current weather forecast for the farmer's location",
                Schema.str("locationKey", "The district or taluk name of the farmer")
            ) { locationKey ->
                val weatherResult = kotlinx.coroutines.runBlocking { weatherRepository.getWeatherForLocation(locationKey) }
                if (weatherResult.isSuccess) {
                    val weather = weatherResult.getOrNull()!!
                    JSONObject().apply {
                        put("temperature", weather.currentTemperature)
                        put("condition", weather.condition)
                        put("humidity", weather.humidity)
                        put("rainChance", weather.rainChance)
                        put("windSpeed", weather.windSpeed)
                    }
                } else {
                    JSONObject().apply { put("error", "No weather data found for \$locationKey") }
                }
            }
        )
    )

    private val marketPriceTool = Tool(
        listOf(
            defineFunction(
                name = "get_market_price",
                description = "Get the latest APMC market price for a specific crop",
                Schema.str("cropName", "The name of the crop in English")
            ) { cropName ->
                val prices = apmcPriceDao.getPricesForCrop(cropName)
                if (prices.isNotEmpty()) {
                    val latest = prices.first()
                    JSONObject().apply {
                        put("market", latest.marketName)
                        put("minPrice", latest.minPrice)
                        put("maxPrice", latest.maxPrice)
                        put("modalPrice", latest.modalPrice)
                    }
                } else {
                    JSONObject().apply { put("error", "No market price data found for \$cropName") }
                }
            }
        )
    )

    private val farmerDataTool = Tool(
        listOf(
            defineFunction(
                name = "get_farmer_crops",
                description = "Get the list of crops the farmer has currently listed or added to their profile"
            ) {
                val uid = auth.currentUser?.uid
                if (uid == null) {
                    JSONObject().apply { put("error", "User not logged in") }
                } else {
                    val listings = kotlinx.coroutines.runBlocking { cropListingDao.getListingsByFarmer(uid).firstOrNull() } ?: emptyList()
                    val resultStr = if (listings.isEmpty()) {
                        "No crops found."
                    } else {
                        listings.joinToString(", ") { "\${it.cropName} (\${it.quantityKg} kg)" }
                    }
                    JSONObject().apply { put("crops", resultStr) }
                }
            }
        )
    )

    private suspend fun getOrCreateChat(language: String): Chat {
        if (currentChat != null) return currentChat!!

        val context = contextEngine.buildContext(language)
        val systemInstructionText = contextEngine.buildSystemInstruction(context)

        val generativeModel = GenerativeModel(
            modelName = "gemini-2.0-flash",
            apiKey = BuildConfig.GEMINI_API_KEY,
            systemInstruction = content { text(systemInstructionText) },
            tools = listOf(weatherTool, marketPriceTool, farmerDataTool),
            safetySettings = listOf(
                SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.ONLY_HIGH),
                SafetySetting(HarmCategory.HATE_SPEECH, BlockThreshold.ONLY_HIGH)
            )
        )

        val chat = generativeModel.startChat()
        currentChat = chat
        return chat
    }

    override suspend fun processQuery(query: String, language: String): AIResponse {
        return withContext(Dispatchers.IO) {
            try {
                if (BuildConfig.GEMINI_API_KEY.isBlank()) {
                    return@withContext AIResponse(text = fallbackResponse(language), language = language, success = false, error = "API_KEY_MISSING")
                }

                // 1. Context & Routing (for future explicit orchestration, right now we just rely on chat)
                val context = contextEngine.buildContext(language)
                val intent = intentRouter.routeIntent(query, language, context.role)
                
                val chat = getOrCreateChat(language)
                val languageTag = if (language == "kn") "KANNADA" else "ENGLISH"
                val taggedQuery = "[CURRENT_LANGUAGE=\$languageTag]\n\$query"

                var response = chat.sendMessage(content { text(taggedQuery) })
                
                // Extremely basic function call handling for now
                var attempts = 0
                while (response.functionCalls.isNotEmpty() && attempts < 3) {
                    val functionResponses = mutableListOf<FunctionResponsePart>()
                    for (functionCall in response.functionCalls) {
                        try {
                            val decl = (weatherTool.functionDeclarations + marketPriceTool.functionDeclarations + farmerDataTool.functionDeclarations).find { it.name == functionCall.name }
                            if (decl != null) {
                                val result = decl.execute(functionCall)
                                functionResponses.add(FunctionResponsePart(functionCall.name, result as JSONObject))
                            } else {
                                functionResponses.add(FunctionResponsePart(functionCall.name, JSONObject().apply { put("error", "Unknown function") }))
                            }
                        } catch (e: Exception) {
                            functionResponses.add(FunctionResponsePart(functionCall.name, JSONObject().apply { put("error", "Execution failed") }))
                        }
                    }
                    response = chat.sendMessage(content {
                        functionResponses.forEach { part -> part(part) }
                    })
                    attempts++
                }

                val finalResponseText = response.text ?: fallbackResponse(language)
                AIResponse(text = finalResponseText, language = language, success = true)
            } catch (e: Exception) {
                e.printStackTrace()
                AIResponse(text = fallbackResponse(language), language = language, success = false, error = e.message)
            }
        }
    }
    
    private fun fallbackResponse(language: String): String {
        return if (language == "kn") "ಕ್ಷಮಿಸಿ, AI ಪ್ರಸ್ತುತ ಲಭ್ಯವಿಲ್ಲ." else "Sorry, AI is currently unavailable."
    }
}
