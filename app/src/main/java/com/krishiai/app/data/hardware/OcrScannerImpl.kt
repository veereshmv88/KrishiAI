package com.krishiai.app.data.hardware

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.krishiai.app.domain.hardware.OcrResult
import com.krishiai.app.domain.hardware.OcrScanner
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class OcrScannerImpl @Inject constructor() : OcrScanner {
    
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    override suspend fun scanTextFromImage(bitmap: Bitmap): OcrResult {
        return try {
            val image = InputImage.fromBitmap(bitmap, 0)
            val result = recognizer.process(image).await()
            if (result.text.isNotEmpty()) {
                OcrResult.Success(result.text)
            } else {
                OcrResult.Error("No text found in the image")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            OcrResult.Error(e.message ?: "Failed to perform OCR")
        }
    }
}
