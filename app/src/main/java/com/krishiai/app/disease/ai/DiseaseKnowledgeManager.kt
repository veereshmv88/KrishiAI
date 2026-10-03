package com.krishiai.app.disease.ai

import android.content.Context
import org.json.JSONObject
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

data class DiseaseKnowledge(
    val cropName: String,
    val diseaseName: String,
    val scientificName: String,
    val description: String,
    val symptoms: String,
    val causes: String,
    val spreadMethod: String,
    val weatherConditions: String,
    val organicTreatment: String,
    val chemicalTreatment: String,
    val recommendedFertilizer: String,
    val recommendedPesticide: String,
    val preventionTips: String,
    val irrigationAdvice: String,
    val estimatedRecoveryTime: String,
    val harvestWarning: String,
    val whenToConsultOfficer: String,
    val severityLevel: String,
    val referenceImagePath: String
)

@Singleton
class DiseaseKnowledgeManager @Inject constructor(
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: Context
) {
    
    private val knowledgeCache = mutableMapOf<String, DiseaseKnowledge>()

    fun getKnowledge(className: String): DiseaseKnowledge? {
        if (knowledgeCache.isEmpty()) {
            loadKnowledge()
        }
        return knowledgeCache[className]
    }

    fun getAllKeys(): List<String> {
        if (knowledgeCache.isEmpty()) {
            loadKnowledge()
        }
        return knowledgeCache.keys.toList()
    }

    fun findKnowledgeFuzzy(diseaseName: String): DiseaseKnowledge? {
        if (knowledgeCache.isEmpty()) {
            loadKnowledge()
        }
        // Match ignoring case and replacing spaces with underscores
        val normalizedQuery = diseaseName.lowercase().replace(" ", "_")
        return knowledgeCache.values.firstOrNull { 
            it.diseaseName.lowercase().replace(" ", "_") == normalizedQuery ||
            it.diseaseName.lowercase().contains(normalizedQuery) ||
            normalizedQuery.contains(it.diseaseName.lowercase().replace(" ", "_"))
        } ?: knowledgeCache.values.firstOrNull {
            it.cropName.lowercase() + "___" + it.diseaseName.lowercase() == normalizedQuery
        } ?: knowledgeCache.values.firstOrNull {
            it.diseaseName.lowercase() == "healthy"
        }
    }

    private fun loadKnowledge() {
        try {
            val jsonString = context.assets.open("models/disease/disease_knowledge.json")
                .bufferedReader().use { it.readText() }
            val jsonObject = JSONObject(jsonString)

            jsonObject.keys().forEach { key ->
                val entry = jsonObject.getJSONObject(key)
                knowledgeCache[key] = DiseaseKnowledge(
                    cropName = entry.optString("cropName"),
                    diseaseName = entry.optString("diseaseName"),
                    scientificName = entry.optString("scientificName"),
                    description = entry.optString("description"),
                    symptoms = entry.optString("symptoms"),
                    causes = entry.optString("causes"),
                    spreadMethod = entry.optString("spreadMethod"),
                    weatherConditions = entry.optString("weatherConditions"),
                    organicTreatment = entry.optString("organicTreatment"),
                    chemicalTreatment = entry.optString("chemicalTreatment"),
                    recommendedFertilizer = entry.optString("recommendedFertilizer"),
                    recommendedPesticide = entry.optString("recommendedPesticide"),
                    preventionTips = entry.optString("preventionTips"),
                    irrigationAdvice = entry.optString("irrigationAdvice"),
                    estimatedRecoveryTime = entry.optString("estimatedRecoveryTime"),
                    harvestWarning = entry.optString("harvestWarning"),
                    whenToConsultOfficer = entry.optString("whenToConsultOfficer"),
                    severityLevel = entry.optString("severityLevel"),
                    referenceImagePath = entry.optString("referenceImagePath")
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
