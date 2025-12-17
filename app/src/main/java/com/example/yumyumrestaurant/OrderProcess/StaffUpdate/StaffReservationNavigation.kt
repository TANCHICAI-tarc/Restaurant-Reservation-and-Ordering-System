package com.example.yumyumrestaurant.OrderProcess.StaffUpdate

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.example.yumyumrestaurant.R

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
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        title = { Text(stringResource(currentScreen.title)) },
        modifier = modifier,
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back_button)
                    )
                }
            }
        }
    )
}

/*@Composable
fun StaffReservationNavigation(menuViewModel: StaffMenuViewModel, navController: NavHostController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentScreen = when{
        backStackEntry?.destination?.route?.startsWith(StaffReservationRoute.ReservationDetails.name) == true -> ReservationNavigation.ReservationDetails
        else -> StaffReservationRoute.ReservationList
    }

    Scaffold(
        topBar = {
            ReservationAppBar(
                currentScreen = currentScreen,
                canNavigateBack = navController.previousBackStackEntry != null,
                navigateUp =  { navController.navigateUp() }
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
                    viewModel = staffBookingViewModel,
                    onReservationClick = { reservationId ->
                        navController.navigate("${StaffReservationRoute.ReservationDetails.name}/$reservationId")
                    }
                )
            }

            composable(
                route = "${StaffReservationRoute.ReservationDetails.name}/{menuID}",
                arguments = listOf(navArgument("menuID") { type = NavType.StringType })
            ) { backStackEntry ->
                val reservationId = entry.arguments?.getString("reservationId") ?: ""
                /*StaffReservationDetailsScreen(
                    reservationId = reservationId,
                    reservationViewModel = staffBookingViewModel
                )*/
            }

        }
    }

}*/