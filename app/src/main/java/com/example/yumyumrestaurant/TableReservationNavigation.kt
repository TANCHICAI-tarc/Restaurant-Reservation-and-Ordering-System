package com.example.yumyumrestaurant

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.yumyumrestaurant.ItemDetailScreen.ItemDetailScreen
import com.example.yumyumrestaurant.Reservation.ReservationViewModel
import com.example.yumyumrestaurant.ReservationTable.ReservationConfirmationScreen

import com.example.yumyumrestaurant.ReservationTable.ReservationTableViewModel
import com.example.yumyumrestaurant.TableSelectionScreen.TableSelectionScreen
import com.example.yumyumrestaurant.TableSelectionScreen.TableViewModel
import com.example.yumyumrestaurant.TableSelectionScreen.TableViewModelFactory
import com.example.yumyumrestaurant.data.ReservationFormScreen

enum class ReservationNavigation(@StringRes val title: Int) {
    ReservationFormScreen(R.string.title_reservation_form),
    ZoneTableSelection(title = R.string.title_select_table_zone),
    ReservationConfirmationScreen(title = R.string.title_reservation_confirmation),
//    Success(title = R.string.title_reservation_success),
}
const val TableDetailsRoute = "table_details_screen/{tableId}"
const val TableIdArg = "tableId"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationAppBar(
    @StringRes currentScreenTitle: Int,
    canNavigateBack: Boolean,
    navigateUp: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = { Text(stringResource(currentScreenTitle)) },
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
    val route = backStackEntry?.destination?.route

    val currentTitleRes = when (route) {
        TableDetailsRoute -> R.string.title_table_details
        else -> {
            ReservationNavigation.valueOf(route ?: ReservationNavigation.ReservationFormScreen.name).title
        }
    }

     val reservationViewModel: ReservationViewModel = viewModel()

    val context = LocalContext.current
    val tableViewModel: TableViewModel = viewModel(
        factory = TableViewModelFactory(context.applicationContext as Application)
    )

    Scaffold(
        topBar = {
            if (route != ReservationNavigation.ReservationFormScreen.name) {
                ReservationAppBar(
                    currentScreenTitle = currentTitleRes,
                    canNavigateBack = navController.previousBackStackEntry != null,
                    navigateUp = { navController.navigateUp() }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = ReservationNavigation.ReservationFormScreen.name,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(ReservationNavigation.ReservationFormScreen.name) {

                ReservationFormScreen(
                    onNextScreen = {
                        navController.navigate(ReservationNavigation.ZoneTableSelection.name)
                    },
                    reservationViewModel
                )
            }

            composable(ReservationNavigation.ZoneTableSelection.name) {
                TableSelectionScreen(

                    onNavigateToDetails = { tableId ->
                        navController.navigate("table_details_screen/$tableId")
                    }
                )
            }


            composable(TableDetailsRoute) { backStackEntry ->
                val tableId = backStackEntry.arguments?.getString(TableIdArg)
                if (tableId != null) {
                    ItemDetailScreen(
                        tableId = tableId,
                        tableViewModel = tableViewModel,
                        onNavigateUp = { navController.navigateUp() }
                    )
                }
            }

            composable(ReservationNavigation.ReservationConfirmationScreen.name) {
                ReservationConfirmationScreen(


                )
            }
        }
    }
}