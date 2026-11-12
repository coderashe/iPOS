package com.ipos.data.entities

import android.util.Log
import com.ipos.data.CsvInvoiceItem
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

class CsvParser {

    fun parseProductCsv(inputStream: InputStream): List<ProductCsvModel> {
        val productList = mutableListOf<ProductCsvModel>()
        val reader = BufferedReader(InputStreamReader(inputStream))
        var line: String?
        var lineNumber = 0

        try {
            // Skip the header row
            val header = reader.readLine()
            lineNumber++
            Log.d("CsvParser", "Header: $header")
            
            // Detect delimiter (tab or comma)
            val delimiter = if (header?.contains('\t') == true) {
                Log.d("CsvParser", "Detected TAB-separated file")
                "\t"
            } else {
                Log.d("CsvParser", "Detected COMMA-separated file")
                ","
            }

            while (reader.readLine().also { line = it } != null) {
                lineNumber++
                line?.let {
                    try {
                        Log.d("CsvParser", "Line $lineNumber: $it")
                        val tokens = it.split(delimiter)
                        Log.d("CsvParser", "Tokens count: ${tokens.size}, Tokens: $tokens")
                        
                        if (tokens.size >= 12) {
                            val productDescription = tokens[0].trim()
                            val productQty = tokens[1].trim().toLongOrNull() ?: 0L
                            val brandID = tokens[2].trim().toLongOrNull() ?: 0L
                            val shoeSizeId = tokens[3].trim().toLongOrNull() ?: 0L
                            val saleable = tokens[4].trim().toBooleanStrictOrNull() ?: true
                            val purchasable = tokens[5].trim().toBooleanStrictOrNull() ?: true
                            val averageCost = tokens[6].trim().toDoubleOrNull() ?: 0.0
                            val standardCost = tokens[7].trim().toDoubleOrNull() ?: 0.0
                            val salesTaxCode = tokens[8].trim().ifBlank { null }
                            val productCode = tokens[9].trim()
                            val inclusiveTaxed = tokens[10].trim().toBooleanStrictOrNull() ?: true
                            val imageUri = tokens[11].trim()

                            productList.add(
                                ProductCsvModel(
                                    productDescription,
                                    productQty,
                                    brandID,
                                    shoeSizeId,
                                    saleable,
                                    purchasable,
                                    averageCost,
                                    standardCost,
                                    salesTaxCode,
                                    productCode,
                                    inclusiveTaxed,
                                    imageUri = imageUri,
                                    lastUpdated = System.currentTimeMillis()
                                )
                            )
                            Log.d("CsvParser", "✅ Parsed product: $productDescription")
                        } else {
                            Log.w("CsvParser", "⚠️ Line $lineNumber has only ${tokens.size} tokens, expected 12")
                        }
                    } catch (e: Exception) {
                        Log.e("CsvParser", "❌ Error parsing line $lineNumber: ${e.message}", e)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("CsvParser", "❌ Error reading CSV: ${e.message}", e)
        } finally {
            reader.close()
        }
        
        Log.d("CsvParser", "Total products parsed: ${productList.size}")
        return productList
    }

    fun parsePriceListCsv(inputStream: InputStream): List<PriceListCsvModel> {
        val priceList = mutableListOf<PriceListCsvModel>()
        val reader = BufferedReader(InputStreamReader(inputStream))
        var line: String?

        // Skip the header row
        reader.readLine()

        while (reader.readLine().also { line = it } != null) {
            line?.let {
                val tokens = it.split(",")
                if (tokens.size >= 6) {
                    val productId = tokens[0].toLong()
                    val productDescription = tokens[1]
                    val unitCost = tokens[2].toDouble()
                    val sellingPrice = tokens[3].toDouble()
                    val markupPercentage = tokens[4].toDouble()
                    val grossProfitPercentage = tokens[5].toDouble()

                    priceList.add(
                        PriceListCsvModel(
                            productId,
                            productDescription,
                            unitCost,
                            sellingPrice,
                            markupPercentage,
                            grossProfitPercentage
                        )
                    )
                }
            }
        }
        reader.close()
        return priceList
    }

    fun parseInvoiceCsv(inputStream: InputStream): List<CsvInvoiceItem> {
        val items = mutableListOf<CsvInvoiceItem>()
        val reader = BufferedReader(InputStreamReader(inputStream))
        var lineNumber = 0

        try {
            val header = reader.readLine()
            lineNumber++
            Log.d("InvoiceCsvParser", "Header: $header")

            val delimiter = if (header?.contains('\t') == true) {
                Log.d("InvoiceCsvParser", "Detected TAB-separated file")
                "\t"
            } else {
                Log.d("InvoiceCsvParser", "Detected COMMA-separated file")
                ","
            }

            reader.forEachLine { line ->
                lineNumber++
                try {
                    val tokens = line.split(delimiter).map { it.trim() }
                    if (tokens.size >= 6) {
                        items.add(
                            CsvInvoiceItem(
                                brand = tokens[0],
                                size = tokens[1].toDoubleOrNull() ?: 0.0,
                                description = tokens[2],
                                quantity = tokens[3].toIntOrNull() ?: 1,
                                amountTendered = tokens[4].toDoubleOrNull() ?: 0.0,
                                customerId = tokens[5].toLongOrNull() ?: 0L
                            )
                        )
                    } else {
                        Log.w("InvoiceCsvParser", "Line $lineNumber has ${tokens.size} tokens, expected 6")
                    }
                } catch (e: Exception) {
                    Log.e("InvoiceCsvParser", "Error parsing line $lineNumber: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.e("InvoiceCsvParser", "Error reading CSV: ${e.message}", e)
        } finally {
            reader.close()
        }

        Log.d("InvoiceCsvParser", "Parsed ${items.size} invoice items")
        return items
    }

}
