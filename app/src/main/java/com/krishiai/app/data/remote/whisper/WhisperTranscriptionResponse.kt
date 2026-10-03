package com.krishiai.app.data.remote.whisper

import com.google.gson.annotations.SerializedName

data class WhisperTranscriptionResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("text")
    val text: String?,
    
    @SerializedName("language")
    val language: String?,
    
    @SerializedName("error")
    val error: String?
)
