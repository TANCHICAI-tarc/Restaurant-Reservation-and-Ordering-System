package com.example.yumyumrestaurant

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.example.yumyumrestaurant.data.ReservationData.ReservationEntity

@Composable
fun UserViewReservationScreen(
//    navController: NavHostController
) {
    val defaultReservations = listOf(
        ReservationEntity(
            reservationId = "R001",
            date = "2025-12-20",
            startTime = "18:00",
            endTime = "18:30",
            durationMinutes = 30,
            guestCount = 2,
            zone = "INDOOR",
            reservationStatus = "CONFIRMED",
            customerId = "user1"
        ),
        ReservationEntity(
            reservationId = "R002",
            date = "2025-12-22",
            startTime = "19:00",
            endTime = "19:15",
            durationMinutes = 15,
            guestCount = 4,
            zone = "OUTDOOR",
            reservationStatus = "PENDING",
            customerId = "user1"
        ),
        ReservationEntity(
            reservationId = "R003",
            date = "2025-12-10",
            startTime = "17:00",
            endTime = "17:45",
            durationMinutes = 45,
            guestCount = 6,
            zone = "INDOOR",
            reservationStatus = "COMPLETED",
            customerId = "user1"
        )
    )


    var selectedTab by remember { mutableStateOf(0) }

    val activeReservations = defaultReservations.filter {
        it.reservationStatus == "Pending" || it.reservationStatus == "CONFIRMED"
    }

    val historyReservations = defaultReservations.filter {
        it.reservationStatus == "Completed" || it.reservationStatus == "Cancelled"
    }

    val reservationsShown =
        if (selectedTab == 0) activeReservations else historyReservations

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .statusBarsPadding()
    ) {

        Text(
            text = "My Reservation",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(12.dp))

        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Text(
                        text = "CONFIRMED",
                        fontSize = 11.sp
                    )
                }
            )

            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Text(
                        text = "COMPLETED",
                        fontSize = 11.sp
                    )
                }
            )

            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = {
                    Text(
                        text = "CANCELLED",
                        fontSize = 11.sp
                    )
                }
            )

        }

        Spacer(modifier = Modifier.height(12.dp))

        if (reservationsShown.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No reservation")
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(reservationsShown) { reservation ->
                    ReservationCard(reservation)
                }
            }
        }
    }
}


@Composable
fun ReservationCard(
    reservation: ReservationEntity,
    onCancelClick: (ReservationEntity) -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {

            Text(
                text = "Reservation ${reservation.reservationId}",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text("Date: ${reservation.date}")
            Text("Time: ${reservation.startTime} - ${reservation.endTime}")
            Text("Guests: ${reservation.guestCount}")
            Text("Zone: ${reservation.zone}")

            Spacer(modifier = Modifier.height(10.dp))

            // Status badge
            val statusColor = when (reservation.reservationStatus) {
                "CONFIRMED" -> Color(0xFF4CAF50)
                "PENDING" -> Color(0xFFFFC107)
                "COMPLETED" -> Color(0xFF2196F3)
                "CANCELLED" -> Color(0xFFF44336)
                else -> Color.Gray
            }

            Box(
                modifier = Modifier
                    .align(Alignment.End)
                    .background(statusColor, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = reservation.reservationStatus,
                    color = Color.White,
                    fontSize = 12.sp
                )
            }

            // ✅ Cancel button ONLY for confirmed / pending
            if (
                reservation.reservationStatus == "CONFIRMED" ||
                reservation.reservationStatus == "PENDING"
            ) {
                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { onCancelClick(reservation) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF44336)
                    )
                ) {
                    Text(
                        text = "Cancel Reservation",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}



