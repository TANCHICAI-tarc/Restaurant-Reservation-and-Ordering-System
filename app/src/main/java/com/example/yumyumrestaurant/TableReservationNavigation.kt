package com.example.yumyumrestaurant



import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState

import com.example.yumyumrestaurant.ReservationFormScreen.ReservationFormScreenViewModel
import com.example.yumyumrestaurant.data.ReservationFormScreen


// 1. Updated Navigation Enum for Table Reservation
enum class ReservationNavigation(@StringRes val title: Int) {
    ReservationFormScreen(R.string.title_reservation_form),
    ZoneTableSelection(title = R.string.title_select_table_zone),
//    GuestDetails(title = R.string.title_guest_details),
//    Success(title = R.string.title_reservation_success),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationAppBar(
    currentScreen: ReservationNavigation,
    canNavigateBack: Boolean,
    navigateUp: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = { Text(stringResource(currentScreen.title)) },
        modifier = modifier,
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Back Button"
                    )
                }
            }
        }
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TableReservationNavigation(

    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentScreen = ReservationNavigation.valueOf(
        backStackEntry?.destination?.route ?: ReservationNavigation.ReservationFormScreen.name
    )

    Scaffold(
        topBar = {
//            if (currentScreen != ReservationNavigation.Success) {
//                ReservationAppBar(
//                    currentScreen = currentScreen,
//                    canNavigateBack = navController.previousBackStackEntry != null,
//                    navigateUp = { navController.navigateUp() }
//                )
//            }
        }
    ) { innerPadding ->


        NavHost(
            navController = navController,
            startDestination = ReservationNavigation.ReservationFormScreen.name,
            modifier = Modifier.padding(innerPadding)
        ) {
            // --- 1. Date and Time Selection ---
            composable(ReservationNavigation.ReservationFormScreen.name) {
                ReservationFormScreen(
                    // 2. Pass the navigation action here:
                    onNextScreen = {
                        navController.navigate(ReservationNavigation.ZoneTableSelection.name)
                    }
                )

            }

            composable(ReservationNavigation.ZoneTableSelection.name) {
                // Placeholder for your next screen content
                Text(
                    "Zone/Table Selection Screen Content",
                    modifier = Modifier.fillMaxHeight()
                )
            }

//            // --- 2. Zone/Table Selection ---
//            composable(ReservationNavigation.ZoneTableSelection.name) {
//                // Placeholder for selecting Zone/Table.
//                // This screen would finalize the seating area choice.
//                Text(
//                    "Zone/Table Selection Screen Content",
//                    modifier = Modifier.fillMaxHeight()
//                )
//                // When done, navigate to guest details
//                /* Example of navigation logic:
//                ZoneTableSelectionScreen(
//                    viewModel = reservationViewModel,
//                    onNextClicked = {
//                        navController.navigate(ReservationNavigation.GuestDetails.name)
//                    }
//                )*/
//            }
//
//            // --- 3. Guest Details/Final Review ---
//            composable(ReservationNavigation.GuestDetails.name) {
//                // Placeholder for collecting customer name, phone, email, etc.
//                // This is where you might finalize the ReservationData object.
//                Text(
//                    "Guest Details Screen Content",
//                    modifier = Modifier.fillMaxHeight()
//                )
//                // When done, navigate to success/final step
//                /* Example of navigation logic:
//                GuestDetailsScreen(
//                    viewModel = reservationViewModel,
//                    onReserveClicked = { reservationData ->
//                        // Call ViewModel submit logic here
//                        navController.navigate(ReservationNavigation.Success.name)
//                    }
//                )*/
//            }
//
//            // --- 4. Success/Confirmation ---
//            composable(ReservationNavigation.Success.name) {
//                // Placeholder for success screen, showing reservation ID
//                Text(
//                    "Reservation Success Screen Content",
//                    modifier = Modifier.fillMaxHeight()
//                )
//
//            }
        }
    }
}