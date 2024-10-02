package com.example.learncompose303.data

sealed class Destinations(val route: String) {
    //    data object Rides : Destinations("Ride requests")
    data object Rides : Destinations("rides")
    data object SignIn : Destinations("signIn")
    data object SignUp : Destinations("signup")
    data object Invoice : Destinations("invoicing")
    data object Login : Destinations("login")
    data object Order : Destinations("order")
    data object OrderReports : Destinations("orderReports")
    data object OrderReportDetail : Destinations("order_report/{id}") {
        fun createRoute(id: Long) = "order_report/$id"
    }

    data object LoadStocks : Destinations("loadStocks")
    data object Cart : Destinations("cartScreen")
    data object CustomerSelection : Destinations("customerSelection")
    data object ProductSelection : Destinations("productSelection")
    data object Income : Destinations("My income")
    data object Priority : Destinations("Priority")
    data object Wallet : Destinations("Wallet")
    data object Home : Destinations("home")
    data object Search : Destinations("search")
    data object Notifications : Destinations("updates")
    data object Profile : Destinations("profile")
    data object Post : Destinations("post")
    data object TTTScreen : Destinations("game")
    data object Reports : Destinations("reports")
    data object InvoiceDetail : Destinations("invoice_detail/{invoiceId}") {
        fun createRoute(invoiceId: Long) = "invoice_detail/$invoiceId"
    }

    data object Detail : Destinations("detail/{itemId}") {
        fun createRoute(itemId: Int) = "detail/$itemId"
    }
}
