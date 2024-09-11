package com.example.learncompose303.data.domain.util

sealed class OrderType {
    object Ascending : OrderType()
    object Descending : OrderType()
}
