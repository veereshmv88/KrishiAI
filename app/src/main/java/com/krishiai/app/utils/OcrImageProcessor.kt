package com.krishiai.app.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint

object OcrImageProcessor {
    fun preprocessForOcr(bitmap: Bitmap, rotationDegrees: Float = 0f): Bitmap {
        // 1. Apply rotation if needed
        var processed = if (rotationDegrees != 0f) {
            rotateBitmap(bitmap, rotationDegrees)
        } else {
            bitmap
        }

        // 2. Enhance contrast and convert to grayscale
        processed = enhanceContrastAndGrayscale(processed)

        // 3. Adaptive thresholding (simulated with high contrast)
        // Since we want to avoid OpenCV, we use ColorMatrix for a strong binarization effect
        processed = applyBinarization(processed)

        return processed
    }

    private fun rotateBitmap(source: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degrees)
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    private fun enhanceContrastAndGrayscale(bitmap: Bitmap): Bitmap {
        val bmpGrayscale = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmpGrayscale)
        val paint = Paint()

        val colorMatrix = ColorMatrix()
        colorMatrix.setSaturation(0f) // Grayscale
        
        // Increase Contrast (scale > 1)
        val scale = 1.5f
        val translate = (-.5f * scale + .5f) * 255f
        val contrastMatrix = ColorMatrix(floatArrayOf(
            scale, 0f, 0f, 0f, translate,
            0f, scale, 0f, 0f, translate,
            0f, 0f, scale, 0f, translate,
            0f, 0f, 0f, 1f, 0f
        ))
        
        colorMatrix.postConcat(contrastMatrix)
        
        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        return bmpGrayscale
    }

    private fun applyBinarization(bitmap: Bitmap): Bitmap {
        // A simple extreme contrast to simulate thresholding without OpenCV
        val binarized = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(binarized)
        val paint = Paint()

        val scale = 5f // Extreme contrast
        val translate = (-.5f * scale + .5f) * 255f
        val contrastMatrix = ColorMatrix(floatArrayOf(
            scale, 0f, 0f, 0f, translate,
            0f, scale, 0f, 0f, translate,
            0f, 0f, scale, 0f, translate,
            0f, 0f, 0f, 1f, 0f
        ))
        
        paint.colorFilter = ColorMatrixColorFilter(contrastMatrix)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        return binarized
    }
}
