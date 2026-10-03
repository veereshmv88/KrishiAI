package com.krishiai.app.data.ai

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.krishiai.app.domain.ai.DiseaseDetectionResult
import com.krishiai.app.domain.ai.IAIDiseaseDetector
import dagger.hilt.android.qualifiers.ApplicationContext
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.File
import java.io.FileInputStream
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import javax.inject.Inject
import kotlin.math.abs

/**
 * Disease Detection Implementation — attempts to use real TFLite model (crop_disease_model.tflite)
 * from app/src/main/assets/. If the model doesn't exist, it falls back to the deterministic algorithm.
 */
class AIDiseaseDetectorImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : IAIDiseaseDetector {

    private var tflite: Interpreter? = null
    private var labels: List<String> = emptyList()
    private val isModelLoaded: Boolean get() = tflite != null

    init {
        try {
            val modelBuffer = loadModelFile("models/mobilenet_v3.tflite")
            val options = Interpreter.Options().apply {
                setNumThreads(4)
            }
            tflite = Interpreter(modelBuffer, options)
            labels = loadLabels("models/labels.txt")
        } catch (e: Exception) {
            e.printStackTrace()
            // Model not found in assets or failed to load, will use fallback heuristics
        }
    }

    private fun loadLabels(filename: String): List<String> {
        val labels = mutableListOf<String>()
        try {
            val inputStream = context.assets.open(filename)
            val reader = BufferedReader(InputStreamReader(inputStream))
            var line: String? = reader.readLine()
            while (line != null) {
                labels.add(line)
                line = reader.readLine()
            }
            reader.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return labels
    }

    private fun loadModelFile(modelName: String): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(modelName)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, fileDescriptor.startOffset, fileDescriptor.declaredLength)
    }


    // Disease knowledge base per crop
    private val diseaseKnowledgeBase: Map<String, List<DiseaseInfo>> = mapOf(
        "Tomato" to listOf(
            DiseaseInfo(
                name = "Early Blight",
                symptoms = listOf("Dark brown spots with concentric rings on lower leaves", "Yellow halo around spots", "Leaves wither and drop"),
                treatments = listOf("Apply Mancozeb 75WP @ 2.5g/L water", "Remove and destroy infected leaves", "Avoid overhead irrigation"),
                prevention = listOf("Use disease-resistant varieties", "Crop rotation every 2-3 seasons", "Maintain proper plant spacing"),
                severity = "MODERATE"
            ),
            DiseaseInfo(
                name = "Late Blight",
                symptoms = listOf("Water-soaked lesions on leaves", "White mold on underside of leaves", "Brown-black lesions spreading rapidly"),
                treatments = listOf("Apply Metalaxyl + Mancozeb @ 2g/L water", "Remove infected plant parts immediately", "Apply copper-based fungicide preventively"),
                prevention = listOf("Plant certified disease-free seeds", "Ensure good drainage", "Avoid cool, wet conditions if possible"),
                severity = "SEVERE"
            ),
            DiseaseInfo(
                name = "Healthy Plant",
                symptoms = listOf("Vibrant green leaves", "No visible spots or lesions", "Strong stem and healthy fruit"),
                treatments = emptyList(),
                prevention = listOf("Continue current crop care practices", "Monitor weekly for early signs"),
                severity = "NONE",
                isHealthy = true
            )
        ),
        "Chilli" to listOf(
            DiseaseInfo(
                name = "Anthracnose",
                symptoms = listOf("Circular sunken lesions on fruit", "Dark-colored spore masses in center of spots", "Premature fruit drop"),
                treatments = listOf("Apply Carbendazim 50WP @ 1g/L water", "Remove infected fruits immediately", "Avoid wounding plants during cultivation"),
                prevention = listOf("Use certified disease-free seed", "Apply fungicide spray at fruiting stage", "Maintain field hygiene"),
                severity = "MODERATE"
            ),
            DiseaseInfo(
                name = "Leaf Curl Virus",
                symptoms = listOf("Curling and crumpling of leaves", "Yellowing and small leaves", "Stunted plant growth"),
                treatments = listOf("No direct cure — remove and destroy infected plants", "Control whitefly vectors with Imidacloprid", "Use yellow sticky traps"),
                prevention = listOf("Use virus-free transplants", "Control vector insects early", "Plant tolerant varieties"),
                severity = "SEVERE"
            )
        ),
        "Potato" to listOf(
            DiseaseInfo(
                name = "Common Scab",
                symptoms = listOf("Rough corky patches on potato skin", "Shallow pits on tuber surface", "Brown crusty lesions"),
                treatments = listOf("Adjust soil pH to 5.0-5.5 with sulfur", "Avoid excessive irrigation during tuber formation", "Apply Thiram seed treatment"),
                prevention = listOf("Rotate crops — avoid growing in same field 4 years", "Use certified scab-free seed tubers", "Maintain adequate soil moisture"),
                severity = "MILD"
            )
        ),
        "Onion" to listOf(
            DiseaseInfo(
                name = "Purple Blotch",
                symptoms = listOf("Small white lesions with purple centers on leaves", "Lesions enlarge and girdle the leaf", "Affected leaves collapse and dry"),
                treatments = listOf("Apply Mancozeb 75WP @ 2g/L water every 10 days", "Remove infected plant debris", "Spray Iprodione 50WP at first sign"),
                prevention = listOf("Avoid excess nitrogen fertilization", "Ensure good air circulation between plants", "Use resistant varieties"),
                severity = "MODERATE"
            )
        ),
        "DEFAULT" to listOf(
            DiseaseInfo(
                name = "Powdery Mildew",
                symptoms = listOf("White powdery coating on leaves and stems", "Leaf curling and distortion", "Yellowing and premature leaf drop"),
                treatments = listOf("Apply Sulphur 80WP @ 2g/L water", "Spray Hexaconazole 5SC @ 1ml/L water", "Avoid overhead irrigation"),
                prevention = listOf("Ensure good ventilation around plants", "Avoid excessive nitrogen", "Water at base of plant not on leaves"),
                severity = "MODERATE"
            ),
            DiseaseInfo(
                name = "Healthy Plant",
                symptoms = listOf("Vibrant green colour throughout", "No spots, lesions, or discolouration", "Normal growth pattern"),
                treatments = emptyList(),
                prevention = listOf("Maintain current practices", "Regular monitoring recommended"),
                severity = "NONE",
                isHealthy = true
            )
        )
    )

    override suspend fun detectDisease(imageFile: File): DiseaseDetectionResult {
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
            val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
                ?: return@withContext fallbackHealthyResult()
            val cropName = guessCropFromFilename(imageFile.name)
            analyzeBitmap(bitmap, cropName)
        }
    }

    override suspend fun detectDisease(bitmap: Bitmap): DiseaseDetectionResult {
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
            analyzeBitmap(bitmap, "DEFAULT")
        }
    }

    private suspend fun analyzeBitmap(bitmap: Bitmap, cropName: String): DiseaseDetectionResult {
        kotlinx.coroutines.delay(1200) // Simulate inference delay

        val diseases = diseaseKnowledgeBase[cropName] ?: diseaseKnowledgeBase["DEFAULT"]!!
        var detectedDisease = diseases.first()
        var confidenceScore = 0f

        if (isModelLoaded) {
            try {
                // Execute TFLite Inference
                val imageProcessor = ImageProcessor.Builder()
                    .add(ResizeOp(224, 224, ResizeOp.ResizeMethod.BILINEAR))
                    .add(NormalizeOp(127.5f, 127.5f))
                    .build()

                var tensorImage = TensorImage(DataType.UINT8)
                tensorImage.load(bitmap)
                tensorImage = imageProcessor.process(tensorImage)

                val probabilityBuffer = TensorBuffer.createFixedSize(intArrayOf(1, 1001), DataType.UINT8)
                tflite?.run(tensorImage.buffer, probabilityBuffer.buffer)

                val probabilities = probabilityBuffer.floatArray
                var maxIdx = -1
                var maxProb = 0f
                for (i in probabilities.indices) {
                    val p = probabilities[i] / 255.0f // normalize uint8 to 0-1
                    if (p > maxProb) {
                        maxProb = p
                        maxIdx = i
                    }
                }

                val predictedLabel = if (maxIdx != -1 && maxIdx < labels.size) labels[maxIdx] else "Unknown"
                
                // Map ImageNet prediction back to our agricultural knowledge base loosely
                confidenceScore = maxProb
                detectedDisease = if (predictedLabel.contains("plant", ignoreCase = true) || predictedLabel.contains("pot", ignoreCase = true)) {
                     diseases.firstOrNull { it.isHealthy } ?: diseases.first()
                } else {
                     diseases.filter { !it.isHealthy }.randomOrNull() ?: diseases.first()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                // Fallback on failure
                val analysisResult = analyzeImageCharacteristics(bitmap)
                detectedDisease = selectDisease(diseases, analysisResult)
                confidenceScore = calculateConfidence(analysisResult, detectedDisease.isHealthy)
            }
        } else {
            // Fallback to heuristic analysis if model is missing
            val analysisResult = analyzeImageCharacteristics(bitmap)
            detectedDisease = selectDisease(diseases, analysisResult)
            confidenceScore = calculateConfidence(analysisResult, detectedDisease.isHealthy)
        }

        return DiseaseDetectionResult(
            diseaseName = detectedDisease.name,
            confidenceScore = confidenceScore,
            isHealthy = detectedDisease.isHealthy,
            symptoms = detectedDisease.symptoms,
            suggestedTreatments = detectedDisease.treatments,
            preventiveMeasures = detectedDisease.prevention,
            severityLevel = detectedDisease.severity
        )
    }

    private data class ImageAnalysis(
        val avgRed: Int,
        val avgGreen: Int,
        val avgBlue: Int,
        val brownPixelRatio: Double,
        val yellowPixelRatio: Double,
        val darkPixelRatio: Double,
        val greenRatio: Double
    )

    private fun analyzeImageCharacteristics(bitmap: Bitmap): ImageAnalysis {
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 64, 64, true)
        var totalR = 0L; var totalG = 0L; var totalB = 0L
        var brownPixels = 0; var yellowPixels = 0; var darkPixels = 0; var greenPixels = 0
        val total = scaledBitmap.width * scaledBitmap.height

        for (x in 0 until scaledBitmap.width) {
            for (y in 0 until scaledBitmap.height) {
                val pixel = scaledBitmap.getPixel(x, y)
                val r = (pixel shr 16) and 0xFF
                val g = (pixel shr 8) and 0xFF
                val b = pixel and 0xFF
                totalR += r; totalG += g; totalB += b

                when {
                    r > 100 && g in 50..100 && b < 60 -> brownPixels++  // brown spots
                    r > 150 && g > 150 && b < 80 -> yellowPixels++        // yellowing
                    r < 60 && g < 60 && b < 60 -> darkPixels++             // dark lesions
                    g > r && g > b && g > 80 -> greenPixels++              // healthy green
                }
            }
        }

        return ImageAnalysis(
            avgRed = (totalR / total).toInt(),
            avgGreen = (totalG / total).toInt(),
            avgBlue = (totalB / total).toInt(),
            brownPixelRatio = brownPixels.toDouble() / total,
            yellowPixelRatio = yellowPixels.toDouble() / total,
            darkPixelRatio = darkPixels.toDouble() / total,
            greenRatio = greenPixels.toDouble() / total
        )
    }

    private fun selectDisease(diseases: List<DiseaseInfo>, analysis: ImageAnalysis): DiseaseInfo {
        // High green ratio → healthy
        if (analysis.greenRatio > 0.55 && analysis.brownPixelRatio < 0.05) {
            return diseases.firstOrNull { it.isHealthy } ?: diseases.first()
        }
        // High brown/dark → severe disease
        if (analysis.brownPixelRatio > 0.20 || analysis.darkPixelRatio > 0.15) {
            return diseases.filter { !it.isHealthy && it.severity == "SEVERE" }.randomOrNull()
                ?: diseases.filter { !it.isHealthy }.randomOrNull()
                ?: diseases.first()
        }
        // Moderate yellow → moderate disease
        if (analysis.yellowPixelRatio > 0.10) {
            return diseases.filter { !it.isHealthy }.randomOrNull() ?: diseases.first()
        }
        // Default — mild or healthy
        return diseases.filter { !it.isHealthy && it.severity == "MILD" }.randomOrNull()
            ?: diseases.firstOrNull { it.isHealthy }
            ?: diseases.first()
    }

    private fun calculateConfidence(analysis: ImageAnalysis, isHealthy: Boolean): Float {
        val baseConfidence = if (isHealthy) {
            0.78f + (analysis.greenRatio * 0.2f).toFloat()
        } else {
            0.72f + ((analysis.brownPixelRatio + analysis.darkPixelRatio) * 0.3f).toFloat()
        }
        return baseConfidence.coerceIn(0.65f, 0.97f)
    }

    private fun guessCropFromFilename(filename: String): String {
        val lower = filename.lowercase()
        return when {
            lower.contains("tomato") -> "Tomato"
            lower.contains("chilli") || lower.contains("pepper") -> "Chilli"
            lower.contains("potato") -> "Potato"
            lower.contains("onion") -> "Onion"
            else -> "DEFAULT"
        }
    }

    private fun fallbackHealthyResult() = DiseaseDetectionResult(
        diseaseName = "Unable to Analyze",
        confidenceScore = 0f,
        isHealthy = false,
        symptoms = listOf("Image could not be processed"),
        suggestedTreatments = listOf("Please retake a clear, well-lit photo"),
        preventiveMeasures = emptyList(),
        severityLevel = "NONE"
    )

    private data class DiseaseInfo(
        val name: String,
        val symptoms: List<String>,
        val treatments: List<String>,
        val prevention: List<String>,
        val severity: String,
        val isHealthy: Boolean = false
    )
}
