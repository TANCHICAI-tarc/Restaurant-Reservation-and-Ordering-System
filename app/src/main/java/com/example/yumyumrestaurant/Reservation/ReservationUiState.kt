package com.example.yumyumrestaurant.Reservation

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.yumyumrestaurant.data.CustomerEntity
import com.example.yumyumrestaurant.data.ReservationData.ReservationEntity
import com.example.yumyumrestaurant.data.TableData.TableEntity
import java.time.LocalDate
import java.time.LocalTime
@RequiresApi(Build.VERSION_CODES.O)
data class ReservationUiState (

    val reservations: List<ReservationEntity> = emptyList(),

    val selectedTables: List<TableEntity> = emptyList(),


    var selectedDate: LocalDate = LocalDate.now(),
    val reservation: ReservationEntity? = null,
    val selectedStartTime: LocalTime = run {
        val suggested = LocalTime.now().plusHours(2)

        val opening = LocalTime.of(9, 0)
        val closing = LocalTime.of(22, 0)
        when {
            suggested.isBefore(opening) -> opening
            suggested.isAfter(closing) -> opening
            else -> suggested
        }
    },

    val selectedDurationMinutes: Int = 15,
    var selectedEndTime: LocalTime?=null,


    var guestCount: Int = 2,
    var showDatePicker: Boolean = false,
    var showStartTimePicker: Boolean = false,
    var showDurationPicker: Boolean = false,


    var zoneExpanded:Boolean=false,

    val reservationId: String = "",
    val specialRequests: String = "",
    val startTimeError: String = "",
    val endTimeError: String = "",

    val isFormValid: Boolean = true,
    val shouldNavigateToNextStep: Boolean = false,
    val selectedZone: String = "INDOOR",

    val customer: CustomerEntity = CustomerEntity(),
    val isReserving:Boolean=false,

)