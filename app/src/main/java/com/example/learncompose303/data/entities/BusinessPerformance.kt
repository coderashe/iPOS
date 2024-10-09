package com.example.learncompose303.data.entities

data class BusinessPerformance(
    val totalExpenses: Double,
    val totalSales: Double,
    val topPerformingProducts: List<ProductPerformance>,
    val peakHours: List<HourPerformance>, // Peak hours for sales
    val salesPerMonth: Map<String, Double> // Sales aggregated by month (e.g., Jan, Feb, etc.)
)

data class ProductPerformance(
    val productID: Long,  // Rename to match query
    val productName: String,  // Rename to match query
    val totalSold: Int,
    val totalRevenue: Double
)

data class HourPerformance(
    val hour: Int, // Hour of the day (0-23)
    val totalSales: Double
)

data class MonthlySales(
    val month: String,
    val totalSales: Double
)
