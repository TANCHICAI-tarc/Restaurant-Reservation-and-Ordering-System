package com.example.yumyumrestaurant.data

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Build
import android.widget.NumberPicker
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.yumyumrestaurant.Reservation.ReservationViewModel
import com.example.yumyumrestaurant.data.TableData.ZoneType
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM d, yyyy")
private val DATE_DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("EEE, MMM d ,yyyy")
private val TIME_DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a")

private const val DATE_PLACEHOLDER_TEXT = "-"
private const val DEFAULT_DURATION_MINUTES = 15

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
fun ReservationFormScreen( onNextScreen: () -> Unit,viewModel: ReservationViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current



    LaunchedEffect(uiState.shouldNavigateToNextStep) {
        if (uiState.shouldNavigateToNextStep) {
            onNextScreen()
            viewModel.resetNavigationFlag()
        }
    }

    LaunchedEffect(viewModel.validationEvents) {
        viewModel.validationEvents.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    ReservationFormScreenBody(viewModel, onNextScreen)
}

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ReservationFormScreenBody( viewModel: ReservationViewModel,onNextScreen: () -> Unit) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    val selectedLocalDate = uiState.selectedDate
    val selectedStartTime = uiState.selectedStartTime

    val selectedStartDateText = selectedLocalDate?.format(DATE_FORMATTER) ?: DATE_PLACEHOLDER_TEXT



    val calculateEndTime = remember(selectedStartTime, uiState.selectedDurationMinutes) {
        viewModel.calculateEndTime(selectedStartTime, uiState.selectedDurationMinutes)
    }

    val calculatedEndDate = remember(selectedLocalDate, selectedStartTime, uiState.selectedDurationMinutes) {
        viewModel.calculateEndDate(selectedLocalDate, selectedStartTime, uiState.selectedDurationMinutes)
    }

    val shouldShowPlaceholder = remember(calculatedEndDate, selectedLocalDate, selectedStartTime) {
        selectedLocalDate == null || selectedStartTime == null || calculatedEndDate == null
    }

    val calculatedEndDateText = if (shouldShowPlaceholder) {
        DATE_PLACEHOLDER_TEXT
    } else {
        calculatedEndDate?.format(DATE_FORMATTER) ?: DATE_PLACEHOLDER_TEXT
    }

    val calculateEndTimeText = if (shouldShowPlaceholder) {
        DATE_PLACEHOLDER_TEXT
    } else {
        calculateEndTime?.format(TIME_DISPLAY_FORMATTER) ?: DATE_PLACEHOLDER_TEXT
    }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        Text("Reserve a Table", style = MaterialTheme.typography.headlineSmall)

        Spacer(Modifier.height(16.dp))

        Row {
            Text(text = "Reservation Date:", fontSize = 16.sp)
            Text(text = " *", color = Color.Red, fontSize = 16.sp)
        }
        OutlinedTextField(
            value = selectedLocalDate?.format(DATE_DISPLAY_FORMATTER) ?: "",
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .clickable { viewModel.setShowDatePicker(true) },
            enabled = false,
            placeholder = { Text("Select Date") }
        )

        Spacer(Modifier.height(16.dp))

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Row {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Start Date", fontSize = 20.sp)
                    OutlinedButton(onClick = { viewModel.setShowDatePicker(true) }) {
                        Text(selectedStartDateText, fontSize = 15.sp)
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("End Date", fontSize = 20.sp)
                    OutlinedButton(onClick = { }) {
                        Text(calculatedEndDateText, fontSize = 15.sp)
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Row {
            Text(text = "Reservation Time:", fontSize = 16.sp)
            Text(text = " *", color = Color.Red, fontSize = 16.sp)
        }

        OutlinedTextField(
            value = selectedStartTime?.format(TIME_DISPLAY_FORMATTER) ?: "",
            onValueChange = {},
            modifier = Modifier.fillMaxWidth()
                .clickable { viewModel.setShowStartTimePicker(true) },
            enabled = false,
        )

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Row {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Start Time", fontSize = 20.sp)
                    OutlinedButton(onClick = { viewModel.setShowStartTimePicker(true) }) {
                        Text(
                            selectedStartTime?.format(TIME_DISPLAY_FORMATTER) ?: DATE_PLACEHOLDER_TEXT,
                            fontSize = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("End Time", fontSize = 20.sp)
                    OutlinedButton(onClick = { }) {
                        Text(calculateEndTimeText, fontSize = 15.sp)
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Duration:")



            OutlinedTextField(
                value = formatDuration(uiState.selectedDurationMinutes) ,
                onValueChange = {},
                modifier = Modifier.fillMaxWidth()
                    .clickable { viewModel.setShowDurationPicker(true) },
                enabled = false,
            )

        }

        Spacer(modifier = Modifier.height(8.dp))


        Spacer(Modifier.height(16.dp))

        Row {
            Text(text = "Seating Area:", fontSize = 16.sp)
            Text(text = " *", color = Color.Red, fontSize = 16.sp)
        }

        val zones = ZoneType.entries.map { it.name }

        ExposedDropdownMenuBox(
            expanded = uiState.zoneExpanded,
            onExpandedChange = { viewModel.setZoneExpanded(!uiState.zoneExpanded)  }
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
                onDismissRequest = { viewModel.setZoneExpanded(false) }
            ) {
                zones.forEach { zone->
                    DropdownMenuItem(
                        text = { Text(zone)},
                        onClick = {
                            viewModel.setSelectedZone(zone)
                            viewModel.setZoneExpanded(false)
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Guests: ${uiState.guestCount}", modifier = Modifier.weight(1f))

            IconButton(onClick = { viewModel.decrementGuestCount() }) {
                Icon(Icons.Default.Remove, contentDescription = "Decrease")
            }

            Text(
                text = uiState.guestCount.toString(),
                fontSize = 20.sp,
                modifier = Modifier.width(40.dp),
                textAlign = TextAlign.Center
            )

            IconButton(onClick = { viewModel.incrementGuestCount() }) {
                Icon(Icons.Default.Add, contentDescription = "Increase")
            }
        }

        Spacer(Modifier.height(24.dp))

        Text("Special Requests:(Optional)", style = MaterialTheme.typography.titleMedium)

        OutlinedTextField(
            value = uiState.specialRequests,
            onValueChange = { viewModel.setSpecialRequests(it) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Example: Need a high chair.") },
            label = { Text("Special Request") },
            singleLine = false,
            maxLines = 5,
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = { viewModel.searchTable() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Search Now")
        }
    }


    if (uiState.showDurationPicker) {
        DurationPickerDialog(
            initialMinutes = uiState.selectedDurationMinutes,
            onDismiss = { viewModel.setShowDurationPicker(false) },
            onConfirm = { totalMinutes ->
                viewModel.setSelectedDurationMinutes(totalMinutes)
            }
        )
    }

    if (uiState.showDatePicker) {
        val initialDate = uiState.selectedDate ?: LocalDate.now()

        val initialYear = initialDate.year
        val initialMonth = initialDate.monthValue - 1
        val initialDay = initialDate.dayOfMonth

        val picker = DatePickerDialog(
            context,
            { _, year, month, day ->
                viewModel.setSelectedDate(LocalDate.of(year, month + 1, day))
                viewModel.setShowDatePicker(false)
            },
            initialYear,
            initialMonth,
            initialDay
        )

        val todayInMillis = viewModel.localDateToMillis(LocalDate.now())
        picker.datePicker.minDate = todayInMillis

        picker.setOnCancelListener { viewModel.setShowDatePicker(false) }
        picker.setOnDismissListener { viewModel.setShowDatePicker(false) }

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
                if(viewModel.validateStartTimeSelection(selectedTime)){
                    viewModel.setSelectedStartTime(selectedTime)
                }

                viewModel.setShowStartTimePicker(false)
            },
            initialHour,
            initialMinute,
            false
        ).apply {
            setOnCancelListener { viewModel.setShowStartTimePicker(false) }
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
    val initialHour = initialMinutes / 60
    val initialMinute = initialMinutes % 60

    var hour by remember { mutableStateOf(initialHour) }
    var minute by remember { mutableStateOf(initialMinute) }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Select Duration") },
        text = {
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {

                // Hour Column
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("hr")
                    Spacer(Modifier.height(8.dp))
                    DurationNumberPicker(
                        value = hour,
                        range = 0..5,
                        onValueChange = { hour = it
                            minute = when {
                                hour == 0 && minute < 15 -> 15
                                hour == 5 -> 0
                                else -> minute
                            }
                        }
                    )
                }

                val minuteRange = when (hour) {
                    0 -> 15..59
                    5 -> 0..0        // Only 0 mins allowed at max hour
                    else -> 0..59    // Normal range
                }


                Spacer(modifier = Modifier.width(32.dp))

                // Minute Column
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("min")
                    Spacer(Modifier.height(8.dp))
                    DurationNumberPicker(
                        value = minute,
                        range = minuteRange,
                        onValueChange = { minute = it }
                    )
                }
            }

        },
        confirmButton = {
            TextButton(onClick = {
                val totalMinutes = hour * 60 + minute

                if (totalMinutes in 15..300) {
                    onConfirm(totalMinutes)
                }
                onDismiss()
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) { Text("Cancel") }
        }
    )
}

@Composable
fun DurationNumberPicker(
    value: Int,
    range: IntRange,
    onValueChange: (Int) -> Unit
) {
    AndroidView(
        factory = { context ->
            NumberPicker(context).apply {
                minValue = range.first
                maxValue = range.last
                setOnValueChangedListener { _, _, newVal ->
                    onValueChange(newVal)
                }
            }
        },
        update = {
            it.minValue = range.first
            it.maxValue = range.last
            if (it.value != value) it.value = value
        }
    )
}


