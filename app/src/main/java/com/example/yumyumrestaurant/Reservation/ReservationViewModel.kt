package com.example.yumyumrestaurant.Reservation


import android.app.Application
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.*
import com.example.stayeasehotel.data.regiondata.RegionDatabase
import com.example.yumyumrestaurant.UserSession
import com.example.yumyumrestaurant.data.CustomerEntity
import com.example.yumyumrestaurant.data.RegionData.RegionRepository
import com.example.yumyumrestaurant.data.ReservationData.ReservationEntity
import com.example.yumyumrestaurant.data.ReservationData.ReservationRepository
import com.example.yumyumrestaurant.data.ReservationTableData.Reservation_TableDatabase
import com.example.yumyumrestaurant.data.TableData.TableEntity
import com.example.yumyumrestaurant.data.TableData.TableRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
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
sealed class ReservationNavEvent {
    object NavigateToTableSelection : ReservationNavEvent()
}

@RequiresApi(Build.VERSION_CODES.O)
class ReservationViewModel(application: Application) : AndroidViewModel(application) {
    private val _busyRanges = MutableStateFlow<List<Pair<LocalTime, LocalTime>>>(emptyList())
    val busyRanges: StateFlow<List<Pair<LocalTime, LocalTime>>> = _busyRanges.asStateFlow()
    private val _uiState = MutableStateFlow(ReservationUiState())
    val uiState: StateFlow<ReservationUiState> = _uiState.asStateFlow()

    private val _validationEvents = MutableSharedFlow<String>()
    val validationEvents: SharedFlow<String> = _validationEvents.asSharedFlow()



    private val openingTime = LocalTime.of(9, 0)
    private val closingTime = LocalTime.of(22, 0)
    private val minDuration = 30
    private val maxDuration = 300
    private val minAdvanceMinutes = 120L

    private val reservationRepository : ReservationRepository

    private val currentCustomer: CustomerEntity
        get() {
            val account = UserSession.currentAccount
            return if (account != null) {
                CustomerEntity(
                    userId = account.userId,
                    name = account.name,
                    email = account.email,
                    phoneNum = account.phoneNum
                )
            } else {
                CustomerEntity()
            }
        }

    private val currentUserId: String
        get() = currentCustomer.userId

    init {

        val reservationDao = Reservation_TableDatabase.getReservationTableDatabase(application).reservationDao()
        reservationRepository = ReservationRepository(reservationDao)

        val currentDateTimePlus2 = LocalDateTime.now().plusHours(2)


        val (initialDate, initialTime) = normalizeStartDateTime(currentDateTimePlus2)

        _uiState.update { currentState ->
            currentState.copy(
                selectedDate = initialDate,
                selectedStartTime = initialTime
            )
        }

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
    fun updateStartTime(startTime: LocalTime) {
        _uiState.update { currentState ->
            try {


                val endTime = startTime.plusMinutes(15)


                currentState.copy(
                    selectedStartTime = startTime,
                    selectedEndTime = endTime
                )
            } catch (e: Exception) {
                // Fallback if parsing fails
                currentState.copy(selectedStartTime = startTime)
            }
        }
    }

    private val _navigationChannel = Channel<ReservationNavEvent>()
    val navigationEvents = _navigationChannel.receiveAsFlow()

    @RequiresApi(Build.VERSION_CODES.O)
    fun searchTable() {
        viewModelScope.launch {
            val state = _uiState.value


            val isValid = validateReservation(
                date = state.selectedDate ?: LocalDate.now(),
                time = state.selectedStartTime ?: LocalTime.now(),
                durationMinutes = state.selectedDurationMinutes
            )


            if (isValid) {
                _uiState.update {
                    it.copy(
                        isFormValid = true,
                        shouldNavigateToNextStep = true,
                        customer = currentCustomer
                    )
                }

                _navigationChannel.send(ReservationNavEvent.NavigateToTableSelection)
            } else {
                // Update state to show errors if needed
                _uiState.update { it.copy(isFormValid = false) }
            }
        }
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




    suspend fun getAvailableTimeSlots(
        date: LocalDate,
        tableId: String,
        userSelectDuration: Int
    ): List<LocalTime> {
        val confirmedReservations = reservationRepository
            .getConfirmedReservationsForTableOnDate(tableId, date.toString())



        val busyRanges = confirmedReservations?.mapNotNull { res ->
            try {

                val rawStart = LocalTime.parse(res.startTime)

                val start = rawStart.withSecond(0).withNano(0)


                val end = start.plusMinutes(res.durationMinutes.toLong() + 30)

                start to end
            } catch (e: Exception) {
                null
            }
        }

        val availableSlots = mutableListOf<LocalTime>()

        var currentTime = if (date == LocalDate.now()) {
            LocalTime.now().plusHours(2).withSecond(0).withNano(0)
        } else {
            openingTime.withSecond(0).withNano(0)
        }

        if (currentTime.isBefore(openingTime)) currentTime = openingTime

        while (currentTime.isBefore(closingTime)) {
            val slotStart = currentTime
            val slotEnd = currentTime.plusMinutes(userSelectDuration.toLong())

            if (slotEnd.isAfter(closingTime)) {
                 break
            }

            val overlappingBusyRange = busyRanges?.find { (busyStart, busyEnd) ->
                slotStart.isBefore(busyEnd) && busyStart.isBefore(slotEnd)
            }


            if (overlappingBusyRange == null) {
                if (slotStart.minute % 30 == 0) {
                    availableSlots.add(slotStart)
                }

                currentTime = currentTime.plusMinutes(1)
            } else {
                currentTime = overlappingBusyRange.second.plusMinutes(1)
            }
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






    suspend fun validateReservation(
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

            val now = LocalDateTime.now().withSecond(0).withNano(0)
            val minAdvanceDateTime = now.plusMinutes(minAdvanceMinutes)
            val startDateTimeTruncated = startDateTime.withSecond(0).withNano(0)

            if (startDateTimeTruncated.isEqual(minAdvanceDateTime)) {

                return true
            }
            if (!startDateTimeTruncated.isAfter(minAdvanceDateTime)) {
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

        val stateDate = _uiState.value.selectedDate ?: LocalDate.now()
        val duration = _uiState.value.selectedDurationMinutes


        if (time.isAfter(closingTime)) {
            viewModelScope.launch {
                _validationEvents.emit("We close at ${closingTime.format(TIME_DISPLAY_FORMATTER)}. Please select a time before 10:00 PM.")
            }
            return
        }

        if (time.isBefore(openingTime)) {
            viewModelScope.launch {
                _validationEvents.emit("We open at ${openingTime.format(TIME_DISPLAY_FORMATTER)}. Please select a time after 9:00 AM.")
            }
            return
        }


        val combinedDateTime = stateDate.atTime(time)


        val (normalizedDate, normalizedTime) = normalizeStartDateTime(combinedDateTime)


        updateReservation(
            date = normalizedDate,
            time = normalizedTime,
            duration = duration
        )
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


    fun generateReservationID(onGenerate: (String) -> Unit) {
        viewModelScope.launch {

            val reservationId = reservationRepository.generateReservationID()

            _uiState.update { currentState ->

                currentState.copy(
                    reservationId=reservationId
                ) }
            onGenerate(reservationId)
        }
    }
    fun saveReservation(onResult: (String) -> Unit) {
        val state = _uiState.value

        viewModelScope.launch {


            val reservation = ReservationEntity(
                reservationId = state.reservationId,
                date = state.selectedDate.toString(),
                startTime = state.selectedStartTime.toString(),
                durationMinutes = state.selectedDurationMinutes,
                endTime = state.selectedStartTime.plusMinutes(state.selectedDurationMinutes.toLong()).toString(),
                guestCount = state.guestCount,
                zone = state.selectedZone,
                specialRequests = state.specialRequests,
                reservationStatus = "Confirmed",
                userId = state.customer!!.userId

            )
            reservationRepository.insertTable(reservation)


            onResult(state.reservationId)
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




    private fun normalizeStartDateTime(
        dateTime: LocalDateTime
    ): Pair<LocalDate, LocalTime> {
        val openingTime = LocalTime.of(9, 0)
        val closingTime = LocalTime.of(22, 0)

        var date = dateTime.toLocalDate()
        val time = dateTime.toLocalTime()

        return when {

            time.isAfter(closingTime) || time == closingTime -> {
                date.plusDays(1) to openingTime
            }


            time.isBefore(openingTime) -> {
                date to openingTime
            }


            else -> {
                date to time
            }
        }
    }
    fun isReservationTimeValid(
        time: LocalTime,
        date: LocalDate,
        duration: Int
    ): Boolean {
        val slotStart = time.withSecond(0).withNano(0)
        val slotEnd = slotStart.plusMinutes(duration.toLong())


        if (date == LocalDate.now()) {
            if (slotStart.isBefore(LocalTime.now().plusHours(2))) return false
        }


        if (slotStart.isBefore(openingTime) || slotEnd.isAfter(closingTime)) return false


        return _busyRanges.value.none { (bStart, bEnd) ->
            slotStart.isBefore(bEnd) && bStart.isBefore(slotEnd)
        }
    }


}

