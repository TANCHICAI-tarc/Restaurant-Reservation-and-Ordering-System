package com.example.yumyumrestaurant.ItemDetailScreen

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.yumyumrestaurant.TableSelectionScreen.TableViewModel
import kotlin.math.PI
import kotlin.math.pow


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailScreen(
    tableId: String,
    tableViewModel: TableViewModel,
    onNavigateUp: () -> Unit
) {
    // Load table data
    LaunchedEffect(tableId) {
        tableViewModel.loadTableById(tableId)
    }

    val tableUiState by tableViewModel.uiState.collectAsState()
    val selectedTable = tableUiState.selectedTable

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {


            val tableDiameterMeters = 1.5f
            val tableRadiusMeters = tableDiameterMeters / 2

            val tableRadiusPx = selectedTable?.radius ?: 40f

            val pixelsPerMeter = tableRadiusPx / tableRadiusMeters

            val tableArea = PI * tableRadiusMeters.pow(2)

            val seatRadiusPx = 20f
            val chairDiameterMeters = (seatRadiusPx * 2) / pixelsPerMeter

//            val availableSlots by remember { mutableStateOf(
//                tableViewModel.getAvailableTimeSlots(tableId)
//            ) }
            var selectedTime by remember { mutableStateOf("") }

//            TimeSlotSection(
//                timeSlots = availableSlots,
//                onTimeSelected = { selectedTime = it }
//            )



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
                if (selectedTime.isNotEmpty()) {
                    Text("Selected Time: $selectedTime", fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp))
                }
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
    timeSlots: List<String>,
    onTimeSelected: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Text("Available Time Slots", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(timeSlots) { slot ->
                Button(
                    onClick = { onTimeSelected(slot) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBBDEFB))
                ) {
                    Text(slot)
                }
            }
        }
    }
}

