package com.example.yumyumrestaurant.shareFilterScreen

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.material3.DatePickerDefaults.dateFormatter


import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import com.example.yumyumrestaurant.R

import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.SemanticsProperties.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.text.format

@Composable
fun SharedFilterSection(
    viewModel: SharedFilterViewModel,
    title: String,
    onFilterApplied: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var localSearchQuery by rememberSaveable { mutableStateOf(uiState.searchQuery) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(dimensionResource(R.dimen.dp_8))
    ) {
        OutlinedTextField(
            value = localSearchQuery,
            onValueChange = { localSearchQuery = it },
            placeholder = { Text(stringResource(R.string.search_item)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,

            keyboardActions = KeyboardActions(
                onSearch = {
                    viewModel.updateSearchQuery(localSearchQuery)
                    viewModel.performSearch()
                }
            )
        )

        Spacer(Modifier.height(dimensionResource(R.dimen.dp_8)))

        AppliedFiltersSummary(
            filters = uiState.appliedFilters,
            onRemoveFilter = { type, value ->
                viewModel.removeFilter(type, value)
                             },
            onClearAll = { viewModel.showClearFiltersConfirmationDialog(true) }
        )

        Spacer(Modifier.height(dimensionResource(R.dimen.dp_2)))

        ClearFilterDialog(uiState = uiState, viewModel = viewModel)

        FilterDialog(viewModel = viewModel)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppliedFiltersSummary(
    filters: Map<String, List<String>>,
    onRemoveFilter: (filterType: String, value: String) -> Unit,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = dimensionResource(R.dimen.dp_8))
    ) {
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState())
                .padding(bottom = dimensionResource(R.dimen.dp_8)),
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.dp_12))
        ) {
            filters.forEach { (filterType, values) ->
                Text("$filterType:", modifier = Modifier.alignByBaseline())
                if (values.isEmpty() || (values.size == 1 && values.contains("All"))) {
                    FilterChip("All", showRemoveIcon = false) { onRemoveFilter(filterType, "All") }
                } else {
                    values.filter { it != "All" }.forEach { value ->
                        FilterChip(value) { onRemoveFilter(filterType, value) }
                    }
                }
            }
        }

        if (filters.any { it.value.any { v -> v != "All" } }) {
            TextButton(onClick = onClearAll) {
                Text(stringResource(R.string.clear_all_filters))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun FilterDialog(viewModel: SharedFilterViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val locationLabels = listOf("All","INDOOR", "OUTDOOR")

    if (uiState.isFilterDialogVisible) {
        Dialog(onDismissRequest = { viewModel.toggleFilterDialog(false) }) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(dimensionResource(R.dimen.dp_12)),
                tonalElevation = dimensionResource(R.dimen.dp_4)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(dimensionResource(R.dimen.dp_16))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .padding(bottom = dimensionResource(R.dimen.dp_8)),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(onClick = { viewModel.toggleFilterDialog(false) }) {
                            Icon(Icons.Default.Close, contentDescription = stringResource(R.string.close_filter_dialog))
                        }
                        Text(
                            "Filter Reservation",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = {
                            val isValid = viewModel.validateTimeRange()
                            if (isValid) {
                                viewModel.performSearch()
                                viewModel.toggleFilterDialog(false)
                            }
                        }) { Text(stringResource(R.string.filter)) }
                    }

                    Spacer(Modifier.height(dimensionResource(R.dimen.dp_16)))



                    if (uiState.showLocation) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.setIsLocationExpanded(!uiState.isLocationExpanded) }
                                .padding(vertical = dimensionResource(R.dimen.dp_8)),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                stringResource(R.string.location),
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = if (uiState.isLocationExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = if (uiState.isLocationExpanded) stringResource(R.string.collapse) else stringResource(R.string.expand)
                            )
                        }

                        AnimatedVisibility(visible = uiState.isLocationExpanded) {
                            Column {
                                locationLabels.forEach { location ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = dimensionResource(R.dimen.dp_8)),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = location in uiState.selectedLocations,
                                            onCheckedChange = { checked ->
                                                viewModel.toggleLocation(location, checked)
                                            }

                                        )


                                        Spacer(modifier = Modifier.width(dimensionResource(R.dimen.dp_8)))
                                        Text(
                                            text = location,
                                            style = MaterialTheme.typography.bodyLarge,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }

                    }


                    val calendar = Calendar.getInstance()
                    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")





                    val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH)


                    if (uiState.showDateTime) {

                        Text(
                            text = "\uD83D\uDCC5Reservation Date",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )


                        calendar.set(Calendar.HOUR_OF_DAY, 0)
                        calendar.set(Calendar.MINUTE, 0)
                        calendar.set(Calendar.SECOND, 0)


                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val picker = DatePickerDialog(
                                        context,
                                        { _, year, month, day ->
                                            viewModel.setReservationDate(LocalDate.of(year, month + 1, day))
                                        },
                                        uiState.reservationDate?.year ?: calendar.get(Calendar.YEAR),
                                        uiState.reservationDate?.monthValue?.minus(1) ?: calendar.get(Calendar.MONTH),
                                        uiState.reservationDate?.dayOfMonth ?: calendar.get(Calendar.DAY_OF_MONTH)
                                    )






                                    picker.show()
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        text = uiState.reservationDate
                                            ?.format(dateFormatter)
                                            ?: "Select Date",
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.align(Alignment.CenterStart)
                                    )
                                    Icon(
                                        Icons.Default.KeyboardArrowDown,
                                        contentDescription = null,
                                        modifier = Modifier.align(Alignment.CenterEnd)
                                    )
                                }
                            }
                        }


                        Text(
                            text = "\uD83D\uDCDCReservation Made Date",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val picker = DatePickerDialog(
                                        context,
                                        { _, year, month, day ->
                                            viewModel.setReservationMadeDate(LocalDate.of(year, month + 1, day))
                                        },
                                        uiState.reservationDate?.year ?: calendar.get(Calendar.YEAR),
                                        uiState.reservationDate?.monthValue?.minus(1) ?: calendar.get(Calendar.MONTH),
                                        uiState.reservationDate?.dayOfMonth ?: calendar.get(Calendar.DAY_OF_MONTH)
                                    )






                                    picker.show()
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        text = uiState.reservationDate
                                            ?.format(dateFormatter)
                                            ?: "Select Date",
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.align(Alignment.CenterStart)
                                    )
                                    Icon(
                                        Icons.Default.KeyboardArrowDown,
                                        contentDescription = null,
                                        modifier = Modifier.align(Alignment.CenterEnd)
                                    )
                                }
                            }
                        }


                        val calendar = Calendar.getInstance()

                        val defaultHour = uiState.startTime?.hour ?: calendar.get(Calendar.HOUR_OF_DAY)
                        val defaultMinute = uiState.startTime?.minute ?: calendar.get(Calendar.MINUTE)


                        Text(
                            text = "⏰ Time Range",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedButton(
                                onClick = {

                                    TimePickerDialog(
                                        context,
                                        { _, hour, minute ->
                                            val selectedStartTime = LocalTime.of(hour, minute)


                                            viewModel.setStartTime(selectedStartTime)


                                        },
                                        defaultHour,
                                        defaultMinute,
                                        false
                                    ).show()
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {

                                Box(modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        text = uiState.startTime?.format(timeFormatter) ?: stringResource(R.string.start),
                                        style = MaterialTheme.typography.bodyMedium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Clip,
                                        modifier = Modifier.align(Alignment.CenterStart)
                                    )
                                    Icon(
                                        Icons.Default.KeyboardArrowDown,
                                        contentDescription = "stringResource(R.string.select_start_time)",
                                        modifier = Modifier.align(Alignment.CenterEnd)
                                    )
                                }

                            }

                            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.dp_8)))


                            val defaultHour1 =
                                uiState.endTime?.hour ?: calendar.get(Calendar.HOUR_OF_DAY)
                            val defaultMinute1 =
                                uiState.endTime?.minute ?: calendar.get(Calendar.MINUTE)


                            val context = LocalContext.current
                            LaunchedEffect(Unit) {
                                viewModel.errorEvent.collect { message ->
                                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                }
                            }


                            OutlinedButton(
                                onClick = {

                                    TimePickerDialog(
                                        context,
                                        { _, hour, minute ->

                                            val selectedEndTime = LocalTime.of(hour, minute)
                                            viewModel.setEndTime(selectedEndTime)


                                        },
                                        defaultHour1,
                                        defaultMinute1,
                                        false
                                    ).show()
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {

                                Box(modifier = Modifier.fillMaxWidth()) {
                                    Text(
                                        text = uiState.endTime?.format(timeFormatter) ?: stringResource(
                                            R.string.end
                                        ),
                                        style = MaterialTheme.typography.bodyMedium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Clip,
                                        modifier = Modifier.align(Alignment.CenterStart)
                                    )
                                    Icon(Icons.Default.KeyboardArrowDown,
                                        contentDescription = "stringResource(R.string.select_end_time)",
                                        modifier = Modifier.align(Alignment.CenterEnd))
                                }

                            }
                        }

                    }


                    LaunchedEffect(Unit) {
                        viewModel.errorEvent.collect { message ->
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun datePicker(uiState: FilterUiState, viewModel: SharedFilterViewModel) {
    val context = LocalContext.current
    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val calendar = Calendar.getInstance()

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "📅 Select Date",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        DatePickerButton(
            text = uiState.reservationDate?.format(dateFormatter) ?: "Select Date",
            onClick = {
                val picker = DatePickerDialog(context)
                picker.datePicker.minDate = calendar.timeInMillis
                picker.setOnDateSetListener { _, year, month, day ->
                    viewModel.setReservationDate(LocalDate.of(year, month + 1, day))
                }
                picker.show()
            }
        )
    }
}


@Composable
fun DatePickerButton(text: String, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Text(text = text, modifier = Modifier.align(Alignment.CenterStart))
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, modifier = Modifier.align(Alignment.CenterEnd))
        }
    }
}

@Composable
fun TimePickerButton(text: String, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Text(text = text, modifier = Modifier.align(Alignment.CenterStart))
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, modifier = Modifier.align(Alignment.CenterEnd))
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun startTimePicker(uiState: FilterUiState, viewModel: SharedFilterViewModel) {
    val context = LocalContext.current
    val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a")

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "⏰ Select Start Time",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        TimePickerButton(
            text = uiState.startTime?.format(timeFormatter) ?: "Select Start Time",
            onClick = {
                val now = uiState.startTime ?: LocalTime.now()
                TimePickerDialog(
                    context,
                    { _, hour, minute ->
                        viewModel.setStartTime(LocalTime.of(hour, minute))
                    },
                    now.hour,
                    now.minute,
                    false
                ).show()
            }
        )
    }
}


@Composable
fun FilterSection(
    title: String,
    items: List<String>,
    selectedItems: List<String>,
    isExpanded: Boolean,
    onExpandToggle: () -> Unit,
    onItemToggle: (String, Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onExpandToggle() }.padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
        Icon(
            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
            contentDescription = null
        )
    }

    AnimatedVisibility(visible = isExpanded) {
        Column {
            items.forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = item in selectedItems,
                        onCheckedChange = { checked -> onItemToggle(item, checked) }
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(item, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}



@Composable
fun FilterChip(label: String, showRemoveIcon: Boolean = true, onRemove: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
        tonalElevation = 2.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(label)
            if (showRemoveIcon) {
                Spacer(Modifier.width(4.dp))
                Icon(
                    Icons.Default.Close,
                    contentDescription = stringResource(R.string.remove_filter),
                    modifier = Modifier
                        .size(16.dp)
                        .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) {
                            onRemove()
                        }
                )
            }
        }
    }
}

@Composable
fun ClearFilterDialog(uiState: FilterUiState, viewModel: SharedFilterViewModel) {
    if (uiState.showClearFiltersConfirmation) {
        AlertDialog(
            onDismissRequest = { viewModel.showClearFiltersConfirmationDialog(false) },
            title = { Text(stringResource(R.string.confirm_clear_filters)) },
            text = { Text(stringResource(R.string.clear_all_filters_message)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearAllFilters()
                    viewModel.showClearFiltersConfirmationDialog(false)
                }) { Text(stringResource(R.string.yes)) }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.showClearFiltersConfirmationDialog(false) }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun endTimePicker(uiState: FilterUiState, viewModel: SharedFilterViewModel) {
    val context = LocalContext.current
    val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a")

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "⏰ Select End Time",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        TimePickerButton(
            text = uiState.endTime?.format(timeFormatter) ?: "Select End Time",
            onClick = {
                val now = uiState.endTime
                    ?: uiState.startTime
                    ?: LocalTime.now()

                TimePickerDialog(
                    context,
                    { _, hour, minute ->
                        val selectedEndTime = LocalTime.of(hour, minute)



                        viewModel.setEndTime(selectedEndTime)

                    },
                    now.hour,
                    now.minute,
                    false // 12-hour AM/PM
                ).show()
            }
        )
    }
}
