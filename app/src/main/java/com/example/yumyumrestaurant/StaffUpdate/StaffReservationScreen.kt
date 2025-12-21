package com.example.yumyumrestaurant.StaffUpdate

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun StaffReservationScreen(
    viewModel: StaffOperationViewModel,
    onReservationClick: (String) -> Unit
) {
    val reservations by viewModel.filteredReservations.collectAsState()
    var reservationToDelete by remember { mutableStateOf<String?>(null) }

    Box(modifier = Modifier.fillMaxWidth()) {
        LazyColumn {
            items(reservations) { reservation ->
                ManagementCard(
                    title = reservation.reservationId,
                    subtitle = reservation.date,
                    status = reservation.reservationStatus,
                    onClick = { onReservationClick(reservation.reservationId) },
                    onRemoveClick = { reservationToDelete = reservation.reservationId }
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
    status: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onRemoveClick: () -> Unit
) {
    val statusColor = when (status) {
        "Confirmed" -> Color.Green
        "Completed" -> Color.Blue
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
}