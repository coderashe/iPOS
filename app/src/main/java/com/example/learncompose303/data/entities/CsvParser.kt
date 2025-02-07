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
                if (tokens.size >= 12) {
                    val productDescription = tokens[0]
                    val productQty = tokens[1].toLong()
                    val brandID = tokens[2].toLong()
                    val shoeSizeId = tokens[3].toLong()
                    val saleable = tokens[4].toBoolean()
                    val purchasable = tokens[5].toBoolean()
                    val averageCost = tokens[6].toDouble()
                    val standardCost = tokens[7].toDouble()
                    val salesTaxCode = tokens[8].ifBlank { null }
                    val productCode = tokens[9]
                    val inclusiveTaxed = tokens[10].toBoolean()
                    val imageUri = tokens[11]

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
                            imageUri = imageUri
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
