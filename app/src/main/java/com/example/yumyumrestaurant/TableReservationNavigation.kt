package com.example.yumyumrestaurant

import android.app.Application
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import coil.compose.AsyncImage
import com.example.yumyumrestaurant.ItemDetailScreen.ItemDetailScreen
import com.example.yumyumrestaurant.OrderProcess.MenuItemUiState
import com.example.yumyumrestaurant.OrderProcess.CustomerOrder.CartInfoScreen
//import com.example.yumyumrestaurant.OrderProcess.MenuScreen.CustomerOrderNavigation
import com.example.yumyumrestaurant.OrderProcess.CustomerOrder.MenuDetailsScreen
import com.example.yumyumrestaurant.OrderProcess.CustomerOrder.MenuScreen
import com.example.yumyumrestaurant.OrderProcess.CustomerOrder.OrderSuccessScreen
import com.example.yumyumrestaurant.OrderProcess.MenuViewModel
import com.example.yumyumrestaurant.OrderProcess.OrderViewModel
import com.example.yumyumrestaurant.Reservation.ReservationViewModel

import com.example.yumyumrestaurant.TableSelectionScreen.TableSelectionScreen
import com.example.yumyumrestaurant.TableSelectionScreen.TableViewModel
import com.example.yumyumrestaurant.TableSelectionScreen.TableViewModelFactory
import com.example.yumyumrestaurant.data.ReservationFormScreen

enum class ReservationNavigation(@StringRes val title: Int) {
    ReservationFormScreen(R.string.title_reservation_form),
    ZoneTableSelection(title = R.string.title_select_table_zone),
    ReservationConfirmationScreen(title = R.string.title_reservation_confirmation),
    MenuScreen(title = R.string.title_menu_selection),
    MenuDetailsScreen(title = R.string.title_menu_details),
    CartInfoScreen(title = R.string.title_cart_info),
    OrderSuccessScreen(title = R.string.title_order_success)
//    Success(title = R.string.title_reservation_success),
}
const val TableDetailsRoute = "table_details_screen/{tableId}"
const val TableIdArg = "tableId"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationAppBar(
    @StringRes currentScreenTitle: Int,
    canNavigateBack: Boolean,
    showShare: Boolean = false,
    onShareClick: () -> Unit = {},
    navigateUp: () -> Unit = {},
    modifier: Modifier = Modifier
)  {
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


        } ,
        actions = {
            if (showShare) {
                IconButton(onClick = onShareClick) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share"
                    )
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TableReservationNavigation(
    reservationViewModel: ReservationViewModel,
    menuViewModel: MenuViewModel,
    orderViewModel: OrderViewModel,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val bottomSheetState = rememberModalBottomSheetState()
    var showSheet by remember { mutableStateOf(false) }

    val selectedItem = menuViewModel.menuUiState.collectAsState().value.selectedItem

    val backStackEntry by navController.currentBackStackEntryAsState()
    val route = backStackEntry?.destination?.route

    val currentTitleRes = when {
        route?.startsWith("table_details_screen") == true ->
            R.string.title_table_details

        route != null && ReservationNavigation.values()
            .any { it.name == route } ->
            ReservationNavigation.valueOf(route).title

        else ->
            ReservationNavigation.ReservationFormScreen.title
    }
    val showShareIcon = when (route) {
        ReservationNavigation.ZoneTableSelection.name -> true
        TableDetailsRoute -> true
        else -> false
    }
    val reservationUiState by reservationViewModel.uiState.collectAsState()
    val guestCount = reservationUiState.guestCount

    val context = LocalContext.current
    val tableViewModel: TableViewModel = viewModel(
        factory = TableViewModelFactory(context.applicationContext as Application)
    )

    if(showSheet && selectedItem != null) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = bottomSheetState,
        ) {
            BottomSheetShow(
                item = selectedItem,
                onMakeAnother = {
                    showSheet = false
                    navController.navigate(ReservationNavigation.MenuDetailsScreen.name)
                }
            )
        }
    }

    Scaffold(
        topBar = {
            if (route != ReservationNavigation.OrderSuccessScreen.name) {
                ReservationAppBar(
                    currentScreenTitle = currentTitleRes,

                    canNavigateBack = navController.previousBackStackEntry != null,
                    showShareIcon,
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
                    reservationViewModel = reservationViewModel,
                    guestCount = guestCount,
                    onNavigateToDetails = { tableId ->
                        navController.navigate("table_details_screen/$tableId")
                    },
                    onReserve = {
                        navController.navigate(ReservationNavigation.MenuScreen.name)
                    }
                )
            }


            composable(TableDetailsRoute) { backStackEntry ->
                val tableId = backStackEntry.arguments?.getString(TableIdArg)
                if (tableId != null) {
                    ItemDetailScreen(
                        tableId = tableId,
                        tableViewModel = tableViewModel,
                        reservationViewModel,
                        onNavigateUp = { navController.navigateUp() }
                    )
                }
            }

            composable(ReservationNavigation.MenuScreen.name) {
                MenuScreen(
                    menuViewModel = menuViewModel,
                    orderViewModel = orderViewModel,
                    onNavigateToMenuDetails = { selectedItem ->
                        menuViewModel.setSelectedItem(selectedItem)

                        val alreadyInCart = orderViewModel.orderUiState.value.selectedItems.any { it.menuItem == selectedItem }
                        if (alreadyInCart) {
                            showSheet = true
                        } else {
                            navController.navigate(ReservationNavigation.MenuDetailsScreen.name)
                        }
                    },
                    onNavigateToCart = { navController.navigate(ReservationNavigation.CartInfoScreen.name) }
                )
            }

            composable(ReservationNavigation.MenuDetailsScreen.name) {
                MenuDetailsScreen(
                    menuViewModel = menuViewModel,
                    orderViewModel = orderViewModel,
                    onBack = { navController.navigateUp() }
                )
            }

            composable(ReservationNavigation.CartInfoScreen.name) {
                val orderUiState by orderViewModel.orderUiState.collectAsState()
                val navigateToSuccess by orderViewModel.navigateToSuccess.collectAsState()
                val showDialog by orderViewModel.showConfirmDialog.collectAsState()

                // Observe the navigateToSuccess flag
                LaunchedEffect(navigateToSuccess) {
                    if (navigateToSuccess) {
                        navController.navigate(ReservationNavigation.OrderSuccessScreen.name) {
                            popUpTo(ReservationNavigation.CartInfoScreen.name) { inclusive = true }
                        }
                        orderViewModel.resetNavigation() // reset flag after navigating
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    CartInfoScreen(
                        orderViewModel = orderViewModel,
                        reservationViewModel = reservationViewModel,
                        onBack = { navController.navigateUp() },
                        /*onCheckOut = {
                            reservationViewModel.saveReservation()
                            orderViewModel.confirmOrder()
                        }*/
                    )

                    if (orderUiState.isOrdering) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
                if (showDialog) {
                    AlertDialog(
                        onDismissRequest = { orderViewModel.dismissOrderConfirmation() },
                        title = { Text("Confirm Order") },
                        text = { Text("Are you sure you want to place this order?") },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    orderViewModel.startOrdering()
                                    showSheet = false
                                    reservationViewModel.saveReservation { reservationID ->
                                        orderViewModel.confirmOrderAndProceed(reservationID)
                                    }
                                }
                            ) {
                                Text("Yes")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = {
                                orderViewModel.dismissOrderConfirmation()
                            }) {
                                Text("No")
                            }
                        }
                    )
                }

            }

            composable(ReservationNavigation.OrderSuccessScreen.name) {
                BackHandler(enabled = true) {}
                OrderSuccessScreen(
                    onBackToHome = {
                        navController.navigate(ReservationNavigation.ReservationFormScreen.name) {
                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    modifier = Modifier.fillMaxHeight()
                )
            }
            /*composable(ReservationNavigation.ReservationConfirmationScreen.name) {
                ReservationConfirmationScreen(


                )
            }*/
        }
    }
}

@Composable
fun BottomSheetShow(
    item: MenuItemUiState,
    onMakeAnother: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = item.image,
            contentDescription = item.foodName,
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(Modifier.height(12.dp))

        Text(item.foodName, fontSize = 20.sp, fontWeight = FontWeight.Bold)

        Text(
            text = "RM %.2f".format(item.price),
            fontSize = 18.sp
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = { onMakeAnother() },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Make Another Order")
        }
    }
}