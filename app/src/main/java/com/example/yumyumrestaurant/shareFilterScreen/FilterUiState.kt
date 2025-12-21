package com.example.yumyumrestaurant.shareFilterScreen

import androidx.lifecycle.ViewModel

import com.example.yumyumrestaurant.data.ReservationData.DateRangeFilter
import com.example.yumyumrestaurant.data.ReservationData.ReservationEntity
import com.example.yumyumrestaurant.data.ReservationData.ZoneTab
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.LocalTime

data class FilterUiState(
    val reservationIdQuery: String = "",

    // Indoor / Outdoor
    val selectedZone: ZoneTab? = null,

    // Today | This Week | All

    val selectedDateRange: DateRangeFilter = DateRangeFilter.ALL,

    val reservationDate: LocalDate? = null,

    val startTime: LocalTime? = null,
    val endTime: LocalTime? = null,

    val isZoneExpanded: Boolean = false,
    val isDateRangeExpanded: Boolean = false,

    val isFilterDialogVisible: Boolean = false,
    val hasFiltered: Boolean = false,
    val searchQuery: String = "",
    val showClearFiltersConfirmation:Boolean=false,


    val appliedFilters: Map<String, List<String>> = emptyMap(),

    val showDateTime: Boolean = true,

    val showLocation: Boolean = true,


    val isCategoryExpanded: Boolean = false,
    val isLocationExpanded: Boolean = false,


    val selectedLocations: List<String> = emptyList(),
    val allReservationData: List<ReservationEntity> = emptyList(),
    val filteredReservationData: List<ReservationEntity> = emptyList()



)
