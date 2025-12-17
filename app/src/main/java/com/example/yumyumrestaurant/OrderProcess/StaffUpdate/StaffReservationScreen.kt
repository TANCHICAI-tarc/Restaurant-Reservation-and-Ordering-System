package com.example.yumyumrestaurant.OrderProcess.StaffUpdate

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/*@Composable
fun StaffReservationScreen(
    viewModel: StaffBookingViewModel,
    onReservationClick: (String) -> Unit
) {
    var reservationToDelete by remember { mutableStateOf<String?>(null) }

    Box {
        LazyColumn {
            items(reservations) { reservation ->
                ManagementCard(
                    title = stringResource(R.string.booking_no, reservation.bookingNo),
                    subtitle = reservation.userName,
                    description = reservation.roomType,
                    status = reservation.bookingStatus,
                    onClick = { onReservationClick(reservation.id) },
                    onRemoveClick = { reservationToDelete = reservation.id }
                )
            }
        }

        if (reservationToDelete != null) {
            AlertDialog(
                onDismissRequest = { reservationToDelete = null },
                title = { Text("Remove Reservation") },
                text = { Text("Are you sure you want to remove this reservation?") },
                confirmButton = {
                    TextButton(onClick = {
                        reservationToDelete?.let { viewModel.removeReservation(it) }
                        reservationToDelete = null
                    }) {
                        Text("Remove")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { reservationToDelete = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }

}

@Composable
fun ManagementCard(
    title: String,
    subtitle: String,
    description: String,
    status: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onRemoveClick: () -> Unit
) {
    val statusColor = when (status) {
        "Confirmed" -> Color.Green
        "Arrived" -> Color.Blue
        "Cancelled" -> Color.Red
        else -> Color.Gray
    }
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick() },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    title,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(subtitle)
                    Text(description)
                }

            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .background(statusColor, RoundedCornerShape(50))
                        .padding(
                            horizontal = 12.dp,
                            vertical = 4.dp
                        )
                ) {
                    Text(
                        text = status,
                        color = Color.White
                    )
                }
                IconButton(onClick = onRemoveClick) {
                    Icon(Icons.Default.Delete, contentDescription = "Remove")
                }
            }

        }
    }
}*/