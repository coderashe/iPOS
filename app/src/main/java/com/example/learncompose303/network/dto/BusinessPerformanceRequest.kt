package com.example.learncompose303.network.dto

import com.example.learncompose303.data.entities.HourPerformance
import com.example.learncompose303.data.entities.ProductPerformance

data class BusinessPerformanceRequest(
    val totalExpenses: Double,
    val totalSales: Double,
    val topPerformingProducts: List<ProductPerformance>,
    val peakHours: List<HourPerformance>,
    val salesPerMonth: Map<String, Double>
)
