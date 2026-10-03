package com.krishiai.app.data.remote.dhvaani

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

data class TtsRequest(
    val text: String,
    val language: String
)

interface DhVaaniApiService {
    @POST("tts")
    suspend fun synthesize(
        @Body request: TtsRequest
    ): Response<ResponseBody>
}
