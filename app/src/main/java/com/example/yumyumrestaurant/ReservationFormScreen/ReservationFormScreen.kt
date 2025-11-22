package com.example.yumyumrestaurant.data


import android.app.TimePickerDialog
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import android.app.DatePickerDialog
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.ui.tooling.preview.Preview
import com.example.yumyumrestaurant.ReservationFormScreen.ReservationFormScreenViewModel
import com.example.yumyumrestaurant.data.ReservationData.ReservationData
import com.example.yumyumrestaurant.data.TableData.ZoneType

import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ReservationFormScreen( onNextScreen: () -> Unit,viewModel: ReservationFormScreenViewModel = viewModel() ) {
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(uiState.shouldNavigateToNextStep) {
        if (uiState.shouldNavigateToNextStep) {
            // 2. Trigger Navigation
            onNextScreen()

            viewModel.resetNavigationFlag()
        }
    }


    ReservationFormScreenBody(viewModel, onNextScreen)

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationFormScreenBody( viewModel: ReservationFormScreenViewModel,onNextScreen: () -> Unit) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    var specialRequests by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Reserve a Table", style = MaterialTheme.typography.headlineSmall)

        Spacer(Modifier.height(16.dp))


        Row {

            Text(
                text = "Date"+":",
                fontSize = 16.sp
            )

            Text(
                text = " *",
                color = Color.Red,
                fontSize = 16.sp
            )

        }
        // Date Field
        OutlinedTextField(
            value = uiState.selectedDate.format(DateTimeFormatter.ofPattern("EEE, MMM d")),
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .clickable { viewModel.setShowDatePicker(true)  },
            enabled = false,
            placeholder = { Text("Select Date") }
        )

        Spacer(modifier = Modifier.height(8.dp))


        Row {

            Text(
                text = "Start Time"+":",
                fontSize = 16.sp
            )

            Text(
                text = " *",
                color = Color.Red,
                fontSize = 16.sp
            )

        }

        // Time Field
        OutlinedTextField(
            value = uiState.selectedStartTime.format(DateTimeFormatter.ofPattern("hh:mm a")),
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .clickable { viewModel.setShowStartTimePicker(true) },
            enabled = false,
            placeholder = { Text("Select Start Time") }
        )

        Spacer(modifier = Modifier.height(8.dp))


        Row {

            Text(
                text = "End Time"+":",
                fontSize = 16.sp
            )

            Text(
                text = " *",
                color = Color.Red,
                fontSize = 16.sp
            )

        }

        // Time Field
        OutlinedTextField(
            value = uiState.selectedEndTime.format(DateTimeFormatter.ofPattern("hh:mm a")),
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .clickable { viewModel.setShowEndTimePicker(true) },
            enabled = false,
            placeholder = { Text("Select End Time") }
        )

        Spacer(Modifier.height(16.dp))

        Row {

            Text(
                text = "Seating Area"+":",
                fontSize = 16.sp
            )

            Text(
                text = " *",
                color = Color.Red,
                fontSize = 16.sp
            )

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
                label = { "Text(Select Zone) " },
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


        // Guest Count
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
            onClick = {
                viewModel.onReserveClicked()

                onNextScreen()

            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Reserve Now")
        }
    }

    // Android Date Picker
    if (uiState.showDatePicker) {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                uiState.selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
                viewModel.setShowDatePicker(false)
            },
            uiState.selectedDate.year,
            uiState.selectedDate.monthValue - 1,
            uiState.selectedDate.dayOfMonth
        ).show()
    }

    // Android Time Picker
    if (uiState.showStartTimePicker) {


        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(
            context,
            { _, selectedHour, selectedMinute ->
                val calendar = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, selectedHour)
                    set(Calendar.MINUTE, selectedMinute)
                }
                val time = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(calendar.time)

                viewModel.setSelectedStartTime(time)

                viewModel.setShowStartTimePicker(false)
            },
            hour,
            minute,
            false
        ).show()

    }

    if (uiState.showEndTimePicker) {


        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(
            context,
            { _, selectedHour, selectedMinute ->
                val calendar = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, selectedHour)
                    set(Calendar.MINUTE, selectedMinute)
                }
                val time = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(calendar.time)

                viewModel.setSelectedEndTime(time)

                viewModel.setShowEndTimePicker(false)
            },
            hour,
            minute,
            false
        ).show()

    }


}



