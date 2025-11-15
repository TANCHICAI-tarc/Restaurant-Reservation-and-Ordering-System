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
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import com.example.yumyumrestaurant.ReservationFormScreen.ReservationFormScreenViewModel
import com.example.yumyumrestaurant.data.ReservationData.ReservationData
import com.example.yumyumrestaurant.data.TableData.ZoneType

import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import androidx.lifecycle.viewmodel.compose.viewModel


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ReservationFormScreen( viewModel: ReservationFormScreenViewModel = viewModel() ) {
    ReservationFormScreenBody(viewModel)

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationFormScreenBody( viewModel: ReservationFormScreenViewModel) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()


    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedTime by remember { mutableStateOf(LocalTime.of(11, 0)) }
    var guestCount by remember { mutableStateOf(2) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

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
            value = selectedDate.format(DateTimeFormatter.ofPattern("EEE, MMM d")),
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDatePicker = true },
            enabled = false,
            placeholder = { Text("Select Date") }
        )

        Spacer(modifier = Modifier.height(8.dp))


        Row {

            Text(
                text = "Time"+":",
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
            value = selectedTime.format(DateTimeFormatter.ofPattern("hh:mm a")),
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showTimePicker = true },
            enabled = false,
            placeholder = { Text("Select Time") }
        )

        Spacer(Modifier.height(16.dp))

        Row {

            Text(
                text = "Time"+":",
                fontSize = 16.sp
            )

            Text(
                text = " *",
                color = Color.Red,
                fontSize = 16.sp
            )

        }

        val zones = listOf("All") + ZoneType.entries.map { it.name }
        var expanded by remember { mutableStateOf(false) }
        var selectedZone by remember { mutableStateOf("") }





        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedZone,
                onValueChange = {},
                readOnly = true,
                label = { "Text(stringResource(R.string.select_found_location))" },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                zones.forEach {
                    DropdownMenuItem(
                        text = { it },
                        onClick = {
//                            selectedZone = context.getString(locationResId)
                            expanded = false
//                            onLocationSelected(selectedLocation)
                        }
                    )
                }
            }
        }



        Spacer(Modifier.height(16.dp))


        // Guest Count
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Guests: $guestCount", modifier = Modifier.weight(1f))

            IconButton(onClick = { if (guestCount > 1) guestCount-- }) {
                Icon(Icons.Default.Remove, contentDescription = "Decrease")
            }

            Text(
                text = guestCount.toString(),
                fontSize = 20.sp,
                modifier = Modifier.width(40.dp),
                textAlign = TextAlign.Center
            )

            IconButton(onClick = { if (guestCount < 10) guestCount++ }) {
                Icon(Icons.Default.Add, contentDescription = "Increase")
            }
        }



        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
//                onReserve(ReservationData(selectedDate, selectedTime, guestCount))
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Reserve Now")
        }
    }

    // Android Date Picker
    if (showDatePicker) {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
                showDatePicker = false
            },
            selectedDate.year,
            selectedDate.monthValue - 1,
            selectedDate.dayOfMonth
        ).show()
    }

    // Android Time Picker
    if (showTimePicker) {
        TimePickerDialog(
            context,
            { _, hour, minute ->
                selectedTime = LocalTime.of(hour, minute)
                showTimePicker = false
            },
            selectedTime.hour,
            selectedTime.minute,
            false
        ).show()
    }
}