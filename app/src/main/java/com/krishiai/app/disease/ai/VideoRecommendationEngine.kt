package com.krishiai.app.disease.ai

import com.krishiai.app.disease.data.remote.YouTubeApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class RecommendedVideo(
    val title: String,
    val description: String,
    val channelName: String,
    val thumbnailUrl: String,
    val videoId: String,
    val language: String
)

class VideoRecommendationEngine @Inject constructor(
    private val youtubeApiService: YouTubeApiService
) {
    // Curated local fallback database for major diseases
    private val curatedDatabase = mapOf(
        "tomato_early_blight" to listOf(
            RecommendedVideo(
                title = "Tomato Early Blight Control - ICAR KVK",
                description = "Complete guide to identifying and treating early blight in tomatoes organically and chemically.",
                channelName = "ICAR - Indian Institute of Horticultural Research",
                thumbnailUrl = "https://img.youtube.com/vi/DUMMY_ID_1/hqdefault.jpg",
                videoId = "DUMMY_ID_1",
                language = "en"
            ),
            RecommendedVideo(
                title = "ಟೊಮೆಟೊ ಬೆಳೆಯ ರೋಗಗಳ ನಿರ್ವಹಣೆ",
                description = "ಟೊಮೆಟೊ ಬೆಳೆಯಲ್ಲಿ ಬರುವ ರೋಗಗಳ ನಿಯಂತ್ರಣ ಕ್ರಮಗಳು",
                channelName = "UAS Bangalore",
                thumbnailUrl = "https://img.youtube.com/vi/DUMMY_ID_2/hqdefault.jpg",
                videoId = "DUMMY_ID_2",
                language = "kn"
            ),
            RecommendedVideo(
                title = "Organic Fungicides for Tomato Blight",
                description = "How to prepare and spray organic fungicides for early blight.",
                channelName = "National Centre for Organic Farming",
                thumbnailUrl = "https://img.youtube.com/vi/DUMMY_ID_3/hqdefault.jpg",
                videoId = "DUMMY_ID_3",
                language = "en"
            )
        )
    )

    suspend fun getRecommendations(
        cropName: String,
        diseaseName: String,
        language: String
    ): List<RecommendedVideo> = withContext(Dispatchers.IO) {
        val key = "${cropName.lowercase()}_${diseaseName.lowercase()}".replace(" ", "_")
        
        // 1. Try local curated database first
        val curated = curatedDatabase[key]
        if (curated != null && curated.isNotEmpty()) {
            return@withContext curated.filter { it.language == language || it.language == "en" }.take(3)
        }
        
        // 2. Fallback to YouTube API
        try {
            // Note: Replace "API_KEY" with BuildConfig.YOUTUBE_API_KEY in production
            val apiKey = "" // We'll implement fetching this from BuildConfig later
            if (apiKey.isEmpty()) return@withContext emptyList()
            
            val query = "$cropName $diseaseName treatment ${if (language == "kn") "Kannada" else "English"} agriculture"
            val response = youtubeApiService.searchVideos(
                query = query,
                apiKey = apiKey
            )
            
            return@withContext response.items.mapNotNull { item ->
                val videoId = item.id.videoId ?: return@mapNotNull null
                RecommendedVideo(
                    title = item.snippet.title,
                    description = item.snippet.description,
                    channelName = item.snippet.channelTitle,
                    thumbnailUrl = item.snippet.thumbnails.high?.url ?: "",
                    videoId = videoId,
                    language = language
                )
            }.take(3)
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext emptyList()
        }
    }
}
