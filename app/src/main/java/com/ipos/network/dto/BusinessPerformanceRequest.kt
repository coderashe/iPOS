package com.ipos.network.dto

import com.ipos.data.entities.HourPerformance
import com.ipos.data.entities.ProductPerformance

data class BusinessPerformanceRequest(
    val totalExpenses: Double,
    val totalSales: Double,
    val topPerformingProducts: List<ProductPerformance>,
    val peakHours: List<HourPerformance>,
    val salesPerMonth: Map<String, Double>
)
