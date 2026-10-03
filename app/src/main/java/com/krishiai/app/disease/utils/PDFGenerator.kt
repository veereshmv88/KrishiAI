package com.krishiai.app.disease.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import com.krishiai.app.disease.data.DiseaseScanEntity
import java.io.File
import java.io.FileOutputStream

object PDFGenerator {

    fun generateReport(context: Context, scan: DiseaseScanEntity): File? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
        val page = pdfDocument.startPage(pageInfo)
        
        val canvas: Canvas = page.canvas
        val paint = Paint()
        
        paint.textSize = 24f
        paint.isFakeBoldText = true
        canvas.drawText("KrishiAI - Disease Diagnosis Report", 50f, 50f, paint)

        paint.textSize = 16f
        paint.isFakeBoldText = false
        canvas.drawText("Crop: ${scan.cropName}", 50f, 100f, paint)
        canvas.drawText("Disease: ${scan.diseaseName}", 50f, 130f, paint)
        canvas.drawText("Severity: ${scan.severity}", 50f, 160f, paint)
        canvas.drawText("Confidence: ${(scan.confidence * 100).toInt()}%", 50f, 190f, paint)
        canvas.drawText("Health Score: ${scan.healthScore}/100", 50f, 220f, paint)
        
        // Finalize
        pdfDocument.finishPage(page)
        
        val directory = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        val file = File(directory, "KrishiAI_Report_${System.currentTimeMillis()}.pdf")
        
        try {
            pdfDocument.writeTo(FileOutputStream(file))
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        } finally {
            pdfDocument.close()
        }
        
        return file
    }
}
