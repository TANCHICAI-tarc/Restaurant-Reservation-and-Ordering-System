package com.example.yumyumrestaurant.Reservation

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.yumyumrestaurant.OrderProcess.OrderUiState
import com.example.yumyumrestaurant.OrderProcess.OrderViewModel
import com.example.yumyumrestaurant.OrderProcess.OrderedMenuUi
import com.example.yumyumrestaurant.ReservationTable.ReservationTableViewModel
import com.example.yumyumrestaurant.StaffUpdate.StaffOperationViewModel
import com.example.yumyumrestaurant.StatusBadge
import com.example.yumyumrestaurant.TableDetailScreen.ImageCarousel
import com.example.yumyumrestaurant.data.TableData.TableEntity
import com.example.yumyumrestaurant.data.ReservationData.ReservationEntity
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationDetailScreen(
    reservationId: String,
    reservationTableViewModel: ReservationTableViewModel,
    staffViewModel: StaffOperationViewModel,
    orderViewModel: OrderViewModel,
    onNavigateToTableDetails: (String) -> Unit,
    onNavigateUp: () -> Unit
) {

    LaunchedEffect(reservationId) {
        reservationTableViewModel.loadReservationDataById(reservationId)

    }
    LaunchedEffect(reservationId) {
        staffViewModel.selectReservation(reservationId)
    }
    val orderedMenus by staffViewModel.orderedMenus.collectAsState()
    val selectedOrder by staffViewModel.selectedOrder.collectAsState()
    val resState by reservationTableViewModel.reservationUiState.collectAsState()
    val resTableState by reservationTableViewModel.reservationTableUiState.collectAsState()
    val orderState by orderViewModel.orderUiState.collectAsState()
    val subTotal = orderedMenus.sumOf { it.price * it.quantity }
    val tax = subTotal * 0.06
    val total = subTotal + tax
    val dateFormatter = DateTimeFormatter.ofPattern("EEE, MMM d, yyyy")
    val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a")

    val capturedFilesRaw =resTableState.capturedFiles
    val capturedFiles = remember(capturedFilesRaw) {
        capturedFilesRaw.map { it.absolutePath }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Reservation Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF8F8F8))
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))


            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Reservation #$reservationId",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Gray
                )
                StatusBadge(status = resState.reservation?.reservationStatus ?: "Loading...")
            }

            Spacer(modifier = Modifier.height(16.dp))


            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(2.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    InfoRow(Icons.Default.DateRange, "Date", resState.selectedDate?.format(dateFormatter) ?: "N/A")
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp)
                    InfoRow(Icons.Default.Timer, "Time", "${resState.selectedStartTime?.format(timeFormatter)} - ${resState.selectedStartTime?.plusMinutes(resState.selectedDurationMinutes.toLong())?.format(timeFormatter)}")
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp)
                    InfoRow(Icons.Default.Person, "Guests", "${resState.guestCount} People")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))


            if (orderedMenus.isNotEmpty()) {
                ReceiptSummarySection(
                    orderedItems = orderedMenus,
                    subTotal = subTotal,
                    taxAmount = tax,
                    totalPrice = total
                )
            } else {
                EmptyStateBox("No items ordered for this reservation")
            }

            Spacer(modifier = Modifier.height(24.dp))


            Text(
                text = "Table Location Map",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (capturedFiles.isNotEmpty()) {
                ImageCarousel(imageUrls = capturedFiles)
            } else {
                EmptyStateBox("Map view not available")
            }

            Spacer(modifier = Modifier.height(24.dp))


            Text(
                text = "Assigned Tables",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            resTableState.selectedTables.forEach { table ->
                TableDetailItem(
                    table = table,
                    onNavigate = { id -> onNavigateToTableDetails(id) }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun ReceiptSummarySection(
    orderedItems: List<OrderedMenuUi>,
    subTotal: Double,
    taxAmount: Double,
    totalPrice: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Order Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (orderedItems.isEmpty()) {
                Text(
                    text = "No food items pre-ordered.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                orderedItems.forEach { item ->
                    Column(modifier = Modifier.padding(vertical = 6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "${item.foodName} x${item.quantity}",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "RM %.2f".format(item.price * item.quantity),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        if (!item.remark.isNullOrBlank()) {
                            Text(
                                text = "Note: ${item.remark}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp)

                // Cost Breakdown
                SummaryRow("Subtotal", "RM %.2f".format(subTotal))
                SummaryRow("Service Tax (6%)", "RM %.2f".format(taxAmount))

                Spacer(modifier = Modifier.height(12.dp))

                // Final Total
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Total Paid",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "RM %.2f".format(totalPrice),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun EmptyStateBox(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFEEEEEE)),
        contentAlignment = Alignment.Center
    ) {
        Text(text = message, color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun TableDetailItem(table: TableEntity, onNavigate: (String) -> Unit) {
    Card(
        onClick = { onNavigate(table.tableId) },
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(0.5.dp, Color.LightGray)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Restaurant, null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text("Table ${table.label}", fontWeight = FontWeight.Bold)
                Text("${table.seatCount} Seats • ${table.zone}", style = MaterialTheme.typography.labelSmall)
            }
            Icon(Icons.Default.ChevronRight, null, tint = Color.Gray)
        }
    }
}