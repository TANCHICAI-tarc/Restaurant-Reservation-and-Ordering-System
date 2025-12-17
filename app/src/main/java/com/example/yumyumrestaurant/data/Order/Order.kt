package com.example.yumyumrestaurant.data.Order

data class Order (
    val orderID: String = "",
    val totalAmount: Double = 0.0,
    val orderStatus: String = "",
    val paymentID: String = "",
    val reservationID: String = ""
)