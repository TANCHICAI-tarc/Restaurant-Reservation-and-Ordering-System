package com.example.yumyumrestaurant.StaffUpdate

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.yumyumrestaurant.OrderProcess.OrderedMenuUi
import com.example.yumyumrestaurant.data.CustomerEntity
import com.example.yumyumrestaurant.data.Order.Payment
import com.example.yumyumrestaurant.data.ReservationData.ReservationEntity
import com.example.yumyumrestaurant.data.TableData.TableEntity
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun StaffDetailsScreen(
    reservationID: String,
    viewModel: StaffOperationViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    var showReserveBottomSheet by remember { mutableStateOf(false) }
    var tempReserveStatus by remember { mutableStateOf<String?>(null) }
    var showDialog by remember { mutableStateOf(false) }

    val reservation by viewModel.selectedReservation.collectAsState()
    val order by viewModel.selectedOrder.collectAsState()
    val payment by viewModel.selectedPayment.collectAsState()
    val tables by viewModel.reservedTables.collectAsState()
    val menuItems by viewModel.orderedMenus.collectAsState()
    val customer by viewModel.selectedCustomer.collectAsState()

    LaunchedEffect(reservationID, reservation) {
        if (reservation?.reservationId != reservationID) {
            viewModel.selectReservation(reservationID)
        }
    }

    LaunchedEffect(reservation) {
        reservation?.let { res ->
            tempReserveStatus = res.reservationStatus
        }
    }

    LaunchedEffect(viewModel.toastMessage) {
        viewModel.toastMessage.collectLatest { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }


    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        reservation?.let { res ->
            LazyColumn(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    ReservationHeader(
                        reservationId = res.reservationId,
                        status = tempReserveStatus ?: res.reservationStatus,
                        onStatusClick = {
                            showReserveBottomSheet = true
                        }
                    )
                }
                item {
                    TableReservationCard(
                        reservation = res,
                        tables = tables
                    )
                }

                item {
                    Log.d("GuestCard", "Customer: $customer")
                    customer?.let { guest ->
                        GuestDetailsCard(
                            guest = guest,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                item {
                    MenuOrdersCard(menuItems)
                }

                item {
                    PaymentCard(payment)
                }

                item {
                    Button(
                        onClick = {
                            if (tempReserveStatus != res.reservationStatus) {
                                showDialog = true
                            } else {
                                Toast.makeText(
                                    context,
                                    "No changes to save",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Text("Save Changes")
                    }
                }
            }


            if (showReserveBottomSheet) {
                ModalBottomSheet(onDismissRequest = { showReserveBottomSheet = false }) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Change Reservation Status", style = MaterialTheme.typography.titleMedium)

                        listOf("Confirmed", "Completed", "Cancelled").forEach { status ->
                            Text(
                                text = status,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        tempReserveStatus = status
                                        showReserveBottomSheet = false
                                    }
                                    .padding(vertical = 12.dp)
                            )
                        }
                    }
                }
            }

            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text("Confirm Changes") },
                    text = { Text("Are you sure you want to make this changes?") },
                    confirmButton = {
                        TextButton(onClick = {
                            tempReserveStatus?.let { status ->
                                viewModel.updateReservationStatus(res.reservationId, status)
                            }
                            showDialog = false
                        }) {
                            Text("Yes")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        } ?: run {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
fun ReservationHeader(
    reservationId: String,
    status: String,
    onStatusClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically, // Aligns items vertically in the middle
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = reservationId,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            StatusChip(
                status = status,
                onClick = onStatusClick
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TableReservationCard(
    reservation: ReservationEntity,
    tables: List<TableEntity>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Text(
                text = "Table Reservation",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))

            Text("Guests: ${reservation.guestCount}")
            Text("Date: ${formatDate(reservation.date)}")
            Text("Time: ${formatTimeRange(reservation.startTime, reservation.endTime)}")

            Spacer(Modifier.height(12.dp))

            Text(
                text = "Reserved Tables",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(8.dp))

            if (tables.isEmpty()) {
                Text(
                    text = "No table assigned",
                    color = Color.Gray
                )
            } else {
                tables.forEachIndexed { index, table ->

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {

                            Text(
                                text = "Table ${table.label}",
                                fontWeight = FontWeight.SemiBold
                            )

                            Spacer(Modifier.height(4.dp))

                            Text("Seats: ${table.seatCount}")
                            Text("Zone: ${table.zone}")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MenuOrdersCard(
    orderedMenus: List<OrderedMenuUi>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {

        Column(modifier = Modifier.padding(16.dp)) {

            Text(
                "Menu Orders",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))

            if (orderedMenus.isEmpty()) {
                Text("No menu ordered", color = Color.Gray)
            } else {
                orderedMenus.forEach {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("${it.foodName} × ${it.quantity}")
                        Text("RM %.2f".format(it.price * it.quantity))
                    }

                    if (it.remark.isNotBlank()) {
                        Text(
                            "Remark: ${it.remark}",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }

                    Spacer(Modifier.height(6.dp))
                }
            }
        }
    }
}

@Composable
fun GuestDetailsCard(
    guest: CustomerEntity,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Card(
        modifier = modifier
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Guest Details",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))

            // Guest Name
            Text("Name: ${guest.name}")

            Spacer(Modifier.height(8.dp))

            // Email row with button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Email: ${guest.email}", modifier = Modifier.weight(1f))
                IconButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:${guest.email}")
                        }
                        context.startActivity(intent)
                    },
                    modifier = Modifier.size(36.dp) // reduce size for uniformity
                ) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "Send Email",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Phone row with button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Phone: ${guest.phoneNum}", modifier = Modifier.weight(1f))
                IconButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:${guest.phoneNum}")
                        }
                        context.startActivity(intent)
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Call",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PaymentCard(
    payment: Payment?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Text(
                "Payment",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))

            if (payment == null) {
                Text("Payment not made", color = Color.Gray)
            } else {
                Text("Method: ${payment.paymentMethod}")
                Text("Amount Paid: RM %.2f".format(payment.amountPaid))
                Text("Date: ${formatTimestamp(payment.paymentDate)}")
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun formatDate(date: String): String {
    return try {
        val input = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val output = DateTimeFormatter.ofPattern("dd MMM yyyy")
        LocalDate.parse(date, input).format(output)
    } catch (e: Exception) {
        date
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun formatTimeRange(start: String, end: String): String {
    return try {
        val input = DateTimeFormatter.ofPattern("HH:mm[:ss[.SSSSSS]]")
        val output = DateTimeFormatter.ofPattern("hh:mm a")

        val startTime = LocalTime.parse(start, input).format(output)
        val endTime = LocalTime.parse(end, input).format(output)

        "$startTime - $endTime"
    } catch (e: Exception) {
        "$start - $end"
    }
}

fun formatTimestamp(timestamp: Timestamp?): String {
    return if (timestamp != null) {
        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        sdf.format(timestamp.toDate())
    } else {
        "N/A"
    }
}

@Composable
fun StatusChip(
    status: String,
    onClick: () -> Unit = {}
) {
    val color = when (status) {
        "Confirmed" -> Color.Green
        "Completed" -> Color.Blue
        "Cancelled" -> Color.Red
        else -> Color.Gray
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .background(color, RoundedCornerShape(50))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(status, color = Color.White)
    }
}
