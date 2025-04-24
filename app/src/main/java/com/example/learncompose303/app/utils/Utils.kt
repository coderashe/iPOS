package com.example.learncompose303.app.utils

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

object Utils {

    /**
     * Generates and shares a PDF invoice
     * @param message The invoice content (multiline text)
     * @param context Android context
     * @param fileNamePrefix Optional custom filename prefix (default: "Invoice")
     */
    fun generateAndShareInvoice(
        message: String,
        context: Context,
        fileNamePrefix: String = "Invoice"
    ) {
        try {
            // 1. Create PDF document
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(300, 600, 1).create()
            val page = pdfDocument.startPage(pageInfo)

            // 2. Draw text on PDF
            val canvas: Canvas = page.canvas
            val paint = Paint().apply { textSize = 12f }
            var yPosition = 20
            message.split("\n").forEach { line ->
                canvas.drawText(line, 10f, yPosition.toFloat(), paint)
                yPosition += 20
            }
            pdfDocument.finishPage(page)

            // 3. Save PDF to Documents folder
            val pdfFile = File(
                context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                "${fileNamePrefix}_${System.currentTimeMillis()}.pdf"
            )
            pdfDocument.writeTo(FileOutputStream(pdfFile))
            pdfDocument.close()

            // 4. Share PDF via Intent
            sharePdfFile(context, pdfFile)

        } catch (e: Exception) {
            Toast.makeText(
                context,
                "PDF Error: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun sharePdfFile(context: Context, pdfFile: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooserIntent = Intent.createChooser(
            sendIntent,
            "Share invoice via"
        )
        if (chooserIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(chooserIntent)
        } else {
            Toast.makeText(
                context,
                "No app available to share PDF",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
