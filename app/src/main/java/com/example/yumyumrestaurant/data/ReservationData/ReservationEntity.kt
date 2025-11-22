package com.example.yumyumrestaurant.data.ReservationData

import java.time.LocalDate
import java.time.LocalTime

data class ReservationData(
    val date: LocalDate,
    val startTime: String,
    val endTime: String,
    val guestCount: Int,
    val zone:String,
    val specialRequests:String

)