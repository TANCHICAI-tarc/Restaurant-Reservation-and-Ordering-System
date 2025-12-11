package com.example.yumyumrestaurant.data.ReservationData

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.yumyumrestaurant.data.Converters
import com.example.yumyumrestaurant.data.CustomerEntity
import java.time.LocalDate
import java.time.LocalTime


@Entity(tableName = "Reservations")
@TypeConverters(Converters::class)
data class ReservationEntity(
    @PrimaryKey val reservationId: String = "",

    val date: LocalDate = LocalDate.now(),
    val startTime: LocalTime = LocalTime.now().plusHours(2),
    val durationMinutes: Int = 15,
    val endTime: LocalTime = LocalTime.now().plusHours(durationMinutes.toLong()),

    val guestCount: Int = 2,
    val zone: String = "INDOOR",
    val specialRequests: String = "",
    val reservationStatus: String = "PENDING",
    val customerId: String
)
