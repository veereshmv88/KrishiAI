package com.krishiai.app.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfReportGenerator {
    fun generateDiseaseReport(
        context: Context,
        cropName: String,
        diseaseName: String,
        scientificName: String,
        confidenceScore: Float,
        severityLevel: String,
        healthScore: Int,
        treatment: String,
        organicTreatments: String,
        chemicalTreatments: String,
        recommendedFertilizers: String
    ): File? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 Size
        val page = pdfDocument.startPage(pageInfo)
        
        val canvas: Canvas = page.canvas
        val paint = Paint()

        // Title
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 24f
        paint.color = Color.rgb(46, 125, 50) // Green
        canvas.drawText("KrishiAI Crop Health Report", 150f, 80f, paint)

        // Metadata
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 14f
        paint.color = Color.BLACK
        val dateStr = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date())
        canvas.drawText("Date: $dateStr", 50f, 130f, paint)

        // Report Summary
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 18f
        canvas.drawText("Professional Summary", 50f, 180f, paint)
        
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 14f
        canvas.drawText("Crop: $cropName", 50f, 210f, paint)
        canvas.drawText("Disease Detected: $diseaseName", 50f, 240f, paint)
        canvas.drawText("Scientific Name: $scientificName", 50f, 270f, paint)
        canvas.drawText("Confidence: ${(confidenceScore * 100).toInt()}%", 50f, 300f, paint)
        canvas.drawText("Health Score: $healthScore/100", 50f, 330f, paint)
        canvas.drawText("Severity: $severityLevel", 50f, 360f, paint)

        // Expert Solutions
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 18f
        canvas.drawText("Expert Solutions", 50f, 410f, paint)
        
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 14f
        
        var yPos = 440f
        fun drawWrappedText(text: String, startX: Float) {
            val words = text.split(" ")
            var line = ""
            for (word in words) {
                if (paint.measureText("$line $word") < 500f) {
                    line = "$line $word"
                } else {
                    canvas.drawText(line, startX, yPos, paint)
                    yPos += 25f
                    line = word
                }
            }
            if (line.isNotEmpty()) {
                canvas.drawText(line, startX, yPos, paint)
                yPos += 25f
            }
        }

        canvas.drawText("General Treatment:", 50f, yPos, paint)
        yPos += 25f
        drawWrappedText(treatment, 70f)
        yPos += 15f
        
        canvas.drawText("Organic Treatments:", 50f, yPos, paint)
        yPos += 25f
        drawWrappedText(organicTreatments, 70f)
        yPos += 15f

        canvas.drawText("Chemical Treatments:", 50f, yPos, paint)
        yPos += 25f
        drawWrappedText(chemicalTreatments, 70f)
        yPos += 15f
        
        canvas.drawText("Recommended Fertilizers:", 50f, yPos, paint)
        yPos += 25f
        drawWrappedText(recommendedFertilizers, 70f)

        pdfDocument.finishPage(page)

        val directory = File(context.cacheDir, "reports")
        if (!directory.exists()) {
            directory.mkdirs()
        }
        val file = File(directory, "CropHealthReport_${System.currentTimeMillis()}.pdf")
        
        return try {
            pdfDocument.writeTo(FileOutputStream(file))
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            pdfDocument.close()
        }
    }
}
