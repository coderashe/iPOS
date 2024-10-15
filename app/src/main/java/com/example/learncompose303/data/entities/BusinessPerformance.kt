package com.example.learncompose303.data.entities

data class BusinessPerformance(
    val totalExpenses: Double,
    val totalSales: Double,
    val topPerformingProducts: List<ProductPerformance>,
    val peakHours: List<HourPerformance>, // Peak hours for sales
    val salesPerMonth: Map<String, Double> // Sales aggregated by month (e.g., Jan, Feb, etc.)
)

data class ProductPerformance(
    val productID: Long,
    val productName: String,
    val totalSold: Int,
    val currentStock: Double,
    val price: Double,  // Add price field
) {
    val totalRevenue: Double
        get() = totalSold * price  // Calculate totalRevenue dynamically
}


data class HourPerformance(
    val hour: Long, // Timestamp in milliseconds
    val totalSales: Double
)

data class MonthlySales(
    val monthNumber: String, // Store month number as a String
    val totalSales: Double
) {
    val monthName: String
        get() = when (monthNumber) {
            "01" -> "January"
            "02" -> "February"
            "03" -> "March"
            "04" -> "April"
            "05" -> "May"
            "06" -> "June"
            "07" -> "July"
            "08" -> "August"
            "09" -> "September"
            "10" -> "October"
            "11" -> "November"
            "12" -> "December"
            else -> "Unknown"
        }
}


//data class MonthlySales(
//    val month: String,
//    val totalSales: Double
//)
