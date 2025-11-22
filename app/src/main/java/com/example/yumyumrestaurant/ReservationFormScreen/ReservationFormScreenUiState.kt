package com.example.yumyumrestaurant.ReservationFormScreen;

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.yumyumrestaurant.data.TableData.ZoneType
import java.time.LocalDate
import java.time.LocalTime
import java.util.Calendar

data class ReservationFormScreenUiState (

    var selectedDate: LocalDate = LocalDate.now(),
    var selectedStartTime: String = "",
    var selectedEndTime: String = "",
    var guestCount: Int = 2,
    var showDatePicker: Boolean = false,
    var showStartTimePicker: Boolean = false,
    var showEndTimePicker: Boolean = false,
    var zoneExpanded:Boolean=false,
    val selectedZone: String = ZoneType.INDOOR.name,
    val specialRequests: String = "",
    val startTimeError: String = "",
    val endTimeError: String = "",

    val isFormValid: Boolean = true,
    val shouldNavigateToNextStep: Boolean = false
    )


