package com.example.yumyumrestaurant.Reservation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.yumyumrestaurant.OrderProcess.OrderViewModel
import com.example.yumyumrestaurant.ReservationTable.ReservationTableViewModel
import com.example.yumyumrestaurant.TableDetailScreen.ImageCarousel
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationConfirmationScreen(
    reservationId: String,
    reservationTableViewModel: ReservationTableViewModel,
    onNavigateToTableDetails: (String) -> Unit,
    onConfirmed: () -> Unit = {}
) {
    val resState by reservationTableViewModel.reservationUiState.collectAsState()


    val dateFormatter = DateTimeFormatter.ofPattern("EEE, MMM d, yyyy")
    val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a")
    val capturedFilesRaw by reservationTableViewModel.capturedFiles.collectAsState()
    val capturedFiles = remember(capturedFilesRaw) {
        capturedFilesRaw.map { it.absolutePath }
    }
    Column(modifier = Modifier.fillMaxSize()) {

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {


            Spacer(modifier = Modifier.height(16.dp))


            Text(
                text = "Review Your Reservation",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Reservation ID: #$reservationId",
                color = Color.Gray,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(24.dp))


            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    InfoRow(Icons.Default.DateRange, "Date", resState.selectedDate?.format(dateFormatter) ?: "N/A")
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                    InfoRow(Icons.Default.Timer, "Time", "${resState.selectedStartTime?.format(timeFormatter)} - ${resState.selectedStartTime.plusMinutes(resState.selectedDurationMinutes.toLong())?.format(timeFormatter)}")
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                    InfoRow(Icons.Default.Person, "Guests", "${resState.guestCount} People")
                }
            }
            Spacer(modifier = Modifier.height(24.dp))


            Text(
                text = "Table Location Map",
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            if (!(capturedFiles.isNullOrEmpty())) {

                ImageCarousel(imageUrls = capturedFiles)
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




            Text(
                text = "Selected Tables",
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))



            resState.selectedTables.forEach { table ->
                Card(
                    // Use the onClick parameter of the Card
                    onClick = { onNavigateToTableDetails(table.tableId) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    elevation = CardDefaults.cardElevation(2.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column {

                        if (!table.imageUrls.isNullOrEmpty()) {
                            ImageCarousel(
                                imageUrls = table.imageUrls

                            )
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

                        // Text Section
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "Table ${table.label}", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Text(text = "Tap to view full details", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall)
                            }


                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text(
                                    text = "${table.seatCount} Seats",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }


                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }


        Surface(
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 12.dp,
            tonalElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {




                Button(
                    onClick = {
                        onConfirmed()
                    },
                    modifier = Modifier
                        .weight(1f)

                ) {
                    Text(
                        text = "Confirm & Order Now",
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 18.sp,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun GlobalContinueReservationBar(
    viewModel: ReservationTableViewModel,
    onContinue: () -> Unit
) {
    val resState by viewModel.reservationUiState.collectAsState()

    // Wrap in a Surface to provide elevation and background
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp) // Gap from the edges of the screen
            .clickable { onContinue() },
        shape = RoundedCornerShape(16.dp), // More rounded for a "floating" look
        color = MaterialTheme.colorScheme.primaryContainer,
        shadowElevation = 8.dp,
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Restaurant,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Reservation in Progress",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp
                )
                Text(
                    text = "${resState.selectedTables.size} tables • ${resState.selectedStartTime}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.DarkGray
                )
            }

            Text(
                text = "Continue",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 8.dp)
            )
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}