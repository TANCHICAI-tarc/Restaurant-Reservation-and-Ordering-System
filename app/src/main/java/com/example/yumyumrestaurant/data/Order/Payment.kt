package com.example.yumyumrestaurant.data.Order

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

data class Payment(
    @get:PropertyName("PaymentID") @set:PropertyName("PaymentID") var paymentID: String = "",
    @get:PropertyName("PaymentMethod") @set:PropertyName("PaymentMethod") var paymentMethod: String = "",
    @get:PropertyName("PaymentDate") @set:PropertyName("PaymentDate") var paymentDate: Timestamp? = null,
    @get:PropertyName("AmountPaid") @set:PropertyName("AmountPaid") var amountPaid: Double = 0.0,
)
