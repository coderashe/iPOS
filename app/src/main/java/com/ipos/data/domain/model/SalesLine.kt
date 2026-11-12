package com.ipos.data.domain.model

data class SalesLine(
    val productName: String,
    val size: String,
    val quantity: Int,
    val price: Double,
    var isValid: Boolean = true,
    var errorMessage: String? = null
)
