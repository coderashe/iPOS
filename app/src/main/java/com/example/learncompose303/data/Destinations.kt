package com.example.learncompose303.data

sealed class Destinations(val route: String) {
    //    data object Rides : Destinations("Ride requests")
    data object Rides : Destinations("rides")
    data object Income : Destinations("My income")
    data object Priority : Destinations("Priority")
    data object Wallet : Destinations("Wallet")
    data object Home : Destinations("home")
    data object Search : Destinations("search")
    data object Notifications : Destinations("updates")
    data object Profile : Destinations("profile")
    data object Post : Destinations("post")
    data object TTTScreen : Destinations("game")
    data object Detail : Destinations("detail/{itemId}") {
        fun createRoute(itemId: Int) = "detail/$itemId"
    }
}