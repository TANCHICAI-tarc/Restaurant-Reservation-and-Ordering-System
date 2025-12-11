package com.example.yumyumrestaurant.Reservation

import com.example.yumyumrestaurant.data.CustomerEntity
import com.example.yumyumrestaurant.data.ReservationData.ReservationEntity
import com.example.yumyumrestaurant.data.TableData.TableEntity
import java.time.LocalDate
import java.time.LocalTime

data class ReservationUiState (

    val reservations: List<ReservationEntity> = emptyList(),
    
    val selectedTables: List<TableEntity> = emptyList(),


    var selectedDate: LocalDate = LocalDate.now(),

    var selectedStartTime: LocalTime = LocalTime.now()
        .plusHours(2),

    val selectedDurationMinutes: Int = 15,
    var selectedEndTime: LocalTime = selectedStartTime.plusMinutes(selectedDurationMinutes.toLong()),


    var guestCount: Int = 2,
    var showDatePicker: Boolean = false,
    var showStartTimePicker: Boolean = false,
    var showDurationPicker: Boolean = false,


    var zoneExpanded:Boolean=false,

    val specialRequests: String = "",
    val startTimeError: String = "",
    val endTimeError: String = "",

    val isFormValid: Boolean = true,
    val shouldNavigateToNextStep: Boolean = false,
    val selectedZone: String = "INDOOR",

    val customer: CustomerEntity = CustomerEntity(),



    )