package com.krishiai.app.core.ai.image

import android.graphics.Bitmap

object ImageQualityAnalyzer {

    enum class QualityIssue {
        TOO_DARK,
        TOO_BRIGHT,
        BLURRY,
        GOOD
    }

    fun analyzeQuality(bitmap: Bitmap): QualityIssue {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        var totalLuminance = 0L
        for (pixel in pixels) {
            val r = (pixel shr 16) and 0xFF
            val g = (pixel shr 8) and 0xFF
            val b = pixel and 0xFF
            val luminance = (0.299 * r + 0.587 * g + 0.114 * b).toInt()
            totalLuminance += luminance
        }

        val avgLuminance = totalLuminance / pixels.size
        
        return when {
            avgLuminance < 40 -> QualityIssue.TOO_DARK
            avgLuminance > 220 -> QualityIssue.TOO_BRIGHT
            // We'll skip complex blur detection (Laplacian variance) for now to keep live preview 60fps
            // but the architecture is ready.
            else -> QualityIssue.GOOD
        }
    }
}
