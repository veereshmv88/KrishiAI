package com.krishiai.app.core.ai.image

import android.graphics.Bitmap
import android.graphics.Color
import java.nio.ByteBuffer
import java.nio.ByteOrder

object ImagePreprocessor {

    fun processBitmapToBuffer(
        bitmap: Bitmap,
        targetWidth: Int = 384,
        targetHeight: Int = 384,
        buffer: ByteBuffer? = null
    ): ByteBuffer {
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
        
        // Ensure buffer is allocated, 4 bytes per float * 3 channels
        val byteBuffer = buffer ?: ByteBuffer.allocateDirect(4 * targetWidth * targetHeight * 3)
        byteBuffer.order(ByteOrder.nativeOrder())
        byteBuffer.rewind()

        val intValues = IntArray(targetWidth * targetHeight)
        scaledBitmap.getPixels(intValues, 0, scaledBitmap.width, 0, 0, scaledBitmap.width, scaledBitmap.height)

        var pixel = 0
        for (i in 0 until targetWidth) {
            for (j in 0 until targetHeight) {
                val value = intValues[pixel++]
                // EfficientNetV2 expects values in [0, 1] or [-1, 1], but commonly standard models use standard float.
                // We'll normalize to [0, 1]
                byteBuffer.putFloat(Color.red(value) / 255.0f)
                byteBuffer.putFloat(Color.green(value) / 255.0f)
                byteBuffer.putFloat(Color.blue(value) / 255.0f)
            }
        }
        
        if (scaledBitmap != bitmap) {
            scaledBitmap.recycle()
        }
        
        return byteBuffer
    }
}
