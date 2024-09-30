package com.example.learncompose303.data.entities

import android.content.Context
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

data class ProductCsvModel(
    val productDescription: String,
    val currentStockLevel: Double,
    val saleable: Boolean,
    val purchasable: Boolean,
    val averageCost: Double,
    val standardCost: Double,
    val salesTaxCode: String?,
    val purchasableTaxCode: String?,
    val inclusiveTaxed: Boolean,
    val imageResId: Int?
)

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
}
