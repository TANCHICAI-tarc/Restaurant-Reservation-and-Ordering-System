package com.example.yumyumrestaurant.data.ReservationData

import java.time.LocalDate
import java.time.LocalTime

data class ReservationData(
    val date: LocalDate,
    val time: LocalTime,
    val guests: Int
)