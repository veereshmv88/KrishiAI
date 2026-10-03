package com.krishiai.app.data.remote.whisper

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface WhisperApiService {
    
    @Multipart
    @POST("transcribe")
    suspend fun transcribeAudio(
        @Part audio: MultipartBody.Part,
        @Part("language") language: RequestBody
    ): Response<WhisperTranscriptionResponse>
}
