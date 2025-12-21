package com.example.yumyumrestaurant.TableDetailScreen

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.net.toUri
import com.example.yumyumrestaurant.Reservation.ReservationViewModel
import com.example.yumyumrestaurant.TableSelectionScreen.TableViewModel
import java.time.LocalDate
import java.time.LocalTime
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.pow

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.DialogProperties

import coil.ImageLoader
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.CachePolicy
import com.example.yumyumrestaurant.ReservationTable.ReservationTableViewModel

import java.io.File
import java.time.format.DateTimeFormatter


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TableDetailScreen(
    tableId: String,

    reservationTableViewModel: ReservationTableViewModel,
    onNavigateUp: () -> Unit,
    isReadOnly: Boolean = false
) {
    // Load table data
    LaunchedEffect(tableId) {
        reservationTableViewModel.tableViewModel.loadTableById(tableId)
    }

    val tableUiState by  reservationTableViewModel.tableViewModel.uiState.collectAsState()
    val reservationUiState by reservationTableViewModel.reservationViewModel.uiState.collectAsState()
    val selectedTable = tableUiState.selectedTable

    val context = LocalContext.current


    LaunchedEffect(selectedTable) {
        selectedTable?.let {
            reservationTableViewModel.tableViewModel.clearPreparedFiles()
            reservationTableViewModel.tableViewModel.prepareShareFiles(context, it.imageUrls)
        }
    }

    val isTableSelected = remember(tableUiState.selectedTables, selectedTable) {
        tableUiState.selectedTables.any { it.tableId == selectedTable?.tableId }
    }
    var availableSlots by remember { mutableStateOf<List<LocalTime>>(emptyList()) }


    val busyRanges by reservationTableViewModel.reservationViewModel.busyRanges.collectAsState()

    val isTableAvailableAtTime = remember(busyRanges, reservationUiState.selectedStartTime) {
        reservationTableViewModel.reservationViewModel.isReservationTimeValid(
            time = reservationUiState.selectedStartTime,
            date = reservationUiState.selectedDate ?: LocalDate.now(),
            duration = reservationUiState.selectedDurationMinutes
        )
    }


    Box(modifier = Modifier.fillMaxSize()) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {


            if (!(selectedTable?.imageUrls.isNullOrEmpty())) {
                ImageCarousel(imageUrls = selectedTable!!.imageUrls)
                Spacer(modifier = Modifier.height(12.dp))
            }else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No Image Available",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Red
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
            val tableDiameterMeters = 1.5f
            val tableRadiusMeters = tableDiameterMeters / 2

            val tableRadiusPx = selectedTable?.radius ?: 40f

            val pixelsPerMeter = tableRadiusPx / tableRadiusMeters

            val tableArea = PI * tableRadiusMeters.pow(2)

            val seatRadiusPx = 20f
            val chairDiameterMeters = (seatRadiusPx * 2) / pixelsPerMeter





            val date = reservationUiState.selectedDate ?: LocalDate.now()

            val userSelectDuration=reservationUiState.selectedDurationMinutes

            LaunchedEffect(selectedTable, date) {
                val currentTable = selectedTable
                if (currentTable != null) {

                    val slots = reservationTableViewModel.reservationViewModel.getAvailableTimeSlots(
                        date = date,
                        tableId = currentTable.tableId,
                        userSelectDuration

                    )
                    availableSlots = slots

                }
            }




            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text(
                    text = "Table ${selectedTable?.label ?: "-"}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text("Table ID: ${selectedTable?.tableId ?: "-"}")
                Text("Type of Table: Round Table")
                Text("Area of Table: ${"%.2f".format(tableArea)} m²")

                Text("Type of Chair: Standard Dining Chair")
                Text("Chair Width: ${"%.2f".format(chairDiameterMeters)} m")
                Text("Zone: ${selectedTable?.zone ?: "-"}")
                Text("Seats: ${selectedTable?.seatCount ?: "-"}")


//                Text("Status: ${selectedTable?.status ?: "-"}")
//                if (selectedTime.isNotEmpty()) {
//                    Text("Selected Time: $selectedTime", fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp))
//                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                selectedTable?.let { table ->
                    val nearRegions = reservationTableViewModel.tableViewModel.findNearRegions(table, tableUiState.regions)

                    Column(
                        Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val description = if (nearRegions.isNotEmpty()) {
                            val formattedRegions = when (nearRegions.size) {
                                1 -> nearRegions[0].replace("\n", " ")
                                2 -> nearRegions.joinToString(" and ") { it.replace("\n", " ") }
                                else -> nearRegions.dropLast(1).joinToString(", ") { it.replace("\n", " ") } +
                                        ", and " + nearRegions.last().replace("\n", " ")
                            }
                            "A ${table.zone} table near $formattedRegions"
                        } else {
                            "A ${table.zone} table"
                        }
                        Text("Description", fontWeight = FontWeight.Bold, fontSize = 18.sp)


                        Text(description)

                    }
                }
            }

//
//            Spacer(modifier = Modifier.height(12.dp))

//            // --- Amenities ---
//            DetailsCard(
//                title = "Amenities",
//                value = selectedTable?.amenities?.joinToString(", ") ?: "No amenities",
//                color = Color(0xFFBBDEFB)
//            )
//
//            Spacer(modifier = Modifier.height(12.dp))
//

//
//            Spacer(modifier = Modifier.height(12.dp))

//            // --- Estimated Wait Time ---
//            DetailsCard(
//                title = "Estimated Wait Time",
//                value = selectedTable?.estimatedWaitTime ?: "N/A",
//                color = Color(0xFFFFF9C4)
//            )


            Spacer(modifier = Modifier.height(12.dp))

            Spacer(modifier = Modifier.height(12.dp))
            if(!isReadOnly) {
                TableAvailabilitySection(
                    selectedDate = reservationUiState.selectedDate,
                    selectedTime = reservationUiState.selectedStartTime,
                    isTableAvailable = isTableAvailableAtTime,
                    availableSlots = availableSlots,
                    onTimeChanged = { newTime ->
                        reservationTableViewModel.reservationViewModel.updateStartTime(
                            newTime
                        )
                    }
                )
            }




        }

        if(!isReadOnly){
            Surface(
                modifier = Modifier.align(Alignment.BottomCenter),
                shadowElevation = 12.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Left Button
                    OutlinedButton(
                        onClick = { onNavigateUp() },
                        modifier = Modifier
                            .weight(1f) // Set to 1
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            selectedTable?.let {
                                reservationTableViewModel.tableViewModel.toggleTable(it)
                                onNavigateUp()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        enabled = isTableAvailableAtTime || isTableSelected,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isTableSelected) Color(0xFFC62828) else Color(0xFF2E7D32)
                        )
                    ) {
                        Text(
                            text = if (isTableSelected) "Remove" else "Select",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }
            }

        }

    }
}
@Composable
fun TableAvailabilitySection(
    selectedDate:LocalDate,
    selectedTime: LocalTime,
    isTableAvailable: Boolean,
    availableSlots: List<LocalTime>,
    onTimeChanged: (LocalTime) -> Unit
) {
    var showClockDialog by remember { mutableStateOf(false) }
    val formatter = DateTimeFormatter.ofPattern("hh:mm a")

    Column(modifier = Modifier


        .padding(bottom = 50.dp)) {
        Text("Available Time Slots", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)


        if (isTableAvailable) {
            // ... (Keep your existing "Available" Card code here)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Check, null, tint = Color(0xFF2E7D32))
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Available at ${selectedTime.format(formatter)}", fontWeight = FontWeight.Bold)
                        Text("Table is free for your stay", style = MaterialTheme.typography.labelSmall)
                    }
                    TextButton(onClick = { showClockDialog = true }) { Text("Edit Time") }
                }
            }
        } else {

            val dateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")

            if (availableSlots.isEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                ) {
                    Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red)
                        Text(
                            text = "Fully Booked on ${selectedDate.format(dateFormatter)}",
                            fontWeight = FontWeight.Bold,
                            color = Color.Red
                        )
                        Text(
                            text = "No available time slots for this table on the "+selectedDate+". Please try another day.",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            } else {

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Unavailable at ${selectedTime.format(formatter)}",
                                fontWeight = FontWeight.Bold,
                                color = Color.Red,
                                modifier = Modifier.weight(1f),
                                fontSize = 13.sp
                            )
                            Button(
                                onClick = { showClockDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Text(text = "Change Time", fontSize = 12.sp)
                            }
                        }

                        Text(
                            "Try these available times instead:",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 8.dp)
                        )

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            availableSlots.take(4).forEach { slot ->
                                SuggestionChip(
                                    onClick = { onTimeChanged(slot) },
                                    label = { Text(slot.format(formatter)) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showClockDialog) {
        // Here you call the TimePicker Dialog logic we created earlier
        TimeSlotPickerDialog(
            initialTime = selectedTime,
            onTimeSelected = { onTimeChanged(it); showClockDialog = false },
            onDismiss = { showClockDialog = false }
        )
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeSlotPickerDialog(
    initialTime: LocalTime,
    onTimeSelected: (LocalTime) -> Unit,
    onDismiss: () -> Unit
) {
    // State to hold the clock position
    val timePickerState = rememberTimePickerState(
        initialHour = initialTime.hour,
        initialMinute = initialTime.minute,
        is24Hour = false // Shows AM/PM selector
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false), // Allows better sizing
        confirmButton = {
            TextButton(onClick = {
                val newTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                onTimeSelected(newTime)
            }) {
                Text("Confirm", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        title = {
            Text(
                text = "Select Reservation Time",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // The actual Material 3 Clock UI
                TimePicker(state = timePickerState)

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Pick the exact time you plan to arrive.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    )
}

@Composable
fun ImageCarousel(imageUrls: List<String>) {
    var localPreviewIndex by remember { mutableStateOf(-1) }
    var localPreviewImages by remember { mutableStateOf<List<String>>(emptyList()) }

    val listState = rememberLazyListState()
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    val visibleIndex by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val viewportCenter = layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset / 2
            layoutInfo.visibleItemsInfo.minByOrNull { item ->
                val itemCenter = item.offset + item.size / 2
                abs(itemCenter - viewportCenter)
            }?.index ?: 0
        }
    }

    val context = LocalContext.current
    val imageLoader = remember {
        ImageLoader.Builder(context)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .build()
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        LazyRow(
            state = listState,
            flingBehavior = flingBehavior,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(8.dp)
        ) {
            items(imageUrls.size) { index ->
                val painter = rememberAsyncImagePainter(
                    model = imageUrls[index],
                    imageLoader = imageLoader
                )

                val state = painter.state

                Box(
                    modifier = Modifier
                        .size(355.dp)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            localPreviewImages = imageUrls
                            localPreviewIndex = index
                        }
                ) {
                    Image(
                        painter = painter,
                        contentDescription = "Image $index",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    if (state is AsyncImagePainter.State.Error) {
                        Text(
                            text = "Failed to load image",
                            color = Color.Red,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }
        }
    }

    if (localPreviewIndex != -1 && localPreviewImages.isNotEmpty()) {
        PreviewImageDialog(
            selectedImages = localPreviewImages.map { it.toUri() },
            previewIndex = localPreviewIndex,
            onDismiss = { localPreviewIndex = -1 }
        )
    }

    if (imageUrls.isNotEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${visibleIndex + 1} / ${imageUrls.size}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun PreviewImageDialog(
    selectedImages: List<android.net.Uri>,
    previewIndex: Int,
    onDismiss: () -> Unit
) {
    if (previewIndex in selectedImages.indices) {
        Dialog(onDismissRequest = onDismiss) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .clickable { onDismiss() },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = rememberAsyncImagePainter(selectedImages[previewIndex]),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.8f)
                )
            }
        }
    }
}

