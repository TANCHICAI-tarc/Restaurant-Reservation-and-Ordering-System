package com.example.yumyumrestaurant.data.Order

import com.google.firebase.firestore.PropertyName

data class Order (
    @get:PropertyName("OrderID") @set:PropertyName("OrderID") var orderID: String = "",
    @get:PropertyName("TotalAmount") @set:PropertyName("TotalAmount") var totalAmount: Double = 0.0,
    @get:PropertyName("PaymentID") @set:PropertyName("PaymentID") var paymentID: String = "",
    @get:PropertyName("ReservationID") @set:PropertyName("ReservationID") var reservationID: String = ""
)