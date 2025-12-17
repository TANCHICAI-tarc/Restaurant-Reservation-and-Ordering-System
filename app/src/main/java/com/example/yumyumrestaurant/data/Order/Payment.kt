package com.example.yumyumrestaurant.data.Order

import com.google.firebase.Timestamp

data class Payment(
    val paymentID: String = "",
    val paymentMethod: String = "",
    val paymentDate: Timestamp? = null,
    val amountPaid: Double = 0.0,
    val paymentStatus: String = ""
)
