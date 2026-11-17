package com.ipos.data

sealed class Destinations(val route: String) {

    data object Reports : Destinations("reports")
    data object Faqs : Destinations("faqs")
    data object Currency : Destinations("currency")
    data object BusinessPerformance : Destinations("businessPerformance")
    data object Expense : Destinations("expense")
    data object ExpenseReport : Destinations("expenseReport")
    data object StockTaking : Destinations("stockTaking")
    data object StockTakeReport : Destinations("stockTakeReport")
    data object StockTakeDetail : Destinations("stockTakeDetail/{sessionId}") {
        fun createRoute(sessionId: Long) = "stockTakeDetail/$sessionId"
    }
    data object SalesReport : Destinations("salesReport")
    data object ShoeManagement : Destinations("shoeManagement")
    data object OcrUpload : Destinations("ocrUpload")
    data object FailedInvoices : Destinations("failedInvoices")
    data object InvoiceDetail : Destinations("invoice_detail/{invoiceId}") {
        fun createRoute(invoiceId: Long) = "invoice_detail/$invoiceId"
    }

    data object Dashboard : Destinations("dashboard")
    data object SignIn : Destinations("signIn")
    data object Products : Destinations("products")
    data object SignUp : Destinations("signup")
    data object IncomeStatement : Destinations("incomestatement")
    data object Invoice : Destinations("invoicing")
    data object MultiCurrency : Destinations("multicurrency")
    data object Order : Destinations("order")
    data object OrderReports : Destinations("orderReports")
    data object OrderReportDetail : Destinations("order_report/{id}") {
        fun createRoute(id: Long) = "order_report/$id"
    }

    data object LoadStocks : Destinations("loadStocks")
    data object Cart : Destinations("cartScreen")
    data object ButcherCart : Destinations("butcherCartScreen")
    data object CustomerSelection : Destinations("customerSelection")
    data object ProductSelection : Destinations("productSelection")
    data object Income : Destinations("My income")
    data object Priority : Destinations("Priority")
    data object Wallet : Destinations("Wallet")
    data object SyncStatus : Destinations("syncStatus")
    data object DeviceRegistration : Destinations("deviceRegistration")
}
