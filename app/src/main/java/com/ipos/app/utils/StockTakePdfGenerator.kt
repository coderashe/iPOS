package com.ipos.app.utils

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.widget.Toast
import androidx.core.content.FileProvider
import com.ipos.data.entities.StockTakeSessionStatus
import com.ipos.viewmodels.StockTakeDetail
import com.ipos.viewmodels.SuggestionType
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Generates a multi-page A4 PDF summary of a stock-take session and
 * shares it via the system share sheet.
 * Page size: 595 x 842 pt (A4 at 72 dpi)
 *
 * Key rule: doc.writeTo() MUST be called before doc.close().
 */
object StockTakePdfGenerator {

    private const val PAGE_W = 595f
    private const val PAGE_H = 842f
    private const val MARGIN = 40f
    private const val CONTENT_W = PAGE_W - MARGIN * 2
    private const val LINE_H = 18f
    private const val SECTION_GAP = 20f

    private val fmt = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())

    fun generateAndShare(context: Context, detail: StockTakeDetail) {
        val doc = PdfDocument()
        try {
            // ── Paint definitions ─────────────────────────────────────────
            val titlePaint = Paint().apply {
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textSize = 20f; color = Color.parseColor("#1A237E")
            }
            val sectionPaint = Paint().apply {
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textSize = 13f; color = Color.parseColor("#37474F")
            }
            val bodyPaint = Paint().apply {
                textSize = 11f; color = Color.parseColor("#212121")
            }
            val smallPaint = Paint().apply {
                textSize = 10f; color = Color.parseColor("#616161")
            }
            val redPaint = Paint().apply {
                textSize = 11f; color = Color.parseColor("#C62828")
            }
            val greenPaint = Paint().apply {
                textSize = 11f; color = Color.parseColor("#2E7D32")
            }
            val dividerPaint = Paint().apply {
                strokeWidth = 1f; color = Color.parseColor("#E0E0E0")
            }
            val kpiBgPaint = Paint().apply {
                style = Paint.Style.FILL; color = Color.parseColor("#E3F2FD")
            }
            val kpiBorderPaint = Paint().apply {
                style = Paint.Style.STROKE; strokeWidth = 1f; color = Color.parseColor("#90CAF9")
            }
            val warnBgPaint = Paint().apply {
                style = Paint.Style.FILL; color = Color.parseColor("#FFF8E1")
            }
            val okBgPaint = Paint().apply {
                style = Paint.Style.FILL; color = Color.parseColor("#E8F5E9")
            }
            val infoBgPaint = Paint().apply {
                style = Paint.Style.FILL; color = Color.parseColor("#E3F2FD")
            }

            // ── Page manager ──────────────────────────────────────────────
            var pageNumber = 0
            var currentPage: PdfDocument.Page? = null
            lateinit var canvas: Canvas
            var y = MARGIN

            fun startNewPage() {
                currentPage?.let { doc.finishPage(it) }   // finish previous
                pageNumber++
                val info = PdfDocument.PageInfo.Builder(
                    PAGE_W.toInt(), PAGE_H.toInt(), pageNumber
                ).create()
                currentPage = doc.startPage(info)
                canvas = currentPage!!.canvas
                y = MARGIN
            }

            fun ensureSpace(needed: Float) {
                if (y + needed > PAGE_H - MARGIN - 30f) startNewPage()
            }

            // ── Page 1 ────────────────────────────────────────────────────
            startNewPage()
            y += 10f

            // Title
            canvas.drawText("Stock Take Report", MARGIN, y, titlePaint)
            y += 28f

            val statusLabel = when (StockTakeSessionStatus.valueOf(detail.session.status)) {
                StockTakeSessionStatus.OPEN -> "In Progress"
                StockTakeSessionStatus.COMPLETED -> "Completed"
                StockTakeSessionStatus.CANCELLED -> "Cancelled"
            }
            canvas.drawText(
                "Session #${detail.session.sessionId}  •  $statusLabel",
                MARGIN, y, sectionPaint
            )
            y += LINE_H
            canvas.drawText("Started by: ${detail.session.startedBy}", MARGIN, y, smallPaint)
            y += LINE_H
            canvas.drawText(
                "Started: ${fmt.format(Date(detail.session.startedAt))}",
                MARGIN, y, smallPaint
            )
            detail.session.closedAt?.let {
                canvas.drawText("Closed: ${fmt.format(Date(it))}", MARGIN + 230f, y, smallPaint)
            }
            y += LINE_H + 4f
            canvas.drawLine(MARGIN, y, PAGE_W - MARGIN, y, dividerPaint)
            y += SECTION_GAP

            // ── KPI grid (2 rows × 3 cols) ────────────────────────────────
            val kpis = listOf(
                "Counted" to detail.countedCount.toString(),
                "Total Catalog" to detail.totalCatalog.toString(),
                "Coverage" to "${detail.coveragePct}%",
                "Variances" to detail.varianceCount.toString(),
                "Shortages" to detail.shortageCount.toString(),
                "Surpluses" to detail.surplusCount.toString()
            )
            val kpiW = (CONTENT_W - 12f) / 3f
            val kpiH = 52f
            kpis.chunked(3).forEach { row ->
                ensureSpace(kpiH + 8f)
                row.forEachIndexed { col, (label, value) ->
                    val kx = MARGIN + col * (kpiW + 6f)
                    val rect = RectF(kx, y, kx + kpiW, y + kpiH)
                    canvas.drawRoundRect(rect, 6f, 6f, kpiBgPaint)
                    canvas.drawRoundRect(rect, 6f, 6f, kpiBorderPaint)
                    val valPaint = Paint().apply {
                        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                        textSize = 18f; color = Color.parseColor("#1565C0")
                    }
                    canvas.drawText(value, kx + 10f, y + 26f, valPaint)
                    canvas.drawText(label, kx + 10f, y + 44f, smallPaint)
                }
                y += kpiH + 8f
            }
            y += SECTION_GAP

            // ── Business Insights ─────────────────────────────────────────
            ensureSpace(LINE_H + 4f)
            canvas.drawText("Business Insights", MARGIN, y, sectionPaint)
            y += LINE_H + 4f

            detail.suggestions.forEach { suggestion ->
                val bgPaint = when (suggestion.type) {
                    SuggestionType.WARNING -> warnBgPaint
                    SuggestionType.OK -> okBgPaint
                    SuggestionType.INFO -> infoBgPaint
                }
                val prefix = when (suggestion.type) {
                    SuggestionType.WARNING -> "[!] "
                    SuggestionType.OK -> "[OK] "
                    SuggestionType.INFO -> "[i] "
                }
                val lines = wrapText(
                    "$prefix${suggestion.title}: ${suggestion.body}",
                    bodyPaint, CONTENT_W - 20f
                )
                val boxH = lines.size * LINE_H + 12f
                ensureSpace(boxH + 8f)
                val rect = RectF(MARGIN, y, PAGE_W - MARGIN, y + boxH)
                canvas.drawRoundRect(rect, 6f, 6f, bgPaint)
                var ty = y + LINE_H
                lines.forEach { line ->
                    canvas.drawText(line, MARGIN + 10f, ty, bodyPaint)
                    ty += LINE_H
                }
                y += boxH + 8f
            }
            y += SECTION_GAP

            // ── Variance table ────────────────────────────────────────────
            if (detail.varianceRows.isNotEmpty()) {
                ensureSpace(LINE_H * 3)
                canvas.drawText(
                    "Variance Details  (${detail.varianceRows.size} items, sorted by magnitude)",
                    MARGIN, y, sectionPaint
                )
                y += LINE_H + 4f

                val colProduct = MARGIN
                val colSys = PAGE_W - MARGIN - 180f
                val colCounted = PAGE_W - MARGIN - 100f
                val colVar = PAGE_W - MARGIN - 30f

                val headerPaint = Paint().apply {
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    textSize = 10f; color = Color.parseColor("#455A64")
                }
                canvas.drawText("Product", colProduct, y, headerPaint)
                canvas.drawText("System", colSys, y, headerPaint)
                canvas.drawText("Counted", colCounted, y, headerPaint)
                canvas.drawText("Var", colVar, y, headerPaint)
                y += 4f
                canvas.drawLine(MARGIN, y, PAGE_W - MARGIN, y, dividerPaint)
                y += LINE_H

                detail.varianceRows.forEachIndexed { i, row ->
                    ensureSpace(LINE_H + 4f)
                    if (i % 2 == 0) {
                        canvas.drawRect(
                            RectF(MARGIN, y - LINE_H + 4f, PAGE_W - MARGIN, y + 4f),
                            Paint().apply {
                                style = Paint.Style.FILL
                                color = Color.parseColor("#FAFAFA")
                            }
                        )
                    }
                    val name = if (row.displayName.length > 38)
                        row.displayName.take(36) + "…" else row.displayName
                    canvas.drawText(name, colProduct, y, bodyPaint)
                    canvas.drawText(row.systemQty.toString(), colSys, y, bodyPaint)
                    canvas.drawText("%.0f".format(row.countedQty), colCounted, y, bodyPaint)
                    val varPaint = if (row.variance < 0) redPaint else greenPaint
                    val varText = if (row.variance < 0) "%.0f".format(row.variance)
                    else "+%.0f".format(row.variance)
                    canvas.drawText(varText, colVar, y, varPaint)
                    y += LINE_H
                    if (!row.reason.isNullOrBlank()) {
                        ensureSpace(LINE_H)
                        canvas.drawText("  Reason: ${row.reason}", colProduct, y, smallPaint)
                        y += LINE_H
                    }
                }
                y += SECTION_GAP
            }

            // ── Uncounted gap analysis ────────────────────────────────────
            if (detail.uncountedProducts.isNotEmpty()) {
                ensureSpace(LINE_H * 3)
                canvas.drawText(
                    "Uncounted Products  (${detail.uncountedProducts.size} items)",
                    MARGIN, y, sectionPaint
                )
                y += LINE_H + 4f
                detail.uncountedProducts.forEach { pwb ->
                    ensureSpace(LINE_H + 2f)
                    val name = "${pwb.brand.brandName} - ${pwb.product.productDescription}"
                    val truncated = if (name.length > 55) name.take(53) + "…" else name
                    canvas.drawText("• $truncated", MARGIN + 8f, y, smallPaint)
                    y += LINE_H
                }
                y += SECTION_GAP
            }

            // ── Footer on whatever is the last page ───────────────────────
            val footerY = PAGE_H - 22f
            canvas.drawLine(MARGIN, footerY - 14f, PAGE_W - MARGIN, footerY - 14f, dividerPaint)
            canvas.drawText(
                "Generated by iPOS  •  ${fmt.format(Date())}",
                MARGIN, footerY, smallPaint
            )

            // Finish the last open page
            currentPage?.let { doc.finishPage(it) }

            // ── Write to file BEFORE closing ──────────────────────────────
            val sessionId = detail.session.sessionId
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(Date(detail.session.startedAt))
            val pdfFile = File(
                context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                "StockTake_Session${sessionId}_$dateStr.pdf"
            )
            FileOutputStream(pdfFile).use { doc.writeTo(it) }  // write first
            doc.close()                                          // then close

            sharePdf(context, pdfFile)

        } catch (e: Exception) {
            doc.close()
            Toast.makeText(context, "PDF Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun wrapText(text: String, paint: Paint, maxWidth: Float): List<String> {
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var current = ""
        for (word in words) {
            val test = if (current.isEmpty()) word else "$current $word"
            if (paint.measureText(test) <= maxWidth) current = test
            else { if (current.isNotEmpty()) lines.add(current); current = word }
        }
        if (current.isNotEmpty()) lines.add(current)
        return lines
    }

    private fun sharePdf(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(
            context, "${context.packageName}.fileprovider", file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Stock Take Report — ${file.name}")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(intent, "Share Stock Take Report")
        if (chooser.resolveActivity(context.packageManager) != null) {
            context.startActivity(chooser)
        } else {
            Toast.makeText(context, "No app available to share PDF", Toast.LENGTH_SHORT).show()
        }
    }
}
