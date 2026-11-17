package com.ipos.utils

import com.ipos.data.domain.model.SalesLine

class SalesLineParser {
    
    fun parseOcrText(text: String): List<SalesLine> {
        val lines = text.lines().filter { it.isNotBlank() }
        val salesLines = mutableListOf<SalesLine>()
        
        for (line in lines) {
            val parsed = parseLine(line)
            if (parsed != null) {
                salesLines.add(parsed)
            }
        }
        
        return salesLines
    }
    
    private fun parseLine(line: String): SalesLine? {
        // Split by common delimiters: |, tab, multiple spaces
        val parts = line.split(Regex("[|\\t\\s]{2,}")).map { it.trim() }.filter { it.isNotEmpty() }
        
        if (parts.size < 4) return null
        
        return try {
            val productName = parts[0]
            val size = parts[1]
            val quantity = parseQuantity(parts[2])
            val price = parsePrice(parts[3])
            
            SalesLine(
                productName = productName,
                size = size,
                quantity = quantity,
                price = price,
                isValid = true
            )
        } catch (e: Exception) {
            SalesLine(
                productName = parts.getOrNull(0) ?: "",
                size = parts.getOrNull(1) ?: "",
                quantity = 0,
                price = 0.0,
                isValid = false,
                errorMessage = "Parse error: ${e.message}"
            )
        }
    }
    
    private fun parseQuantity(text: String): Int {
        return text.replace(Regex("[^0-9]"), "").toIntOrNull() ?: 0
    }
    
    private fun parsePrice(text: String): Double {
        val cleaned = text.replace(Regex("[^0-9.]"), "")
        return cleaned.toDoubleOrNull() ?: 0.0
    }
}
