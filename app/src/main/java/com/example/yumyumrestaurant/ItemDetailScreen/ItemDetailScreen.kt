package com.example.yumyumrestaurant.ItemDetailScreen

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
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

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

import androidx.compose.ui.draw.clip

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.net.toUri
import coil.ImageLoader
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.CachePolicy
import kotlin.math.abs


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailScreen(
    tableId: String,
    tableViewModel: TableViewModel,
    reservationViewModel: ReservationViewModel,
    onNavigateUp: () -> Unit
) {
    // Load table data
    LaunchedEffect(tableId) {
        tableViewModel.loadTableById(tableId)
    }

    val tableUiState by tableViewModel.uiState.collectAsState()
    val reservationUiState by reservationViewModel.uiState.collectAsState()
    val selectedTable = tableUiState.selectedTable







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

            var availableSlots by remember { mutableStateOf<List<LocalTime>>(emptyList()) }
            var selectedTime by remember { mutableStateOf("") }



            val date = reservationUiState.selectedDate ?: LocalDate.now()
            val requiredDuration = 15

            LaunchedEffect(selectedTable, date, requiredDuration) {
                val currentTable = selectedTable
                if (currentTable != null) {

                    val slots = reservationViewModel.getAvailableTimeSlots(
                        date = date,
                        tableId = currentTable.tableId,
                        requiredDurationMinutes = requiredDuration
                    )
                    availableSlots = slots

                }
            }






            val price = selectedTable?.seatCount?.times(10) ?: 0

            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text(
                    text = "Table ${selectedTable?.label ?: "-"}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text("Table: ${selectedTable?.tableId ?: "-"}")
                Text("Type of Table: Round Table²")
                Text("Area of Table: ${"%.2f".format(tableArea)} m²")

                Text("Type of Chair: Standard Dining Chair")
                Text("Chair Width: ${"%.2f".format(chairDiameterMeters)} m")
                Text("Zone: ${selectedTable?.zone ?: "-"}")
                Text("Seats: ${selectedTable?.seatCount ?: "-"}")
                Text("Price: RM 10.00 per seat")
                Text("Total Price: RM ${"%.2f".format(price.toFloat())}")
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
                    val nearRegions = tableViewModel.findNearRegions(table, tableUiState.regions)

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
//                        Indoor table near the window.

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

            TimeSlotSection(
                timeSlots = availableSlots,
                onTimeSelected = { selectedTime = it }
            )


//            Shade Umbrella,Power Outlet,Adjustable Chairs
            // --- Back Button ---
            Button(
                onClick = { onNavigateUp() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Back",
                    fontSize = 20.sp
                )
            }
        }
    }
}

@Composable

fun TimeSlotSection(
    timeSlots: List<LocalTime>,
    onTimeSelected: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Text("Available Time Slots", fontWeight = FontWeight.Bold, fontSize = 18.sp)

        Spacer(modifier = Modifier.height(8.dp))
        if(timeSlots.isEmpty()){
            Text("No available slots")
        }else{
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(timeSlots) { slot ->
                    Button(
                        onClick = { onTimeSelected(slot.toString()) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBBDEFB))
                    ) {
                        Text(slot.toString())
                    }
                }
            }
        }

    }
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
