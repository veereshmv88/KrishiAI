package com.krishiai.app.data.ai

import com.google.firebase.auth.FirebaseAuth
import com.krishiai.app.data.local.dao.UserDao
import com.krishiai.app.domain.ai.AIContext
import com.krishiai.app.domain.ai.AIContextEngine
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AIContextEngineImpl @Inject constructor(
    private val userDao: UserDao,
    private val auth: FirebaseAuth
) : AIContextEngine {

    override suspend fun buildContext(language: String): AIContext {
        val uid = auth.currentUser?.uid
        val user = uid?.let { userDao.getUser(it) }

        return AIContext(
            userId = uid,
            role = user?.role ?: "Farmer",
            language = language,
            farmerName = user?.name,
            district = user?.district,
            taluk = user?.taluk,
            farmSizeAcres = user?.farmSizeAcres,
            businessName = user?.businessName
        )
    }

    override fun buildSystemInstruction(context: AIContext): String {
        return buildString {
            append("You are Krishi AI Assistant, an expert agricultural AI assistant.\n")
            append("CRITICAL INSTRUCTION: You are a bilingual assistant. You will be provided with a [CURRENT_LANGUAGE=...] tag in each user message.\n")
            append("If CURRENT_LANGUAGE is ENGLISH, respond in clear farmer-friendly English.\n")
            append("If CURRENT_LANGUAGE is KANNADA, respond in natural, clear Kannada.\n")
            append("Do not switch languages unless explicitly requested by the user.\n")
            append("Keep crop names, scientific names, fertilizer names, API terminology, and market names understandable.\n")
            append("Do NOT translate technical terms incorrectly. Examples: Tomato -> ಟೊಮೇಟೊ, Urea -> ಯೂರಿಯಾ, DAP -> DAP, NPK -> NPK, APMC -> APMC, AGMARKNET -> AGMARKNET, Pesticide -> ಕೀಟನಾಶಕ, Fertilizer -> ರಸಗೊಬ್ಬರ, Disease -> ರೋಗ, Market Price -> ಮಾರುಕಟ್ಟೆ ಬೆಲೆ, District -> ಜಿಲ್ಲೆ, Taluk -> ತಾಲೂಕು.\n")
            append("Use natural Kannada rather than machine-translated awkward sentences.\n")
            append("When using tools (e.g., get_market_price, get_weather_forecast, get_farmer_crops), you MUST internally translate the user's intent (e.g., 'ಅಕ್ಕಿ' -> 'Rice') into English parameters for the API. The final response to the user MUST be in the CURRENT_LANGUAGE.\n")
            append("If an API returns Rice, talk about Rice. NEVER substitute with Tomato.\n")
            append("Always answer clearly and concisely (max 2-3 sentences unless asked for details).\n")
            
            append("\nHere is the context about the user you are talking to:\n")
            append("- Role: ${context.role}\n")
            if (context.role == "Farmer") {
                context.farmerName?.let { append("- Name: $it\n") }
                if (context.district != null && context.taluk != null) {
                    append("- Location: ${context.taluk}, ${context.district}\n")
                }
                context.farmSizeAcres?.let { append("- Farm Size: $it acres\n") }
            } else if (context.role == "Buyer") {
                context.farmerName?.let { append("- Name: $it\n") }
                context.businessName?.let { append("- Business Name: $it\n") }
                context.district?.let { append("- City/District: $it\n") }
            }
            
            append("\nYou have tools to look up real-time weather, market prices, user crops, and disease information. If you don't know the answer, use a tool or say you don't know.\n")
            append("Be extremely polite and helpful.\n")
        }
    }
}
