package com.krishiai.app.domain.hardware

import android.graphics.Bitmap

interface OcrScanner {
    suspend fun scanTextFromImage(bitmap: Bitmap): OcrResult
}

sealed class OcrResult {
    data class Success(val extractedText: String) : OcrResult()
    data class Error(val message: String) : OcrResult()
}
