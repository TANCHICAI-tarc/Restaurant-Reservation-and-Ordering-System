package com.example.yumyumrestaurant.shareFilterScreen

import android.app.Application
import android.util.Log
import androidx.compose.material3.DatePickerDefaults.dateFormatter
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.yumyumrestaurant.data.ReservationData.ReservationEntity
import com.example.yumyumrestaurant.data.ReservationData.ReservationRepository
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.collections.remove
import kotlin.text.clear


class SharedFilterViewModel(
    application: Application,

) : AndroidViewModel(application)  {
    private val reservationRepository = ReservationRepository()
    private val _uiState = MutableStateFlow(FilterUiState())
    val uiState: StateFlow<FilterUiState> = _uiState.asStateFlow()

    private val _filteredReservationData = MutableStateFlow<List<ReservationEntity>>(emptyList())
    val filteredReservationData: StateFlow<List<ReservationEntity>> = _filteredReservationData
    private val _errorEvent = MutableSharedFlow<String>()
    val errorEvent: SharedFlow<String> = _errorEvent

    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")





    val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH)

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }


    fun validateTimeRange(): Boolean {
        val state = _uiState.value



        if ((state.startTime == null) != (state.endTime == null)) {
            viewModelScope.launch {
                _errorEvent.emit("Both start time and end time must be set or cleared.")
            }
            return false
        }
        return true
    }


    fun setAllReservationData(reservations: List<ReservationEntity>) {
        _uiState.update { it.copy(allReservationData = reservations) }
        performSearch()
    }

    fun clearAllFilters() {
        _uiState.update { current ->
            current.copy(

                selectedLocations = listOf("All"),
                reservationDate = null,

                startTime = null,
                endTime = null,
                hasFiltered = false
            )
        }
        performSearch()
    }



    fun toggleLocation(location: String, enabled: Boolean) {
        val location = normalizeInput(location)

        _uiState.update { state ->
            val current = state.selectedLocations.toMutableList()
            if (location == "All") {
                if (enabled) {
                    // "All" selected → clear others and keep only "All"
                    current.clear()
                    current.add("All")
                } else {
                    // "All" deselected → just remove it
                    current.remove("All")
                }
            } else {
                current.remove("All") // deselect "All" if any specific category is toggled

                if (enabled) {
                    current.add(location)
                } else {
                    current.remove(location)
                }
            }

            state.copy(selectedLocations = current)
        }

    }


    private fun normalizeInput(input: String): String {
        val normalizedInput = input.trim().lowercase()
        if (normalizedInput.equals("all")) {
            return "All"
        }

        return when {
            normalizedInput.contains("INDOOR") -> "Indoor"
            normalizedInput.contains("OUTDOOR") -> "Outdoor"
            else -> input.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }

    }
    fun setReservationDate(date: LocalDate) {
        _uiState.update { it.copy(reservationDate = date)
        }


    }

    fun setReservationMadeDate(date: LocalDate) {
        _uiState.update { it.copy(reservationMadeDate = date)
        }




    }

    fun setStartTime(time: LocalTime) { _uiState.update { it.copy(startTime = time) } }
    fun setEndTime(time: LocalTime) { _uiState.update { it.copy(endTime = time) } }

    fun toggleFilterDialog(show: Boolean) {
        _uiState.update { it.copy(isFilterDialogVisible = show) }
    }

    fun showClearFiltersConfirmationDialog(show: Boolean) {
        _uiState.update { it.copy(showClearFiltersConfirmation = show) }
    }
    fun setIsLocationExpanded(expanded: Boolean) {
        _uiState.update { it.copy(isLocationExpanded = expanded) }
    }



    fun removeFilter(type: String, value: String) {
        when(type) {

            "Location" -> _uiState.update { currentState ->
                currentState.copy(
                    selectedLocations = currentState.selectedLocations - value
                )
            }

            "Reservation Date" -> _uiState.update { currentState ->
                currentState.copy(
                    reservationDate = null
                )
            }
            "Reservation Made Date" -> _uiState.update { currentState ->
                currentState.copy(
                    reservationMadeDate = null
                )
            }

            "Start Time" -> _uiState.update { currentState ->
                currentState.copy(
                    startTime = null
                )
            }
            "End Time" -> _uiState.update { currentState ->
                currentState.copy(
                    endTime = null
                )
            }
            else -> { }
        }
        performSearch()
    }


    @OptIn(ExperimentalMaterial3Api::class)
    fun performSearch() {
        val state = _uiState.value
        val trimmedQuery = state.searchQuery.trim()


        val locations = if (state.selectedLocations.contains("All")) emptyList()
        else state.selectedLocations


        val filteredList = state.allReservationData.filter { reservation ->

            val matchesLocation = locations.isEmpty() || reservation.zone in locations


            val matchesQuery = trimmedQuery.isBlank() ||
                    reservation.reservationId.contains(trimmedQuery, ignoreCase = true) ||
                    reservation.reservationStatus.contains(trimmedQuery, ignoreCase = true)||
                    reservation.specialRequests.contains(trimmedQuery, ignoreCase = true)

//                    reservations


            val matchesReservationMadeDate = if (state.reservationMadeDate == null) {
                true
            } else {
                val dbDate = try {

                    LocalDate.parse(reservation.reservationMadeDate)
                } catch (e: Exception) {
                    null
                }
                dbDate == state.reservationMadeDate
            }

            val matchesReservationDate = if (state.reservationDate == null) {
                true
            } else {
                val dbDate = try {

                    LocalDate.parse(reservation.date)
                } catch (e: Exception) {
                    null
                }
                dbDate == state.reservationDate
            }

            val reservationTime = try {
                LocalTime.parse(reservation.startTime)
            } catch (e: Exception) {
                null
            }

            val matchesTime = if (reservationTime == null) true else {
                when {
                    state.startTime != null && state.endTime != null -> {
                        !reservationTime.isBefore(state.startTime) && !reservationTime.isAfter(state.endTime)
                    }
                    state.startTime != null -> !reservationTime.isBefore(state.startTime)
                    state.endTime != null -> !reservationTime.isAfter(state.endTime)
                    else -> true
                }
            }


            matchesLocation && matchesQuery && matchesReservationDate&&matchesReservationMadeDate && matchesTime
        }


        _filteredReservationData.value = filteredList
        _uiState.update { it.copy(
            searchQuery = trimmedQuery,
            filteredReservationData = filteredList,
            hasFiltered = true
        ) }
    }









}
