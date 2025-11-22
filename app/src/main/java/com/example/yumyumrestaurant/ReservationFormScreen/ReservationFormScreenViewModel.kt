package com.example.yumyumrestaurant.ReservationFormScreen


import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.*


import kotlinx.coroutines.launch

import android.net.Uri
import android.util.Log

import androidx.core.content.FileProvider
import com.example.yumyumrestaurant.data.ReservationData.ReservationData
import com.example.yumyumrestaurant.data.ReservationFormScreen
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


class ReservationFormScreenViewModel(application: Application) : AndroidViewModel(application) {



    private val _uiState = MutableStateFlow(ReservationFormScreenUiState())
    val uiState: StateFlow<ReservationFormScreenUiState> = _uiState.asStateFlow()


    private val _navigateToNextScreen = MutableSharedFlow<ReservationData>()
    val navigateToNextScreen: SharedFlow<ReservationData> = _navigateToNextScreen.asSharedFlow()

    // 2. Add the Reserve/Submit function
    fun onReserveClicked() {
        viewModelScope.launch {
            val currentState = _uiState.value

            if (!validateReservationData()) {
                // If validation fails, update the error state
                _uiState.update { it.copy(isFormValid = false) }

            }

            // 3. If valid, set the flag to signal the Composable to navigate
            _uiState.update { it.copy(isFormValid = true, shouldNavigateToNextStep = true) }


        }

    }

    private fun validateReservationData(): Boolean {
        val state = _uiState.value
        var isValid = true

        // --- 3. Check Start Time (Cannot be empty) ---
        if (state.selectedStartTime.isBlank()) {
            _uiState.update { it.copy(startTimeError = "Start time is required.") }
            isValid = false
        }

        // --- 4. Check End Time (Cannot be empty) ---
        if (state.selectedEndTime.isBlank()) {
            _uiState.update { it.copy(endTimeError = "End time is required.") }
            isValid = false
        }

        if (isValid) {
            val startTime = parseTime(state.selectedStartTime)
            val endTime = parseTime(state.selectedEndTime)

            if (startTime == null || endTime == null) {
                // This case handles parsing issues if the string format is unexpected,
                // but the blank checks above prevent most simple empty string issues.
                _uiState.update { it.copy(startTimeError = "Invalid time format.") }
                isValid = false
            } else if (endTime.timeInMillis <= startTime.timeInMillis) {
                _uiState.update { it.copy(endTimeError = "End time must be after start time.") }
                isValid = false
            }
        }

        _uiState.update { it.copy(isFormValid = isValid) }

        return isValid
    }

    private fun parseTime(timeStr: String): Calendar? {
        return try {
            val format = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val date = format.parse(timeStr)
            val calendar = Calendar.getInstance()
            if (date != null) {
                calendar.time = date
            }
            calendar
        } catch (e: Exception) {
            null
        }
    }

    fun resetNavigationFlag() {
        _uiState.update { it.copy(shouldNavigateToNextStep = false) }
    }




    fun setShowStartTimePicker(showStartTimePicker: Boolean) {

        _uiState.value = _uiState.value.copy(showStartTimePicker = showStartTimePicker)
    }

    fun setShowEndTimePicker(showEndTimePicker: Boolean) {

        _uiState.value = _uiState.value.copy(showEndTimePicker = showEndTimePicker)
    }

    fun setShowDatePicker(showDatePicker:Boolean){
        _uiState.value = _uiState.value.copy(showDatePicker = showDatePicker)


    }

    fun setZoneExpanded(zoneExpanded:Boolean){
        _uiState.value = _uiState.value.copy(zoneExpanded = zoneExpanded)


    }
    fun setSelectedZone(selectedZone:String){
        _uiState.value = _uiState.value.copy(selectedZone = selectedZone)


    }

    fun setSpecialRequests(specialRequests:String){
        _uiState.value = _uiState.value.copy(specialRequests = specialRequests)


    }


    fun decrementGuestCount(){
        val currentCount = _uiState.value.guestCount

        if (currentCount > 1) {
            _uiState.value = _uiState.value.copy(guestCount = currentCount - 1)
        }

    }
    fun incrementGuestCount(){
        val currentCount = _uiState.value.guestCount



        if (currentCount < 10) {
            _uiState.value = _uiState.value.copy(guestCount = currentCount + 1)
        }

    }

    fun setSelectedStartTime(selectedStartTime: String) {
        _uiState.value = _uiState.value.copy(selectedStartTime = selectedStartTime)
    }

    fun setSelectedEndTime(selectedEndTime: String) {
        _uiState.value = _uiState.value.copy(selectedEndTime = selectedEndTime)
    }







}








