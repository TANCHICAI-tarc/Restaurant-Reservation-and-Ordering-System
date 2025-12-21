package com.example.yumyumrestaurant.data.ReservationData

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.yumyumrestaurant.data.Converters
import com.example.yumyumrestaurant.data.CustomerEntity
import java.time.LocalDate
import java.time.LocalTime

@RequiresApi(Build.VERSION_CODES.O)
@Entity(tableName = "Reservations")
@TypeConverters(Converters::class)
data class ReservationEntity(
    @PrimaryKey
    var reservationId: String = "",
    var date: String = "",
    var startTime: String = "",
    var durationMinutes: Int = 15,
    var endTime: String = "",

    var guestCount: Int = 2,
    var zone: String = "INDOOR",
    var specialRequests: String = "",
    var reservationStatus: String = "PENDING",
    var userId: String = ""
)

enum class ZoneTab(val title: String) {
    INDOOR("Indoor"),
    OUTDOOR("Outdoor")
}

enum class DateRangeFilter {
    TODAY,
    THIS_WEEK,
    ALL
}


