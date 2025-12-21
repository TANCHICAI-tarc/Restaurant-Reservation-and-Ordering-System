package com.example.yumyumrestaurant

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserViewReservationScreen(
    navController: NavHostController,
    userViewModel: UserViewModel,
    sharedFilterViewModel: SharedFilterViewModel = viewModel(),
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


    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = {
                    IconButton(onClick = {
//                        navController.popBackStack()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                title = {
                    Text(
                        "My Reservation",
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                actions = {
                    IconButton(onClick = { sharedFilterViewModel.toggleFilterDialog(true) }) {
                        Icon(Icons.Default.Tune, contentDescription = "Filter")
                    }
                }
            )
        },

        ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)

        ) {
            SharedFilterSection(
                viewModel = sharedFilterViewModel,
                title = "Filters",
                onFilterApplied = {
//            sharedFilterViewModel.performSearch()
                }
            )


            val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a")
            val filters = mapOf(

                "Location" to sharedUiState.selectedLocations,

                "Start Date" to listOf(sharedUiState.reservationDate?.format(dateFormatter) ?: "All"),

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
                                onCancelClick = { res ->

                                    sharedFilterViewModel.cancelReservation(res)
                                }
                            )
                        }
                    }
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


            if (
                reservation.reservationStatus == "Confirmed"
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



