package com.example.yumyumrestaurant.Reservation


import android.app.Application
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.*
import com.example.stayeasehotel.data.regiondata.RegionDatabase
import com.example.yumyumrestaurant.data.CustomerEntity
import com.example.yumyumrestaurant.data.RegionData.RegionRepository
import com.example.yumyumrestaurant.data.ReservationData.ReservationEntity
import com.example.yumyumrestaurant.data.ReservationData.ReservationRepository
import com.example.yumyumrestaurant.data.Reservation_TableDatabase
import com.example.yumyumrestaurant.data.TableData.TableEntity
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

    private val openingTime = LocalTime.of(9, 0)
    private val closingTime = LocalTime.of(22, 0)
    private val minDuration = 15
    private val maxDuration = 300
    private val minAdvanceMinutes = 120L

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
    fun calculateEndTime(startTime: LocalTime , durationMinutes: Int): LocalTime  {




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

    suspend fun getAvailableTimeSlots(
        date: LocalDate,
        tableId: String,
        requiredDurationMinutes: Int
    ): List<LocalTime> {

        val confirmedReservations = reservationRepository
            .getConfirmedReservationsForTableOnDate(tableId, date)

        val availableSlots = mutableListOf<LocalTime>()
        var currentTime = openingTime

        val minAdvanceDateTime = if (date == LocalDate.now()) {
            LocalDateTime.now().plusMinutes(minAdvanceMinutes)
        } else {
            date.atTime(LocalTime.MIN)
        }
        Log.d("TimeSlotDebug", "CurrentTime: $currentTime, minAdvanceDateTime: $minAdvanceDateTime")

        while (currentTime.isBefore(closingTime)) {

            val slotStartDateTime = date.atTime(currentTime)
            val slotEndDateTime = slotStartDateTime.plusMinutes(requiredDurationMinutes.toLong())

            if (slotStartDateTime.isBefore(minAdvanceDateTime)) {
                currentTime = currentTime.plusMinutes(15)
                continue
            }

            if (slotEndDateTime.toLocalTime().isAfter(closingTime)) {
                break
            }

            val isTaken = confirmedReservations.any { res ->
                val formatter = DateTimeFormatter.ofPattern("HH:mm") // or "hh:mm a" depending on source
                val resStartTime = LocalTime.parse(res.startTime, formatter)


                val resStartDateTime = date.atTime(resStartTime)
                val resEndDateTime = resStartDateTime.plusMinutes(res.durationMinutes.toLong())

                val overlap = slotStartDateTime.isBefore(resEndDateTime) && resStartDateTime.isBefore(slotEndDateTime)

                overlap
            }

            if (!isTaken) {
                availableSlots.add(currentTime)
            }

            currentTime = currentTime.plusMinutes(15)
        }

        return availableSlots
    }

    fun localDateToMillis(date: LocalDate): Long {
        return date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    fun resetNavigationFlag() {
        _uiState.update { it.copy(shouldNavigateToNextStep = false) }
    }

    fun setShowDatePicker(showDatePicker: Boolean) {
        _uiState.update { it.copy(showDatePicker = showDatePicker) }
    }


    fun setShowStartTimePicker(showStartTimePicker: Boolean) {
        _uiState.update { it.copy(showStartTimePicker = showStartTimePicker) }
    }

    fun setShowDurationPicker(showDurationPicker: Boolean) {
        _uiState.update { it.copy(showDurationPicker = showDurationPicker) }
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






    private suspend fun validateReservation(
        date: LocalDate,
        time: LocalTime,
        durationMinutes: Int
    ): Boolean {


        val startDateTime = LocalDateTime.of(date, time)
        val endDateTime = startDateTime.plusMinutes(durationMinutes.toLong())
        val openingDateTime = date.atTime(openingTime)
        val closingDateTime = date.atTime(closingTime)



        if (startDateTime.isBefore(openingDateTime) || endDateTime.isAfter(closingDateTime)) {
            _validationEvents.emit("Please select a time between ${openingTime.format(TIME_DISPLAY_FORMATTER)} and ${closingTime.format(TIME_DISPLAY_FORMATTER)}")
            return false
        }

        if (date.isBefore(LocalDate.now()) || (date == LocalDate.now() && time.isBefore(LocalTime.now()))) {
            _validationEvents.emit("Reservations must be in the future.")
            return false
        }


        if (date == LocalDate.now()) {
            val minAdvanceTime = LocalDateTime.now().plusMinutes(minAdvanceMinutes)
            if (startDateTime.isBefore(minAdvanceTime)) {
                _validationEvents.emit("Reservations must be made at least 2 hours in advance.")
                return false
            }
        }

        return true
    }

    private fun updateReservation(
        date: LocalDate? = _uiState.value.selectedDate,
        time: LocalTime? = _uiState.value.selectedStartTime,
        duration: Int = _uiState.value.selectedDurationMinutes
    ) {
        viewModelScope.launch {
            if (date == null || time == null) return@launch


            if (!validateReservation(date, time, duration)) return@launch




            _uiState.update {
                it.copy(
                    selectedDate = date,
                    selectedStartTime = time,
                    selectedDurationMinutes = duration
                )
            }
        }
    }

    fun setSelectedDate(date: LocalDate) {

        if(date==LocalDate.now()){
            val currentTime = _uiState.value.selectedStartTime ?: openingTime
            val duration = _uiState.value.selectedDurationMinutes
            updateReservation(date = date, time = currentTime, duration = duration)
        }else{
            _uiState.update { it.copy(selectedDate = date) }

        }

    }


    fun setSelectedStartTime(time: LocalTime) {
        val date = _uiState.value.selectedDate ?: LocalDate.now()
        val duration = _uiState.value.selectedDurationMinutes
        updateReservation(date = date, time = time, duration = duration)
    }


    fun setSelectedDurationMinutes(minutes: Int) {
        val validatedMinutes = minutes.coerceIn(minDuration, maxDuration)
        val date = _uiState.value.selectedDate ?: LocalDate.now()
        val time = _uiState.value.selectedStartTime ?: openingTime

        if (minutes < minDuration) {
            viewModelScope.launch { _validationEvents.emit("Duration cannot be less than $minDuration minutes.") }
        } else if (minutes > maxDuration) {
            viewModelScope.launch { _validationEvents.emit("Duration cannot exceed $maxDuration minutes.") }
        }

        updateReservation(date = date, time = time, duration = validatedMinutes)
    }

    fun setGuestCount(count: Int) {
        _uiState.update { it.copy(guestCount = count) }
    }




    fun saveReservation(onResult: (String) -> Unit) {
        val state = _uiState.value

        viewModelScope.launch {
            val reservationId = reservationRepository.generateReservationID()

            val reservation = ReservationEntity(
                reservationId = reservationId,
                date = state.selectedDate.toString(),
                startTime = state.selectedStartTime.toString(),
                durationMinutes = state.selectedDurationMinutes,
                endTime = state.selectedEndTime.toString(),
                guestCount = state.guestCount,
                zone = state.selectedZone,
                specialRequests = state.specialRequests,
                reservationStatus = "Confirmed",
                customerId = state.customer!!.customerId
            )
            reservationRepository.insertTable(reservation)

            onResult(reservationId)
        }
    }

    fun setSelectedTables(tables: List<TableEntity>) {
        _uiState.update { currentState ->

            currentState.copy(
                selectedTables = tables,

                shouldNavigateToNextStep = true
            )
        }
    }

    // Inside ReservationViewModel.kt
    fun updateReservationFee(tables: Set<TableEntity>) {
        val fee = tables.sumOf { it.seatCount * 10 }.toDouble()
        _uiState.update { it.copy(reservationFee = fee) }
    }
}