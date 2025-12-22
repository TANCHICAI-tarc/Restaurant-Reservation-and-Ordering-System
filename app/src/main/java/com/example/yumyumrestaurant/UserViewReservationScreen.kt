package com.example.yumyumrestaurant

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController

import com.example.yumyumrestaurant.Reservation.ReservationViewModel

import com.example.yumyumrestaurant.data.ReservationData.ReservationEntity
import com.example.yumyumrestaurant.shareFilterScreen.AppliedFiltersSummary
import com.example.yumyumrestaurant.shareFilterScreen.SharedFilterSection
import com.example.yumyumrestaurant.shareFilterScreen.SharedFilterViewModel
import com.example.yumyumrestaurant.ui.UserViewModel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private val DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM d, yyyy")


private val TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserViewReservationScreen(
    navController: NavHostController,
    userViewModel: UserViewModel,
    modifier: Modifier,
    sharedFilterViewModel: SharedFilterViewModel,
    onViewDetail: (String) -> Unit,
    reservationViewModel: ReservationViewModel=viewModel()
) {

    val userData by userViewModel.userData.collectAsState()
    val currentUserId = userData?.userId
    val reservationUiState by reservationViewModel.uiState.collectAsState()
    LaunchedEffect(reservationUiState.reservations) {
        sharedFilterViewModel.setAllReservationData(reservationUiState.reservations)
    }

    val sharedUiState by sharedFilterViewModel.uiState.collectAsState()
    val filteredReservationData by sharedFilterViewModel.filteredReservationData.collectAsState()



    Column(
        modifier = modifier
            .fillMaxSize()


    ) {
        SharedFilterSection(
            viewModel = sharedFilterViewModel,
            title = "Filters",
            onFilterApplied = {
            sharedFilterViewModel.performSearch()
            }
        )


        val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a")
        val filters = mapOf(

            "Location" to sharedUiState.selectedLocations,

            "Reservation Date" to listOf(sharedUiState.reservationDate?.format(dateFormatter) ?: "All"),
            "Reservation Made Date" to listOf(sharedUiState.reservationMadeDate?.format(dateFormatter) ?: "All"),

            "Start Time" to listOf(sharedUiState.startTime?.format(timeFormatter) ?: "All"),
            "End Time" to listOf(sharedUiState.endTime?.format(timeFormatter) ?: "All"),
        )

        AppliedFiltersSummary(
            filters = filters,
            onRemoveFilter = { type, value ->
                sharedFilterViewModel.removeFilter(type, value)
            },
            onClearAll = { sharedFilterViewModel.showClearFiltersConfirmationDialog(true) }
        )




        var selectedTab by remember { mutableStateOf(0) }

        val confirmedReservations = filteredReservationData.filter {
            it.userId == currentUserId && it.reservationStatus == "Confirmed"
        }

        val completedReservations = filteredReservationData.filter {
            it.userId == currentUserId && it.reservationStatus == "Completed"
        }

        val cancelledReservations = filteredReservationData.filter {
            it.userId == currentUserId && it.reservationStatus == "Cancelled"
        }


        val reservationsShown = when (selectedTab) {
            0 -> confirmedReservations
            1 -> completedReservations
            2 -> cancelledReservations
            else -> emptyList()
        }


        Column(
            modifier = Modifier
                .fillMaxSize()


        ) {


            TabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    modifier = Modifier.weight(1f),
                    text = { Text("CONFIRMED", fontSize = 12.sp, textAlign = TextAlign.Center) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    modifier = Modifier.weight(1f),
                    text = { Text("COMPLETED", fontSize = 12.sp, textAlign = TextAlign.Center) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    modifier = Modifier.weight(1f),
                    text = { Text("CANCELLED", fontSize = 12.sp, textAlign = TextAlign.Center) }
                )
            }


            Spacer(modifier = Modifier.height(8.dp))



            if (reservationsShown.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No reservations")
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(reservationsShown) { reservation ->
                        ReservationCard(
                            reservation = reservation,
                            onViewDetail = { onViewDetail(it.reservationId) },
                            onCancelClick = { res ->
                                reservationViewModel.changeReservationStatus(
                                    reservation = res,
                                    status = "Cancelled"
                                )
                            }
                        )
                    }
                }
            }
        }
    }

}

@Composable
fun ReservationCard(
    reservation: ReservationEntity,
    onViewDetail: (ReservationEntity) -> Unit = {},

    onCancelClick: (ReservationEntity) -> Unit = {}
) {

    val formattedDate = try {
        LocalDate.parse(reservation.date).format(DATE_FORMATTER)
    } catch (e: Exception) { reservation.date }

    val formattedTime = try {
        val start = LocalTime.parse(reservation.startTime).format(TIME_FORMATTER)
        val end = LocalTime.parse(reservation.endTime).format(TIME_FORMATTER)
        "$start - $end"
    } catch (e: Exception) { "${reservation.startTime} - ${reservation.endTime}" }

    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val createdOn = try {

        LocalDate.parse(reservation.reservationMadeDate)
            .format(dateFormatter)
    } catch (e: Exception) { reservation.reservationMadeDate }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)) {
            // --- UPDATED HEADER ROW ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Reservation #${reservation.reservationId}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    StatusBadge(status = reservation.reservationStatus)
                }



            }

            Spacer(modifier = Modifier.height(12.dp))


            InfoRow(icon = Icons.Default.CalendarMonth, text = formattedDate)
            InfoRow(icon = Icons.Default.Schedule, text = formattedTime)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {

                Row(verticalAlignment = Alignment.CenterVertically) {
                    InfoRow(icon = Icons.Default.Groups, text = "${reservation.guestCount} Guests")
                    Spacer(modifier = Modifier.width(16.dp))
                    ZoneBadge(zone = reservation.zone)
                }

                Spacer(modifier = Modifier.height(8.dp))


                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // This spacer fills all available space on the left
                    Spacer(modifier = Modifier.weight(1f))

                    TextButton(
                        onClick = { onViewDetail(reservation) },
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Text("View Detail", fontSize = 12.sp)
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }


                if (reservation.reservationStatus.uppercase() == "CONFIRMED") {
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { onCancelClick(reservation) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Cancel Reservation", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val (color, label) = when (status.uppercase()) {
        "CONFIRMED" -> Color(0xFF4CAF50) to "Confirmed"
        "COMPLETED" -> Color(0xFF2196F3) to "Completed"
        "CANCELLED" -> Color(0xFFF44336) to "Cancelled"
        else -> Color.Gray to status
    }

    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}


@Composable
fun ZoneBadge(zone: String) {
    val isIndoor = zone.uppercase() == "INDOOR"

    // Define colors for Indoor vs Outdoor
    val containerColor = if (isIndoor) Color(0xFFE3F2FD) else Color(0xFFF1F8E9)
    val contentColor = if (isIndoor) Color(0xFF1976D2) else Color(0xFF388E3C)
    val icon = if (isIndoor) Icons.Default.Home else Icons.Default.Park

    Surface(
        color = containerColor,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.padding(start = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = contentColor
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = zone.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
        }
    }
}


@Composable
fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.Gray)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, style = MaterialTheme.typography.bodyMedium, color = Color.DarkGray)
    }
}