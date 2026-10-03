package com.krishiai.app.disease.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

data class YouTubeSearchResponse(
    val items: List<YouTubeVideoItem>
)

data class YouTubeVideoItem(
    val id: VideoId,
    val snippet: VideoSnippet
)

data class VideoId(
    val videoId: String?
)

data class VideoSnippet(
    val title: String,
    val description: String,
    val channelTitle: String,
    val thumbnails: Thumbnails
)

data class Thumbnails(
    val high: ThumbnailDetail?
)

data class ThumbnailDetail(
    val url: String
)

interface YouTubeApiService {
    @GET("search")
    suspend fun searchVideos(
        @Query("part") part: String = "snippet",
        @Query("q") query: String,
        @Query("type") type: String = "video",
        @Query("maxResults") maxResults: Int = 3,
        @Query("key") apiKey: String
    ): YouTubeSearchResponse
}
