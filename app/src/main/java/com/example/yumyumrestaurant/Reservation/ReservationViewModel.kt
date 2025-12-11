package com.example.yumyumrestaurant.Reservation


import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.*
import com.example.stayeasehotel.data.regiondata.RegionDatabase
import com.example.yumyumrestaurant.data.CustomerEntity
import com.example.yumyumrestaurant.data.RegionData.RegionRepository
 import com.example.yumyumrestaurant.data.ReservationData.ReservationRepository
import com.example.yumyumrestaurant.data.Reservation_TableDatabase
import com.example.yumyumrestaurant.data.TableData.TableRepository
 import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.LocalDateTime


private val TIME_DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a")
private val DATE_DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("EEE, MMM d ,yyyy")
private const val MAX_STANDARD_DURATION_MINUTES = 120L // 2 hours threshold for fee
private const val MAX_RESERVATION_MINUTES = 300L // New realistic max limit (5 hours)

class ReservationViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReservationViewModel::class.java)) {
            return ReservationViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


@RequiresApi(Build.VERSION_CODES.O)
class ReservationViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(ReservationUiState())
    val uiState: StateFlow<ReservationUiState> = _uiState.asStateFlow()

    private val _validationEvents = MutableSharedFlow<String>()
    val validationEvents: SharedFlow<String> = _validationEvents.asSharedFlow()

    private val defaultCustomer = CustomerEntity(
        customerId = "1",
        fullName = "Tan",
        phone = "0123456789",
        email = "tan@example.com"
    )



    private val reservationRepository : ReservationRepository



    init {

        val reservationDao = Reservation_TableDatabase.getReservationTableDatabase(application).reservationDao()
        reservationRepository = ReservationRepository(reservationDao)

        viewModelScope.launch {
            reservationRepository.syncTablesFromFirebase()
        }


        viewModelScope.launch {
            reservationRepository.allReservations.collect { reservations ->
                _uiState.update { currentState ->
                    currentState.copy(reservations = reservations)
                }
            }

        }


    }

    fun searchTable() {



        _uiState.update { it.copy(isFormValid = true, shouldNavigateToNextStep = true,customer = defaultCustomer) }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun calculateEndDate(
        startDate: LocalDate?,
        startLocalTime: LocalTime?,
        durationMinutes: Int
    ): LocalDate? {
        if (startLocalTime == null || durationMinutes <= 0) {
            return null
        }

        val startDateTime = startDate?.atTime(startLocalTime)

        val endDateTime = startDateTime?.plusMinutes(durationMinutes.toLong())

        return endDateTime?.toLocalDate()
    }



    @RequiresApi(Build.VERSION_CODES.O)
    fun calculateEndTime(startTime: LocalTime?, durationMinutes: Int): LocalTime? {


        if (startTime == null || durationMinutes <= 0) {
            return null
        }


        val duration = durationMinutes.toLong()

        val endTime = startTime.plusMinutes(duration)

        return endTime
    }

    fun updateSelectedZone(zone: String) {
        _uiState.value = _uiState.value.copy(selectedZone = zone)
    }

    fun toggleZone() {
        val newZone = if (uiState.value.selectedZone == "INDOOR") "OUTDOOR" else "INDOOR"
        updateSelectedZone(newZone)
    }




    /**
     * Calculates the overtime fee message based on duration.
     */
//    fun getOvertimeFeeMessage(): String {
//        val state = _uiState.value
//        if (state.totalDurationMinutes < MAX_STANDARD_DURATION_MINUTES || state.overtimeMinutes == 0L) {
//            return ""
//        }
//
//        val feePerHalfHour = 15
//
//        val halfHourBlocks = (state.overtimeMinutes + 29) / 30
//
//        val totalFee = halfHourBlocks * feePerHalfHour
//
//        val durationHours = state.totalDurationMinutes / 60
//        val durationMins = state.totalDurationMinutes % 60
//
//        return "Fee Alert: Your ${durationHours}h ${durationMins}m reservation exceeds the 2-hour standard. A fee of $$totalFee will be charged for the ${state.overtimeMinutes} minutes of overtime (calculated at $$feePerHalfHour per 30 min block)."
//    }

//    fun getAvailableTimeSlots(
//        tableId: String,
//        openingTime: LocalTime = LocalTime.of(10, 0),
//        closingTime: LocalTime = LocalTime.of(22, 0),
//        slotDurationMinutes: Long = 30
//    ): List<LocalTime> {
//
//        val confirmedReservations = reservationRepository
//            .getReservationsForTable(tableId)
//            .filter { it.reservationStatus == "CONFIRMED" }
//
//        val slots = mutableListOf<LocalTime>()
//        var currentTime = openingTime
//
//        while (currentTime.isBefore(closingTime)) {
//            val isTaken = confirmedReservations.any { res ->
//                val resStart = res.startTime
//                val resEnd = res.startTime.plusMinutes(res.durationMinutes.toLong())
//                currentTime in resStart until resEnd
//            }
//
//            if (!isTaken) {
//                slots.add(currentTime)
//            }
//
//            currentTime = currentTime.plusMinutes(slotDurationMinutes)
//        }
//
//        return slots
//    }


    fun localDateToMillis(date: LocalDate): Long {
        return date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    fun resetNavigationFlag() {
        _uiState.update { it.copy(shouldNavigateToNextStep = false) }
    }

    fun setShowDatePicker(showDatePicker: Boolean) {
        _uiState.update { it.copy(showDatePicker = showDatePicker) }
    }


    fun setSelectedDurationMinutes(totalMinutes: Int) {
        val validatedMinutes = totalMinutes.coerceIn(15, 300)

        _uiState.update { currentState ->
            currentState.copy(selectedDurationMinutes = validatedMinutes)
        }

        // Optional: Emit a warning if the user input was outside the bounds
        if (totalMinutes < 15) {
            viewModelScope.launch {
                _validationEvents.emit("Duration cannot be less than 15 minutes.")
            }
        } else if (totalMinutes > MAX_RESERVATION_MINUTES) {
            viewModelScope.launch {
                _validationEvents.emit("Duration cannot exceed 5 hours.")
            }
        }
    }
    fun setShowStartTimePicker(showStartTimePicker: Boolean) {
        _uiState.update { it.copy(showStartTimePicker = showStartTimePicker) }
    }

    fun setShowDurationPicker(showDurationPicker: Boolean) {
        _uiState.update { it.copy(showDurationPicker = showDurationPicker) }
    }





    fun setSelectedStartTime(selectedStartTime: LocalTime) {
        viewModelScope.launch {

            val openingTime = LocalTime.of(10, 0)
            val closingTime = LocalTime.of(22, 0)
            val latestState = _uiState.value // ← read fresh state inside coroutine

//        if selectedStartTime in openingTime..closingTime{
//
//        }
            if ( selectedStartTime <= latestState.selectedEndTime) {
                _uiState.value = latestState.copy(
                    selectedStartTime = selectedStartTime
                )
            } else {
                _validationEvents.emit("Start time cannot be later than end time.")
            }
        }
    }

    fun validateStartTimeSelection(selectedTime: LocalTime): Boolean {

        val state = _uiState.value
        val today = LocalDate.now()

        if (state.selectedDate == today) {
            // Must use LocalDateTime to handle the date rollover correctly
            val requiredAdvanceDateTime = LocalDateTime.now().plusMinutes(120)
            val selectedDateTime = today.atTime(selectedTime)

            if (selectedDateTime.isBefore(LocalDateTime.now())) {
                viewModelScope.launch {
                    _validationEvents.emit("Reservations must be at future.")
                }
                return false
            }

            if (selectedDateTime.isBefore(requiredAdvanceDateTime)) {
                viewModelScope.launch {
                    _validationEvents.emit("Reservations must be made at least 2 hours in advance.")
                }
                return false
            }
        }
        return true
    }


    fun setSelectedDate(selectedDate: LocalDate) {
        _uiState.update { it.copy(selectedDate = selectedDate) }
    }



    fun setZoneExpanded(zoneExpanded:Boolean){
        _uiState.update { it.copy(zoneExpanded = zoneExpanded) }
    }

    fun setSelectedZone(selectedZone:String){
        _uiState.update { it.copy(selectedZone = selectedZone) }
    }

    fun setSpecialRequests(specialRequests:String){
        _uiState.update { it.copy(specialRequests = specialRequests) }
    }


    fun decrementGuestCount(){
        val currentCount = _uiState.value.guestCount
        if (currentCount > 1) {
            _uiState.update { it.copy(guestCount = currentCount - 1) }
        }
    }
    fun incrementGuestCount(){
        val currentCount = _uiState.value.guestCount
        if (currentCount < 10) {
            _uiState.update { it.copy(guestCount = currentCount + 1) }
        }
    }

}