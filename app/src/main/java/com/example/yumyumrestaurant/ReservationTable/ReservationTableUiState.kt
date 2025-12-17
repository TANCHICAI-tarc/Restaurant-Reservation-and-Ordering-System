package com.example.yumyumrestaurant.ReservationTable

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.yumyumrestaurant.data.ReservationData.ReservationEntity
import com.example.yumyumrestaurant.data.Reservation_Table_Entity
import com.example.yumyumrestaurant.data.TableData.TableEntity
import java.time.LocalDate
import java.time.LocalTime

@RequiresApi(Build.VERSION_CODES.O)
data class ReservationTableUiState(
    val tables: List<TableEntity> = emptyList(),
    val reservations: List<ReservationEntity> = emptyList(),
    val reservationsTables: List<Reservation_Table_Entity> = emptyList(),
    val selectedDate: LocalDate = LocalDate.now(),
    val selectedTime: LocalTime = LocalTime.now(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
