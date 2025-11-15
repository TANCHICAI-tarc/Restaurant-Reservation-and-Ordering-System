package com.example.yumyumrestaurant.ReservationFormScreen;

import java.time.LocalDate
import java.time.LocalTime
import java.util.Calendar

class ReservationFormScreenUiState (

    val selectedDate: LocalDate = LocalDate.now(),
    val selectedTime: LocalTime = LocalTime.of(11, 0),
    val guestCount: Int = 2,
    val showDatePicker: Boolean = false,
    val showTimePicker: Boolean = false

    )


