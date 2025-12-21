package com.example.yumyumrestaurant.StaffUpdate

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.example.yumyumrestaurant.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

enum class StaffReservationRoute(@StringRes val title: Int) {
    ReservationList(title = (R.string.title_reservation_list)),
    ReservationDetails(title = (R.string.title_reservation_details))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationAppBar(
    currentScreen: StaffReservationRoute,
    canNavigateBack: Boolean,
    navigateUp: () -> Unit = {},
    showFilter: Boolean = false,
    onFilterClick: () -> Unit = {},
    filterLabel: String = "",
    modifier: Modifier = Modifier,
    showDrawer: Boolean = false,
    openDrawer: () -> Unit = {},
) {
    TopAppBar(
        title = { Text(stringResource(currentScreen.title)) },
        modifier = modifier,
        navigationIcon = {
            if (showDrawer) {
                IconButton(onClick = openDrawer) {
                    Icon(Icons.Default.Menu, contentDescription = "Menu")
                }
            } else if (canNavigateBack) {
                IconButton(onClick = navigateUp) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back Button")
                }
            }
        },
        actions = {
            if (showFilter) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(end = 16.dp)
                ) {
                    IconButton(onClick = onFilterClick) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filter"
                        )
                    }
                    Text(
                        text = filterLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.clickable(onClick = onFilterClick)
                    )
                }
            }
        }
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffReservationNavigation(
    viewModel: StaffOperationViewModel,
    navController: NavHostController,
    drawerState: DrawerState? = null,
    scope: CoroutineScope? = null
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentScreen = when{
        backStackEntry?.destination?.route?.startsWith(StaffReservationRoute.ReservationDetails.name) == true -> StaffReservationRoute.ReservationDetails
        else -> StaffReservationRoute.ReservationList
    }
    var showFilterSheet by remember { mutableStateOf(false) }
    val filterLabel by viewModel.filterLabel.collectAsState()
    val context = LocalContext.current
    val isFirstScreen = currentScreen == StaffReservationRoute.ReservationList

    Scaffold(
        topBar = {
            ReservationAppBar(
                currentScreen = currentScreen,
                canNavigateBack = navController.previousBackStackEntry != null,
                navigateUp =  { navController.navigateUp() },
                showFilter = currentScreen == StaffReservationRoute.ReservationList,
                onFilterClick = { showFilterSheet = true },
                filterLabel = filterLabel,
                showDrawer = isFirstScreen,
                openDrawer = { scope?.launch { drawerState?.open() } },
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = StaffReservationRoute.ReservationList.name,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(StaffReservationRoute.ReservationList.name) {
                StaffReservationScreen(
                    viewModel = viewModel,
                    onReservationClick = { reservationId ->
                        viewModel.selectReservation(reservationId)
                        navController.navigate("${StaffReservationRoute.ReservationDetails.name}/$reservationId")
                    }
                )
            }

            composable(
                route = "${StaffReservationRoute.ReservationDetails.name}/{reservationId}",
                arguments = listOf(navArgument("reservationId") { type = NavType.StringType })
            ) { entry ->
                val reservationId = entry.arguments?.getString("reservationId") ?: ""
                StaffDetailsScreen(
                    reservationID = reservationId,
                    viewModel = viewModel,
                    navController = navController
                )
            }

        }

        if (showFilterSheet) {
            ModalBottomSheet(onDismissRequest = { showFilterSheet = false}) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(dimensionResource(R.dimen.dp_16)),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Filter by Status",
                        style = MaterialTheme.typography.titleMedium
                    )
                    val clearFilterText = "All"
                    val confirmedText = "Confirmed"
                    val completedText = "Completed"
                    val cancelledText = "Cancelled"


                    val statuses = listOf(
                        clearFilterText,
                        confirmedText,
                        completedText,
                        cancelledText
                    )

                    statuses.forEach { status ->
                        Text(
                            text = status,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (status == clearFilterText) {
                                        viewModel.clearFilter()
                                    } else {
                                        viewModel.setFilter(status)
                                    }
                                    showFilterSheet = false
                                }
                                .padding(vertical = 12.dp)
                        )
                    }
                }
            }
        }
    }

}