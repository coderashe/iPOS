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
import com.ipos.data.entities.Invoice
import com.ipos.data.entities.InvoiceLine
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class BusinessInfo(
    val storeName: String,
    val deviceName: String,
    val contact: String? = null,
    val email: String? = null,
    val address: String? = null
)

object Utils {

    private const val PAGE_W = 595f
    private const val PAGE_H = 842f
    private const val MARGIN = 40f
    private const val CONTENT_W = PAGE_W - (MARGIN * 2)

    private val dateTimeFmt = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    private val dateOnlyFmt = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    /**
     * Backward-compatible plain text generator
     */
    fun generateAndShareInvoice(
        message: String,
        context: Context,
        fileNamePrefix: String = "Invoice"
    ) {
        try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(300, 600, 1).create()
            val page = pdfDocument.startPage(pageInfo)

            val canvas: Canvas = page.canvas
            val paint = Paint().apply { textSize = 12f }
            var yPosition = 20
            message.split("\n").forEach { line ->
                canvas.drawText(line, 10f, yPosition.toFloat(), paint)
                yPosition += 20
            }
            pdfDocument.finishPage(page)

            val pdfFile = File(
                context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                "${fileNamePrefix}_${System.currentTimeMillis()}.pdf"
            )
            pdfDocument.writeTo(FileOutputStream(pdfFile))
            pdfDocument.close()

            sharePdfFile(context, pdfFile, "Share invoice via")
        } catch (e: Exception) {
            Toast.makeText(context, "PDF Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Generates a sleek, professional A4 PDF invoice / receipt
     */
    fun generateAndShareReceiptPdf(
        context: Context,
        businessInfo: BusinessInfo,
        invoice: Invoice?,
        invoiceLine: InvoiceLine?,
        invoiceLines: List<InvoiceLine>
    ) {
        val pdfDocument = PdfDocument()
        try {
            var pageNumber = 1
            var pageInfo = PdfDocument.PageInfo.Builder(PAGE_W.toInt(), PAGE_H.toInt(), pageNumber).create()
            var currentPage = pdfDocument.startPage(pageInfo)
            var canvas = currentPage.canvas

            val dividerPaint = Paint().apply {
                color = Color.parseColor("#E2E8F0")
                strokeWidth = 1f
            }

            // Top accent banner
            val accentPaint = Paint().apply {
                color = Color.parseColor("#1E3A8A") // Deep Navy
                style = Paint.Style.FILL
            }
            canvas.drawRect(0f, 0f, PAGE_W, 6f, accentPaint)

            var y = MARGIN + 16f

            // Store Title
            val storeTitlePaint = Paint().apply {
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textSize = 22f
                color = Color.parseColor("#0F172A")
            }
            val titleName = businessInfo.storeName.ifBlank { "iPOS STORE" }
            canvas.drawText(titleName, MARGIN, y, storeTitlePaint)

            // Right header: TAX INVOICE
            val docTypePaint = Paint().apply {
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textSize = 18f
                color = Color.parseColor("#1E3A8A")
                textAlign = Paint.Align.RIGHT
            }
            canvas.drawText("TAX INVOICE", PAGE_W - MARGIN, y, docTypePaint)

            // Meta row 1
            y += 18f
            val metaPaint = Paint().apply {
                textSize = 10f
                color = Color.parseColor("#64748B")
            }
            val termText = if (businessInfo.deviceName.isNotBlank()) "  |  Terminal: ${businessInfo.deviceName}" else ""
            canvas.drawText("Store: $titleName$termText", MARGIN, y, metaPaint)

            val invNumPaint = Paint().apply {
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textSize = 11f
                color = Color.parseColor("#334155")
                textAlign = Paint.Align.RIGHT
            }
            val invId = invoice?.invoiceId ?: invoiceLine?.invoiceId ?: 0L
            canvas.drawText("Invoice #: INV-$invId", PAGE_W - MARGIN, y, invNumPaint)

            // Meta row 2: contact info & date
            y += 16f
            val contactParts = listOfNotNull(businessInfo.contact, businessInfo.email, businessInfo.address)
                .filter { it.isNotBlank() }
            if (contactParts.isNotEmpty()) {
                val contactStr = contactParts.joinToString("  •  ")
                canvas.drawText(truncateText(contactStr, metaPaint, 300f), MARGIN, y, metaPaint)
            }

            val invDateMs = invoice?.invoiceDate ?: invoiceLine?.invoiceDate ?: System.currentTimeMillis()
            val dateStr = dateTimeFmt.format(Date(invDateMs))
            val rightMetaPaint = Paint().apply {
                textSize = 10f
                color = Color.parseColor("#64748B")
                textAlign = Paint.Align.RIGHT
            }
            canvas.drawText(dateStr, PAGE_W - MARGIN, y, rightMetaPaint)

            // Meta row 3: Status Pill
            y += 18f
            val status = invoice?.status ?: invoiceLine?.invoiceStatus ?: "Paid"
            val isCreditNote = status.equals("Credit Note", ignoreCase = true)
            val badgeText = if (isCreditNote) "CREDIT NOTE" else "PAID"
            val badgeBgColor = if (isCreditNote) Color.parseColor("#FEE2E2") else Color.parseColor("#DCFCE7")
            val badgeTextColor = if (isCreditNote) Color.parseColor("#DC2626") else Color.parseColor("#16A34A")

            val badgeTextPaint = Paint().apply {
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textSize = 9f
                color = badgeTextColor
                textAlign = Paint.Align.CENTER
            }
            val badgeBgPaint = Paint().apply {
                style = Paint.Style.FILL
                color = badgeBgColor
            }
            val badgeW = badgeTextPaint.measureText(badgeText) + 18f
            val badgeH = 16f
            val badgeRect = RectF(PAGE_W - MARGIN - badgeW, y - 11f, PAGE_W - MARGIN, y + badgeH - 11f)
            canvas.drawRoundRect(badgeRect, 8f, 8f, badgeBgPaint)
            canvas.drawText(badgeText, badgeRect.centerX(), y + 1f, badgeTextPaint)

            // Divider
            y += 14f
            canvas.drawLine(MARGIN, y, PAGE_W - MARGIN, y, dividerPaint)

            // Customer & Cashier Section Card
            y += 14f
            val cardH = 46f
            val cardRect = RectF(MARGIN, y, PAGE_W - MARGIN, y + cardH)
            val cardBgPaint = Paint().apply {
                style = Paint.Style.FILL
                color = Color.parseColor("#F8FAFC")
            }
            val cardBorderPaint = Paint().apply {
                style = Paint.Style.STROKE
                strokeWidth = 1f
                color = Color.parseColor("#E2E8F0")
            }
            canvas.drawRoundRect(cardRect, 6f, 6f, cardBgPaint)
            canvas.drawRoundRect(cardRect, 6f, 6f, cardBorderPaint)

            val sectionLabelPaint = Paint().apply {
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textSize = 8f
                color = Color.parseColor("#94A3B8")
            }
            val sectionValPaint = Paint().apply {
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textSize = 10f
                color = Color.parseColor("#1E293B")
            }

            // Customer
            canvas.drawText("BILLED TO", MARGIN + 12f, y + 16f, sectionLabelPaint)
            val rawCustomer = invoiceLine?.customerName?.trim()
            val custName = if (!rawCustomer.isNullOrBlank()) rawCustomer else "Walk-in Customer"
            val custPhone = invoiceLine?.customerPhone?.trim()?.takeIf { it.isNotBlank() }
            val custDisplay = if (custPhone != null) "$custName ($custPhone)" else custName
            canvas.drawText(truncateText(custDisplay, sectionValPaint, 220f), MARGIN + 12f, y + 33f, sectionValPaint)

            // Cashier
            val midX = MARGIN + 260f
            canvas.drawText("ISSUED BY / CASHIER", midX, y + 16f, sectionLabelPaint)
            val rawCashier = invoiceLine?.cashier?.trim()
            val cashierDisplay = if (!rawCashier.isNullOrBlank()) rawCashier else "Cashier"
            canvas.drawText(truncateText(cashierDisplay, sectionValPaint, 220f), midX, y + 33f, sectionValPaint)

            y += cardH + 16f

            // Table Header helper
            fun drawTableHeader(c: Canvas, startY: Float): Float {
                val tableHeaderBg = Paint().apply {
                    style = Paint.Style.FILL
                    color = Color.parseColor("#F1F5F9")
                }
                val thRect = RectF(MARGIN, startY, PAGE_W - MARGIN, startY + 24f)
                c.drawRoundRect(thRect, 4f, 4f, tableHeaderBg)

                val thTextPaint = Paint().apply {
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    textSize = 9f
                    color = Color.parseColor("#475569")
                }
                val thRightPaint = Paint().apply {
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    textSize = 9f
                    color = Color.parseColor("#475569")
                    textAlign = Paint.Align.RIGHT
                }

                c.drawText("#", MARGIN + 10f, startY + 16f, thTextPaint)
                c.drawText("ITEM DESCRIPTION", MARGIN + 35f, startY + 16f, thTextPaint)
                c.drawText("QTY", MARGIN + 325f, startY + 16f, thRightPaint)
                c.drawText("UNIT PRICE", MARGIN + 415f, startY + 16f, thRightPaint)
                c.drawText("TOTAL", PAGE_W - MARGIN - 10f, startY + 16f, thRightPaint)
                return startY + 24f
            }

            y = drawTableHeader(canvas, y)

            val currencyCode = invoice?.currencyCode?.ifBlank { "USD" } ?: "USD"
            val rowTextPaint = Paint().apply {
                textSize = 10f
                color = Color.parseColor("#1E293B")
            }
            val rowRightPaint = Paint().apply {
                textSize = 10f
                color = Color.parseColor("#1E293B")
                textAlign = Paint.Align.RIGHT
            }
            val rowBgAlt = Paint().apply {
                style = Paint.Style.FILL
                color = Color.parseColor("#F8FAFC")
            }

            val col1 = MARGIN + 10f
            val col2 = MARGIN + 35f
            val col3 = MARGIN + 325f
            val col4 = MARGIN + 415f
            val col5 = PAGE_W - MARGIN - 10f

            val rowH = 22f
            invoiceLines.forEachIndexed { index, line ->
                if (y + rowH > PAGE_H - 140f) {
                    pdfDocument.finishPage(currentPage)
                    pageNumber++
                    pageInfo = PdfDocument.PageInfo.Builder(PAGE_W.toInt(), PAGE_H.toInt(), pageNumber).create()
                    currentPage = pdfDocument.startPage(pageInfo)
                    canvas = currentPage.canvas
                    y = MARGIN + 10f
                    y = drawTableHeader(canvas, y)
                }

                if (index % 2 == 1) {
                    canvas.drawRect(MARGIN, y, PAGE_W - MARGIN, y + rowH, rowBgAlt)
                }

                canvas.drawText("${index + 1}", col1, y + 15f, rowTextPaint)
                val prodName = line.productName.ifBlank { "Item #${line.productID}" }
                canvas.drawText(truncateText(prodName, rowTextPaint, 250f), col2, y + 15f, rowTextPaint)
                canvas.drawText("${line.productQtySold}", col3, y + 15f, rowRightPaint)
                canvas.drawText(formatMoney(currencyCode, line.productPrice), col4, y + 15f, rowRightPaint)
                val lineTotal = if (line.totalAmount > 0.0) line.totalAmount else (line.productPrice * line.productQtySold)
                canvas.drawText(formatMoney(currencyCode, lineTotal), col5, y + 15f, rowRightPaint)

                canvas.drawLine(MARGIN, y + rowH, PAGE_W - MARGIN, y + rowH, dividerPaint)
                y += rowH
            }

            // Totals Section
            y += 14f
            val rawTotal = invoice?.totalAmount ?: invoiceLine?.totalAmount ?: invoiceLines.sumOf { it.totalAmount }
            val summaryBoxW = 230f
            val summaryLeft = PAGE_W - MARGIN - summaryBoxW

            val summaryLabelPaint = Paint().apply {
                textSize = 10f
                color = Color.parseColor("#64748B")
            }
            val summaryValPaint = Paint().apply {
                textSize = 10f
                color = Color.parseColor("#1E293B")
                textAlign = Paint.Align.RIGHT
            }
            val totalLabelPaint = Paint().apply {
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textSize = 13f
                color = Color.parseColor("#0F172A")
            }
            val totalValPaint = Paint().apply {
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textSize = 14f
                color = Color.parseColor("#1E3A8A")
                textAlign = Paint.Align.RIGHT
            }

            // Subtotal
            canvas.drawText("Subtotal:", summaryLeft, y + 12f, summaryLabelPaint)
            canvas.drawText(formatMoney(currencyCode, rawTotal), col5, y + 12f, summaryValPaint)
            y += 18f

            // Tax (if any)
            val tax = invoiceLine?.taxAmount ?: 0.0
            if (tax > 0.0) {
                canvas.drawText("Tax (VAT):", summaryLeft, y + 12f, summaryLabelPaint)
                canvas.drawText(formatMoney(currencyCode, tax), col5, y + 12f, summaryValPaint)
                y += 18f
            }

            // Divider in totals box
            val totalDivider = Paint().apply {
                color = Color.parseColor("#CBD5E1")
                strokeWidth = 1.5f
            }
            canvas.drawLine(summaryLeft, y + 4f, col5, y + 4f, totalDivider)
            y += 16f

            // Total Due / Paid
            val totalLabel = if (isCreditNote) "TOTAL REFUND:" else "TOTAL PAID:"
            canvas.drawText(totalLabel, summaryLeft, y + 12f, totalLabelPaint)
            canvas.drawText(formatMoney(currencyCode, rawTotal), col5, y + 12f, totalValPaint)
            y += 24f

            // Tendered & Change
            val tendered = invoiceLine?.amountTendered ?: 0.0
            val change = invoiceLine?.changeBalance ?: 0.0
            if (tendered > 0.0) {
                canvas.drawText("Amount Tendered:", summaryLeft, y + 12f, summaryLabelPaint)
                canvas.drawText(formatMoney(currencyCode, tendered), col5, y + 12f, summaryValPaint)
                y += 16f
                if (change >= 0.0) {
                    canvas.drawText("Change Given:", summaryLeft, y + 12f, summaryLabelPaint)
                    canvas.drawText(formatMoney(currencyCode, change), col5, y + 12f, summaryValPaint)
                    y += 16f
                }
            }

            // Footer
            val footY = PAGE_H - MARGIN - 18f
            canvas.drawLine(MARGIN, footY - 14f, PAGE_W - MARGIN, footY - 14f, dividerPaint)

            val footThanksPaint = Paint().apply {
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textSize = 10f
                color = Color.parseColor("#475569")
                textAlign = Paint.Align.CENTER
            }
            val footSubPaint = Paint().apply {
                textSize = 8f
                color = Color.parseColor("#94A3B8")
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("Thank you for your business!", PAGE_W / 2f, footY, footThanksPaint)
            canvas.drawText("This is an official computer-generated receipt powered by iPOS", PAGE_W / 2f, footY + 13f, footSubPaint)

            pdfDocument.finishPage(currentPage)

            // Save PDF
            val pdfFile = File(
                context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                "Receipt_INV_${invId}_${System.currentTimeMillis()}.pdf"
            )
            pdfDocument.writeTo(FileOutputStream(pdfFile))
            pdfDocument.close()

            sharePdfFile(context, pdfFile, "Print or Share Receipt")
        } catch (e: Exception) {
            try { pdfDocument.close() } catch (_: Exception) {}
            Toast.makeText(context, "Failed to generate receipt PDF: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Generates a sleek, professional A4 PDF Sales Summary Report
     */
    fun generateAndShareSalesReportPdf(
        context: Context,
        storeName: String,
        filterTitle: String,
        invoices: List<Invoice>,
        currencyTotals: Map<String, Double>
    ) {
        val pdfDocument = PdfDocument()
        try {
            var pageNumber = 1
            var pageInfo = PdfDocument.PageInfo.Builder(PAGE_W.toInt(), PAGE_H.toInt(), pageNumber).create()
            var currentPage = pdfDocument.startPage(pageInfo)
            var canvas = currentPage.canvas

            val dividerPaint = Paint().apply {
                color = Color.parseColor("#E2E8F0")
                strokeWidth = 1f
            }

            fun drawPageHeader(c: Canvas, isFirstPage: Boolean): Float {
                // Top accent
                val accentPaint = Paint().apply {
                    color = Color.parseColor("#1E3A8A")
                    style = Paint.Style.FILL
                }
                c.drawRect(0f, 0f, PAGE_W, 6f, accentPaint)

                var curY = MARGIN + 16f
                val titlePaint = Paint().apply {
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    textSize = 20f
                    color = Color.parseColor("#0F172A")
                }
                val storeDisplay = storeName.ifBlank { "iPOS STORE" }
                c.drawText(storeDisplay, MARGIN, curY, titlePaint)

                val docTypePaint = Paint().apply {
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    textSize = 16f
                    color = Color.parseColor("#1E3A8A")
                    textAlign = Paint.Align.RIGHT
                }
                c.drawText("SALES REPORT", PAGE_W - MARGIN, curY, docTypePaint)

                curY += 18f
                val metaPaint = Paint().apply {
                    textSize = 10f
                    color = Color.parseColor("#64748B")
                }
                c.drawText("Period: $filterTitle", MARGIN, curY, metaPaint)

                val rightMeta = Paint().apply {
                    textSize = 10f
                    color = Color.parseColor("#64748B")
                    textAlign = Paint.Align.RIGHT
                }
                c.drawText("Generated: ${dateTimeFmt.format(Date())}", PAGE_W - MARGIN, curY, rightMeta)

                curY += 12f
                c.drawLine(MARGIN, curY, PAGE_W - MARGIN, curY, dividerPaint)
                curY += 14f

                if (isFirstPage) {
                    // Summary KPI Cards
                    val cardH = 50f
                    val gap = 12f
                    val numCards = 2
                    val cardW = (CONTENT_W - gap) / numCards

                    // Card 1: Total Sales
                    val c1Rect = RectF(MARGIN, curY, MARGIN + cardW, curY + cardH)
                    val cardBgPaint = Paint().apply {
                        style = Paint.Style.FILL
                        color = Color.parseColor("#F1F5F9")
                    }
                    val cardBorder = Paint().apply {
                        style = Paint.Style.STROKE
                        strokeWidth = 1f
                        color = Color.parseColor("#CBD5E1")
                    }
                    c.drawRoundRect(c1Rect, 6f, 6f, cardBgPaint)
                    c.drawRoundRect(c1Rect, 6f, 6f, cardBorder)

                    val kpiLabel = Paint().apply {
                        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                        textSize = 8f
                        color = Color.parseColor("#64748B")
                    }
                    val kpiVal = Paint().apply {
                        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                        textSize = 11f
                        color = Color.parseColor("#0F172A")
                    }
                    c.drawText("TOTAL REVENUE", MARGIN + 10f, curY + 16f, kpiLabel)

                    val revText = if (currencyTotals.isEmpty()) {
                        "$0.00"
                    } else {
                        currencyTotals.entries.joinToString("  |  ") { (curr, sum) ->
                            formatMoney(curr, sum)
                        }
                    }
                    c.drawText(truncateText(revText, kpiVal, cardW - 20f), MARGIN + 10f, curY + 36f, kpiVal)

                    // Card 2: Transactions
                    val c2X = MARGIN + cardW + gap
                    val c2Rect = RectF(c2X, curY, c2X + cardW, curY + cardH)
                    c.drawRoundRect(c2Rect, 6f, 6f, cardBgPaint)
                    c.drawRoundRect(c2Rect, 6f, 6f, cardBorder)

                    c.drawText("TOTAL TRANSACTIONS", c2X + 10f, curY + 16f, kpiLabel)
                    val countValPaint = Paint().apply {
                        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                        textSize = 15f
                        color = Color.parseColor("#1E3A8A")
                    }
                    c.drawText("${invoices.size} Invoices", c2X + 10f, curY + 36f, countValPaint)

                    curY += cardH + 16f
                }

                // Table Header
                val thBg = Paint().apply {
                    style = Paint.Style.FILL
                    color = Color.parseColor("#1E293B")
                }
                val thRect = RectF(MARGIN, curY, PAGE_W - MARGIN, curY + 22f)
                c.drawRoundRect(thRect, 4f, 4f, thBg)

                val thWhite = Paint().apply {
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    textSize = 8.5f
                    color = Color.WHITE
                }
                val thWhiteRight = Paint().apply {
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    textSize = 8.5f
                    color = Color.WHITE
                    textAlign = Paint.Align.RIGHT
                }

                c.drawText("#", MARGIN + 8f, curY + 15f, thWhite)
                c.drawText("INVOICE #", MARGIN + 28f, curY + 15f, thWhite)
                c.drawText("DATE & TIME", MARGIN + 110f, curY + 15f, thWhite)
                c.drawText("DEVICE / REGISTER", MARGIN + 250f, curY + 15f, thWhite)
                c.drawText("STATUS", MARGIN + 370f, curY + 15f, thWhite)
                c.drawText("AMOUNT", PAGE_W - MARGIN - 8f, curY + 15f, thWhiteRight)

                return curY + 22f
            }

            var y = drawPageHeader(canvas, isFirstPage = true)

            val rowText = Paint().apply {
                textSize = 9.5f
                color = Color.parseColor("#1E293B")
            }
            val rowRight = Paint().apply {
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textSize = 9.5f
                color = Color.parseColor("#0F172A")
                textAlign = Paint.Align.RIGHT
            }
            val altBg = Paint().apply {
                style = Paint.Style.FILL
                color = Color.parseColor("#F8FAFC")
            }

            val rowH = 20f

            fun drawFooter(c: Canvas, pageNum: Int) {
                val footY = PAGE_H - MARGIN - 10f
                c.drawLine(MARGIN, footY - 10f, PAGE_W - MARGIN, footY - 10f, dividerPaint)
                val footMeta = Paint().apply {
                    textSize = 8.5f
                    color = Color.parseColor("#94A3B8")
                }
                val footRight = Paint().apply {
                    textSize = 8.5f
                    color = Color.parseColor("#94A3B8")
                    textAlign = Paint.Align.RIGHT
                }
                c.drawText("iPOS Sales Report  •  Confidential", MARGIN, footY, footMeta)
                c.drawText("Page $pageNum", PAGE_W - MARGIN, footY, footRight)
            }

            if (invoices.isEmpty()) {
                val emptyPaint = Paint().apply {
                    textSize = 12f
                    color = Color.parseColor("#64748B")
                    textAlign = Paint.Align.CENTER
                }
                canvas.drawText("No transactions recorded for $filterTitle", PAGE_W / 2f, y + 40f, emptyPaint)
            } else {
                invoices.forEachIndexed { index, inv ->
                    if (y + rowH > PAGE_H - MARGIN - 30f) {
                        drawFooter(canvas, pageNumber)
                        pdfDocument.finishPage(currentPage)
                        pageNumber++
                        pageInfo = PdfDocument.PageInfo.Builder(PAGE_W.toInt(), PAGE_H.toInt(), pageNumber).create()
                        currentPage = pdfDocument.startPage(pageInfo)
                        canvas = currentPage.canvas
                        y = drawPageHeader(canvas, isFirstPage = false)
                    }

                    if (index % 2 == 1) {
                        canvas.drawRect(MARGIN, y, PAGE_W - MARGIN, y + rowH, altBg)
                    }

                    canvas.drawText("${index + 1}", MARGIN + 8f, y + 14f, rowText)
                    canvas.drawText("#INV-${inv.invoiceId}", MARGIN + 28f, y + 14f, rowText)
                    val dateFormatted = dateTimeFmt.format(Date(inv.invoiceDate))
                    canvas.drawText(truncateText(dateFormatted, rowText, 130f), MARGIN + 110f, y + 14f, rowText)

                    val dev = inv.deviceName?.ifBlank { "POS" } ?: "POS"
                    canvas.drawText(truncateText(dev, rowText, 110f), MARGIN + 250f, y + 14f, rowText)

                    val stat = inv.status.ifBlank { "Paid" }
                    val statPaint = Paint().apply {
                        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                        textSize = 8.5f
                        color = if (stat.equals("Credit Note", ignoreCase = true)) Color.parseColor("#DC2626") else Color.parseColor("#16A34A")
                    }
                    canvas.drawText(stat, MARGIN + 370f, y + 14f, statPaint)

                    val curr = inv.currencyCode.ifBlank { "USD" }
                    canvas.drawText(formatMoney(curr, inv.totalAmount), PAGE_W - MARGIN - 8f, y + 14f, rowRight)

                    canvas.drawLine(MARGIN, y + rowH, PAGE_W - MARGIN, y + rowH, dividerPaint)
                    y += rowH
                }
            }

            drawFooter(canvas, pageNumber)
            pdfDocument.finishPage(currentPage)

            val safeFilter = filterTitle.replace(" ", "_")
            val pdfFile = File(
                context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                "SalesReport_${safeFilter}_${System.currentTimeMillis()}.pdf"
            )
            pdfDocument.writeTo(FileOutputStream(pdfFile))
            pdfDocument.close()

            sharePdfFile(context, pdfFile, "Share Sales Report via")
        } catch (e: Exception) {
            try { pdfDocument.close() } catch (_: Exception) {}
            Toast.makeText(context, "Failed to generate sales report PDF: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun sharePdfFile(context: Context, pdfFile: File, chooserTitle: String) {
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
        val chooserIntent = Intent.createChooser(sendIntent, chooserTitle)
        if (chooserIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(chooserIntent)
        } else {
            Toast.makeText(context, "No app available to share PDF", Toast.LENGTH_SHORT).show()
        }
    }

    private fun truncateText(text: String, paint: Paint, maxWidth: Float): String {
        if (paint.measureText(text) <= maxWidth) return text
        var truncated = text
        while (truncated.isNotEmpty() && paint.measureText("$truncated...") > maxWidth) {
            truncated = truncated.dropLast(1)
        }
        return "$truncated..."
    }

    private fun formatMoney(currency: String, amount: Double): String {
        val symbol = when (currency.trim().uppercase()) {
            "USD" -> "$"
            "ZIG" -> "ZiG "
            "ZAR" -> "R "
            else -> "$currency "
        }
        return String.format(Locale.US, "%s%.2f", symbol, amount)
    }
}
