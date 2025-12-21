package com.example.yumyumrestaurant.data

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Build
import android.util.Log
import android.widget.NumberPicker
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.yumyumrestaurant.Reservation.ReservationNavEvent
import com.example.yumyumrestaurant.Reservation.ReservationViewModel
import com.example.yumyumrestaurant.ReservationTable.ReservationTableViewModel
import com.example.yumyumrestaurant.data.TableData.ZoneType
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM d, yyyy")
private val DATE_DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("EEE, MMM d ,yyyy")
private val TIME_DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a")

private const val DATE_PLACEHOLDER_TEXT = "-"
private const val DEFAULT_DURATION_MINUTES = 30

fun formatDuration(totalMinutes: Int): String {


    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60

    return when {

        minutes == 0 -> {
            "$hours hr 0 min"
        }

        hours == 0 -> {
            "0 hr $minutes min"
        }

        else -> {
            "$hours hr $minutes min"
        }
    }
}



@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ReservationFormScreen( onNextScreen: () -> Unit,reservationTableviewModel: ReservationTableViewModel) {
    val uiState by reservationTableviewModel.reservationViewModel.uiState.collectAsState()
    val context = LocalContext.current


    LaunchedEffect(Unit) {
        reservationTableviewModel.reservationViewModel.navigationEvents.collect { event ->
            when (event) {
                is ReservationNavEvent.NavigateToTableSelection -> {
                    onNextScreen()
                }
            }
        }
    }

    LaunchedEffect(reservationTableviewModel.reservationViewModel.validationEvents) {
        reservationTableviewModel.reservationViewModel.validationEvents.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    ReservationFormScreenBody(reservationTableviewModel, onNextScreen)
}

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ReservationFormScreenBody( reservationTableviewModel: ReservationTableViewModel,onNextScreen: () -> Unit) {
    val context = LocalContext.current
    val uiState by reservationTableviewModel.reservationViewModel.uiState.collectAsState()

    val selectedLocalDate = uiState.selectedDate
    val selectedStartTime = uiState.selectedStartTime


    val calculateEndTime = remember(selectedStartTime, uiState.selectedDurationMinutes) {
        reservationTableviewModel.reservationViewModel.calculateEndTime(selectedStartTime, uiState.selectedDurationMinutes)
    }






    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {

        Spacer(Modifier.height(16.dp))

        LabelWithAsterisk("Reservation Date")
        OutlinedTextField(
            value = selectedLocalDate?.format(DATE_DISPLAY_FORMATTER) ?: "",
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .clickable { reservationTableviewModel.reservationViewModel.setShowDatePicker(true) },
            enabled = false,
            leadingIcon = { Icon(Icons.Default.CalendarMonth, null) },
            placeholder = { Text("Select Date") },
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                LabelWithAsterisk("Start Time")
                OutlinedTextField(
                    value = selectedStartTime.format(TIME_DISPLAY_FORMATTER),
                    onValueChange = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { reservationTableviewModel.reservationViewModel.setShowStartTimePicker(true) },
                    enabled = false,
                    shape = RoundedCornerShape(12.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                LabelWithAsterisk("Duration")
                OutlinedTextField(
                    value = formatDuration(uiState.selectedDurationMinutes),
                    onValueChange = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { reservationTableviewModel.reservationViewModel.setShowDurationPicker(true) },
                    enabled = false,
                    shape = RoundedCornerShape(12.dp)
                )
            }

        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F7F7)),
            border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TimeSummaryUnit("Start Time", selectedStartTime)
                Icon(Icons.Default.ArrowForward, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                TimeSummaryUnit("End Time", calculateEndTime)
            }
        }

        Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color.LightGray.copy(alpha = 0.4f))


        LabelWithAsterisk("Seating Area")

        val zones = ZoneType.entries.map { it.name }

        ExposedDropdownMenuBox(
            expanded = uiState.zoneExpanded,
            onExpandedChange = { reservationTableviewModel.reservationViewModel.setZoneExpanded(!uiState.zoneExpanded)  }
        ) {
            OutlinedTextField(
                value = uiState.selectedZone,
                onValueChange = {},
                readOnly = true,
                label = { Text("Select Zone") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = uiState.zoneExpanded)
                },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = uiState.zoneExpanded,
                onDismissRequest = { reservationTableviewModel.reservationViewModel.setZoneExpanded(false) }
            ) {
                zones.forEach { zone->
                    DropdownMenuItem(
                        text = { Text(zone)},
                        onClick = {
                            reservationTableviewModel.reservationViewModel.setSelectedZone(zone)
                            reservationTableviewModel.reservationViewModel.setZoneExpanded(false)
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Number of Guests", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            GuestCounter(uiState.guestCount, reservationTableviewModel.reservationViewModel)
        }

        Spacer(Modifier.height(24.dp))

        Text("Special Requests (Optional)", fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value = uiState.specialRequests,
            onValueChange = { reservationTableviewModel.reservationViewModel.setSpecialRequests(it) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Example: Need a high chair.") },
            label = { Text("Special Request?") },
            singleLine = false,
            maxLines = 5,
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {

                reservationTableviewModel.refreshTableStatuses(
                    uiState.selectedDate,
                    uiState.selectedStartTime,
                    uiState.selectedDurationMinutes
                )
                reservationTableviewModel.reservationViewModel.searchTable() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Search Available Table")
        }
    }


    if (uiState.showDurationPicker) {
        DurationPickerDialog(
            initialMinutes = uiState.selectedDurationMinutes,
            onDismiss = { reservationTableviewModel.reservationViewModel.setShowDurationPicker(false) },
            onConfirm = { totalMinutes ->
                reservationTableviewModel.reservationViewModel.setSelectedDurationMinutes(totalMinutes)
            }
        )
    }

    if (uiState.showDatePicker) {
        val initialDate = uiState.selectedDate ?: LocalDate.now()

        val picker = DatePickerDialog(
            context,
            { _, year, month, day ->
                reservationTableviewModel.reservationViewModel.setSelectedDate(LocalDate.of(year, month + 1, day))
                reservationTableviewModel.reservationViewModel.setShowDatePicker(false)
            },
            initialDate.year,
            initialDate.monthValue - 1,
            initialDate.dayOfMonth
        )

         picker.datePicker.minDate = reservationTableviewModel.reservationViewModel.localDateToMillis(initialDate)

        picker.setOnDismissListener { reservationTableviewModel.reservationViewModel.setShowDatePicker(false) }
        picker.show()
    }
    if (uiState.showStartTimePicker) {
        val initialLocalTime = uiState.selectedStartTime ?: LocalTime.now()

        val initialHour = initialLocalTime.hour
        val initialMinute = initialLocalTime.minute



        TimePickerDialog(
            context,
            { _, selectedHour, selectedMinute ->
                val selectedTime = LocalTime.of(selectedHour, selectedMinute)

                reservationTableviewModel.reservationViewModel.setSelectedStartTime(selectedTime)


                reservationTableviewModel.reservationViewModel.setShowStartTimePicker(false)
            },
            initialHour,
            initialMinute,
            false
        ).apply {
            setOnCancelListener { reservationTableviewModel.reservationViewModel.setShowStartTimePicker(false) }
            show()
        }
    }
}



@Composable
fun DurationPickerDialog(
    initialMinutes: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var hour by remember { mutableIntStateOf(initialMinutes / 60) }
    var minute by remember { mutableIntStateOf(initialMinutes % 60) }


    val availableIntervals = remember(hour) {
        when (hour) {
            0 -> listOf(15, 30, 45)
            5 -> listOf(0)
            else -> listOf(0, 15, 30, 45)
        }
    }


    LaunchedEffect(availableIntervals) {
        if (!availableIntervals.contains(minute)) {
            minute = availableIntervals.first()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Duration", fontWeight = FontWeight.Bold) },
        text = {
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            ) {

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("hr", style = MaterialTheme.typography.labelMedium)
                    DurationNumberPicker(
                        value = hour,
                        range = 0..5,
                        onValueChange = { hour = it }
                    )
                }

                Spacer(modifier = Modifier.width(32.dp))

                // Minute Column
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("min", style = MaterialTheme.typography.labelMedium)
                    DurationNumberPicker(
                        value = minute,
                        displayedValues = availableIntervals,
                        onValueChange = { minute = it }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm((hour * 60) + minute)
                onDismiss()
            }) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}



@Composable
fun DurationNumberPicker(
    value: Int,
    range: IntRange = 0..0,
    displayedValues: List<Int>? = null,
    onValueChange: (Int) -> Unit
) {
    AndroidView(
        modifier = Modifier.width(70.dp),
        factory = { context ->
            NumberPicker(context).apply {
                if (displayedValues != null) {
                    val stringValues = displayedValues.map { String.format("%02d", it) }.toTypedArray()
                    minValue = 0
                    maxValue = stringValues.size - 1
                    this.displayedValues = stringValues
                    this.value = displayedValues.indexOf(value).coerceAtLeast(0)
                } else {
                    minValue = range.first
                    maxValue = range.last
                    this.value = value
                }

                // Initial Listener
                setOnValueChangedListener { _, _, newVal ->
                    if (displayedValues != null) {
                        // CRITICAL: Check if index is valid before accessing list
                        if (newVal >= 0 && newVal < displayedValues.size) {
                            onValueChange(displayedValues[newVal])
                        }
                    } else {
                        onValueChange(newVal)
                    }
                }
            }
        },
        update = { picker ->
            if (displayedValues != null) {
                val stringValues = displayedValues.map { String.format("%02d", it) }.toTypedArray()
                val targetIndex = displayedValues.indexOf(value).coerceAtLeast(0)

                // 1. Temporarily remove listener to prevent recursive calls/crashes during update
                picker.setOnValueChangedListener(null)

                // 2. Perform the reset sequence
                picker.displayedValues = null
                picker.minValue = 0
                picker.maxValue = (stringValues.size - 1).coerceAtLeast(0)
                picker.displayedValues = stringValues
                picker.value = targetIndex

                // 3. Re-attach the listener with the new data reference
                picker.setOnValueChangedListener { _, _, newVal ->
                    if (newVal >= 0 && newVal < displayedValues.size) {
                        onValueChange(displayedValues[newVal])
                    }
                }
            } else {
                if (picker.value != value) {
                    picker.value = value
                }
            }
        }
    )
}

@Composable
fun LabelWithAsterisk(label: String) {
    Row(modifier = Modifier.padding(bottom = 4.dp)) {
        Text(text = label, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Text(text = " *", color = Color.Red, fontSize = 14.sp)
    }
}

@Composable
fun TimeSummaryUnit(label: String, time: java.time.LocalTime) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 12.sp, color = Color.Gray)
        Text(time.format(java.time.format.DateTimeFormatter.ofPattern("hh:mm a")), fontWeight = FontWeight.Bold)
    }
}

@Composable
fun GuestCounter(count: Int, viewModel: ReservationViewModel) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = { viewModel.decrementGuestCount() },enabled = count > 1) {
            Icon(Icons.Default.RemoveCircleOutline, null)
        }
        Text(count.toString(), fontSize = 18.sp, modifier = Modifier.padding(horizontal = 8.dp))
        IconButton(onClick = { viewModel.incrementGuestCount() }) {
            Icon(Icons.Default.AddCircleOutline, null)
        }
    }
}