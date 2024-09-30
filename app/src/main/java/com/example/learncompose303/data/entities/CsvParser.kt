package com.example.learncompose303.data.entities

import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

class CsvParser {

    fun parseProductCsv(inputStream: InputStream): List<ProductCsvModel> {
        val productList = mutableListOf<ProductCsvModel>()
        val reader = BufferedReader(InputStreamReader(inputStream))
        var line: String?

        // Skip the header row
        reader.readLine()

        while (reader.readLine().also { line = it } != null) {
            line?.let {
                val tokens = it.split(",")
                if (tokens.size >= 10) {
                    val productDescription = tokens[0]
                    val currentStockLevel = tokens[1].toDouble()
                    val saleable = tokens[2].toBoolean()
                    val purchasable = tokens[3].toBoolean()
                    val averageCost = tokens[4].toDouble()
                    val standardCost = tokens[5].toDouble()
                    val salesTaxCode = tokens[6].ifBlank { null }
                    val purchasableTaxCode = tokens[7].ifBlank { null }
                    val inclusiveTaxed = tokens[8].toBoolean()
                    val imageResId = tokens[9].toIntOrNull()

                    productList.add(
                        ProductCsvModel(
                            productDescription,
                            currentStockLevel,
                            saleable,
                            purchasable,
                            averageCost,
                            standardCost,
                            salesTaxCode,
                            purchasableTaxCode,
                            inclusiveTaxed,
                            imageResId
                        )
                    )
                }
            }
        }
        reader.close()
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

}