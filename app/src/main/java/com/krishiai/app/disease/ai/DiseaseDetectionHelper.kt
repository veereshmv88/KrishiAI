package com.krishiai.app.disease.ai

import android.content.Context
import android.graphics.Bitmap
import com.krishiai.app.core.ai.AIEngine
import com.krishiai.app.core.ai.AIResult
import com.krishiai.app.core.ai.image.ImagePreprocessor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

data class Prediction(val label: String, val confidence: Float)

class DiseaseDetectionHelper(
    private val context: Context,
    private val modelPath: String,
    private val classIndices: Map<Int, String>,
    private val threshold: Float = 0.60f
) : AIEngine<Bitmap, List<Prediction>> {

    private var interpreter: Interpreter? = null

    override fun initialize() {
        if (interpreter == null) {
            val modelBuffer = loadModelFile(context, modelPath)
            val options = Interpreter.Options()
            options.setNumThreads(4)
            interpreter = Interpreter(modelBuffer, options)
        }
    }

    private fun loadModelFile(context: Context, modelPath: String): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(modelPath)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    override suspend fun analyze(input: Bitmap): AIResult<List<Prediction>> = withContext(Dispatchers.Default) {
        try {
            if (interpreter == null) {
                return@withContext AIResult.Error(IllegalStateException("Interpreter not initialized"))
            }

            // Preprocess Image (384x384 float32)
            val byteBuffer = ImagePreprocessor.processBitmapToBuffer(input, targetWidth = 384, targetHeight = 384)
            
            // Output array for 38 classes
            val output = Array(1) { FloatArray(38) }

            // Run inference
            interpreter?.run(byteBuffer, output)

            // Map and sort results
            val predictions = output[0]
                .mapIndexed { index, confidence -> 
                    Prediction(
                        label = classIndices[index] ?: "Unknown",
                        confidence = confidence
                    ) 
                }
                .sortedByDescending { it.confidence }
                .take(3)

            val topPrediction = predictions.firstOrNull()
            
            if (topPrediction == null || topPrediction.confidence < threshold) {
                return@withContext AIResult.LowConfidence()
            }

            AIResult.Success(predictions)
        } catch (e: Exception) {
            AIResult.Error(e, "Inference failed")
        }
    }

    override fun close() {
        interpreter?.close()
        interpreter = null
    }
}
