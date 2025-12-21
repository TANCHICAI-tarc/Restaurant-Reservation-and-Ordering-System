package com.example.yumyumrestaurant.shareFilterScreen

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.yumyumrestaurant.data.ReservationData.ReservationEntity
import com.example.yumyumrestaurant.data.ReservationData.ReservationRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale



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
    }



    fun toggleLocation(location: String, enabled: Boolean) {
        _uiState.update { state ->
            val current = state.selectedLocations.toMutableList()
            if (location == "All") {
                if (enabled) current.clear().also { current.add("All") } else current.remove("All")
            } else {
                current.remove("All")
                if (enabled) current.add(location) else current.remove(location)
            }
            state.copy(selectedLocations = current)
        }
    }

    fun setReservationDate(date: LocalDate) { _uiState.update { it.copy(reservationDate = date) } }

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



//    fun performSearch() {
//
//        val trimmedQuery = _uiState.value.searchQuery.trim()
//        _uiState.update { it.copy(searchQuery = trimmedQuery) }
//
//        val state = _uiState.value
//
//        _filteredReservationData.value = state.allReservationData
//        val selectedLocation = state.selectedLocations
//
//
//
//        _filteredReservationData.value = state.allReservationData.filter { item ->
//
//
//            val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
//            val itemDate = try {
//                LocalDate.parse(item.dateFound, dateFormatter)
//            } catch (e: Exception) {
//                LocalDate.MIN
//            }
//
//            val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH)
//
//
//            val normalizedTimeString = item.timeFound.trim().replace("am", "AM").replace("pm", "PM")
//
//            val itemTime = LocalTime.parse(normalizedTimeString, timeFormatter)
//
//
//
//
//            val matchesCategory = "All" in selectedCategory || selectedCategory.isEmpty() || item.category in selectedCategory
//            val matchesLocation = "All" in selectedLocation || selectedLocation.isEmpty() || item.foundLocation in selectedLocation
//            val matchesSubmitter = "All" in selectedSubmitters || selectedSubmitters.isEmpty() || item.reporter.type in selectedSubmitters
//            val matchesStatus = "All" in selectedStatuses || selectedStatuses.isEmpty() || item.status in selectedStatuses
//
//            val matchesSearch = trimmedQuery.isBlank() ||
//
//                    item.id.contains(trimmedQuery, ignoreCase = true) ||
//                    item.itemTitle.contains(trimmedQuery, ignoreCase = true) ||
//                    item.description?.contains(trimmedQuery, ignoreCase = true) == true
//
//
//
//            val matchesDate = (state.startDate == null || !itemDate.isBefore(state.startDate)) &&
//                    (state.endDate == null || !itemDate.isAfter(state.endDate))
//
//            val matchesTime = (state.startTime == null || !itemTime.isBefore(state.startTime)) &&
//                    (state.endTime == null || !itemTime.isAfter(state.endTime))
//
//            matchesCategory &&
//                    matchesLocation &&
//                    matchesSubmitter &&
//                    matchesStatus &&
//                    matchesSearch &&
//                    matchesDate &&
//                    matchesTime
//
//
//        }
//
//
//        _filteredClaimItems.value = state.allClaimItems.filter { claim ->
//
//
//            val claimDateTime = LocalDateTime.ofInstant(
//                Instant.ofEpochMilli(claim.claimTime),
//                ZoneId.systemDefault()
//            )
//
//            val claimDate = claimDateTime.toLocalDate()
//            val claimTime = claimDateTime.toLocalTime()
//
//            val matchesDate = (state.startDate == null || !claimDate.isBefore(state.startDate)) &&
//                    (state.endDate == null || !claimDate.isAfter(state.endDate))
//
//            val matchesTime = (state.startTime == null || !claimTime.isBefore(state.startTime)) &&
//                    (state.endTime == null || !claimTime.isAfter(state.endTime))
//
//
//
//
//            val matchesCategory = "All" in selectedCategory || selectedCategory.isEmpty() ||
//                    (claim.item?.category in selectedCategory)
//
//            val matchesLocation = "All" in selectedLocation || selectedLocation.isEmpty() ||
//                    (claim.item?.foundLocation in selectedLocation)
//
//            val matchesStatus = "All" in selectedStatuses || selectedStatuses.isEmpty() ||
//                    (claim.claimStatus in selectedStatuses)
//
//
//
//            val matchesSearch = trimmedQuery.isBlank() ||
//                    claim.claimId.contains(trimmedQuery, ignoreCase = true) ||
//                    (claim.item?.id?.contains(trimmedQuery, ignoreCase = true) == true) ||
//                    (claim.item?.itemTitle?.contains(trimmedQuery, ignoreCase = true) == true) ||
//                    (claim.item?.description?.contains(trimmedQuery, ignoreCase = true) == true) ||
//                    (claim.claimDescription.contains(trimmedQuery, ignoreCase = true))
//
//
//
//            matchesCategory &&
//                    matchesLocation &&
//                    matchesStatus &&
//
//                    matchesSearch&&
//                    matchesDate&&
//                    matchesTime
//
//        }
//
//
//        _uiState.update { it.copy(hasFiltered = true) }
//    }


    fun removeFilter(type: String, value: String) {
        when(type) {

            "Location" -> _uiState.update { currentState ->
                currentState.copy(
                    selectedLocations = currentState.selectedLocations - value
                )
            }

            "ReservationDate Date" -> _uiState.update { currentState ->
                currentState.copy(
                    reservationDate = null
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
            else -> { /* no-op or log unknown type */}
        }
        performSearch()
    }

    fun setReservationData(reservations: List<ReservationEntity>) {
        _uiState.update { it.copy(allReservationData = reservations) }
        performSearch()
    }

    fun performSearch() {

        val state = _uiState.value
        val trimmedQuery = state.searchQuery.trim()


        _uiState.update { it.copy(searchQuery = trimmedQuery) }




        _filteredReservationData.value = state.allReservationData




        _filteredReservationData.value = state.allReservationData
//            .filter { item ->


        val locations =
            if (state.selectedLocations.contains("All")) emptyList()
            else state.selectedLocations
        Log.d("FilterDebug", "All reservations: ${state.allReservationData}")
        Log.d("FilterDebug", "Selected locations: $locations")
        val filteredList = state.allReservationData.filter { reservation ->
            val matchesLocation = locations.isEmpty() || reservation.zone in locations
//            val matchesQuery = trimmedQuery.isBlank() || reservation.title.contains(trimmedQuery, true) || item.id.contains(trimmedQuery, true)
//            val matchesDate = state.reservationDate == null || reservation.date == state.reservationDate
//            val matchesTime = (state.startTime == null || !reservation.reservationTime.isBefore(state.startTime)) &&
//                    (state.endTime == null || !reservation.startTime.isAfter(state.endTime))

            matchesLocation
//                    &&
//                    matchesQuery &&
//                    matchesDate
//                    matchesTime
        }

        _uiState.update { it.copy(
            filteredReservationData = filteredList,
            hasFiltered = true
        ) }
    }
    fun cancelReservation(reservation: ReservationEntity) {

        val updatedReservations = _uiState.value.allReservationData.map {
            if (it.reservationId == reservation.reservationId) {
                it.copy(reservationStatus = "Cancelled")
            } else it
        }
        _uiState.value = _uiState.value.copy(allReservationData = updatedReservations)


        viewModelScope.launch {
            try {
                reservationRepository.cancelReservation(reservation.reservationId)
            } catch (e: Exception) {
                _errorEvent.emit("Failed to cancel reservation: ${e.message}")
            }
        }


        performSearch()
    }




    private fun combineDateTime(
        date: LocalDate?,
        time: LocalTime?
    ): LocalDateTime? {
        return if (date != null && time != null) {
            LocalDateTime.of(date, time)
        } else null
    }


}
