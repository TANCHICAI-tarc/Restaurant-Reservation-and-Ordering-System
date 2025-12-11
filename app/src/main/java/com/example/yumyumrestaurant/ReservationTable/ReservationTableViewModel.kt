package com.example.yumyumrestaurant.ReservationTable

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.yumyumrestaurant.Reservation.ReservationViewModel
import com.example.yumyumrestaurant.TableSelectionScreen.TableUiState
import com.example.yumyumrestaurant.TableSelectionScreen.TableViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime



// In ReservationTableViewModel.kt
class ReservationTableViewModelFactory(
    private val tableViewModel: TableViewModel,
    private val reservationViewModel: ReservationViewModel
) : ViewModelProvider.Factory { // <-- Keep this simple for now, but inspect the create function

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReservationTableViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ReservationTableViewModel(tableViewModel, reservationViewModel) as T
        }
        // CRITICAL: If you don't recognize the class, throw the standard error
        throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
    }
}

class ReservationTableViewModel(
    private val tableVM: TableViewModel,
    private val reservationVM: ReservationViewModel
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReservationTableUiState())
    val reservationTableUiState: StateFlow<ReservationTableUiState> = _uiState
    val reservationViewModel get() = reservationVM
    val tableViewModel get() = tableVM

    val reservationUiState = reservationVM.uiState
    val tableUiState = tableViewModel.uiState


    fun refreshTableStatuses(
        date: LocalDate,
        startTime: LocalTime,
        durationMinutes: Int
    ) {
        val start = LocalDateTime.of(date, startTime)
        val end = start.plusMinutes(durationMinutes.toLong())

        val updatedTables = _uiState.value.tables.map { table ->
            // 1. Get links for this table
            val reservationLinksForTable = _uiState.value.reservationsTables.filter { it.tableId == table.tableId }

            // 2. Get actual ReservationEntity objects
            val reservationsForTable = reservationLinksForTable.mapNotNull { link ->
                _uiState.value.reservations.find { it.reservationId == link.reservationId }
            }


            val isReserved = reservationsForTable.any { r ->
                val rStart = LocalDateTime.of(r.date, r.startTime)
                val rEnd = rStart.plusMinutes(r.durationMinutes.toLong())
                start < rEnd && end > rStart
            }

            table.copy(status = if (isReserved) "RESERVED" else "AVAILABLE")
        }

        _uiState.update { it.copy(tables = updatedTables) }
    }


}
