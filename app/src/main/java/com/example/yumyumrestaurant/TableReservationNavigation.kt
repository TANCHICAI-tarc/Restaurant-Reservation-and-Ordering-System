package com.example.yumyumrestaurant

import android.app.Application
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.View
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
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerState
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
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import coil.compose.AsyncImage

 import com.example.yumyumrestaurant.OrderProcess.MenuItemUiState
import com.example.yumyumrestaurant.OrderProcess.CustomerOrder.CartInfoScreen
//import com.example.yumyumrestaurant.OrderProcess.MenuScreen.CustomerOrderNavigation
import com.example.yumyumrestaurant.OrderProcess.CustomerOrder.MenuDetailsScreen
import com.example.yumyumrestaurant.OrderProcess.CustomerOrder.MenuScreen
import com.example.yumyumrestaurant.OrderProcess.CustomerOrder.OrderSuccessScreen
import com.example.yumyumrestaurant.OrderProcess.MenuViewModel
import com.example.yumyumrestaurant.OrderProcess.OrderViewModel
import com.example.yumyumrestaurant.Reservation.GlobalContinueReservationBar
import com.example.yumyumrestaurant.Reservation.ReservationConfirmationScreen

import com.example.yumyumrestaurant.Reservation.ReservationViewModel
import com.example.yumyumrestaurant.ReservationTable.CapturePurpose
import com.example.yumyumrestaurant.ReservationTable.ReservationTableViewModel
import com.example.yumyumrestaurant.TableDetailScreen.TableDetailScreen

import com.example.yumyumrestaurant.TableSelectionScreen.TableSelectionScreen
import com.example.yumyumrestaurant.TableSelectionScreen.TableViewModel
import com.example.yumyumrestaurant.TableSelectionScreen.TableViewModelFactory



import com.example.yumyumrestaurant.data.ReservationFormScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

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
    navigateUp: () -> Unit = {},
    showDrawer: Boolean = false,
    openDrawer: () -> Unit = {},
    showShare: Boolean = false,
    onShareClick: () -> Unit = {},

    modifier: Modifier = Modifier
)  {
    TopAppBar(
        title = { Text(stringResource(currentScreenTitle)) },
        modifier = modifier,
        navigationIcon = {
            if (showDrawer) { // Show hamburger on the first screen
                IconButton(onClick = openDrawer) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Menu"
                    )
                }
            } else if (canNavigateBack) { // Show back button on subsequent screens
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Back Button"
                    )
                }
            }

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
    reservationTableViewModel: ReservationTableViewModel,
    menuViewModel: MenuViewModel,
    orderViewModel: OrderViewModel,
    navController: NavHostController,
    modifier: Modifier = Modifier,
    isFirstScreenDrawer: Boolean = false,
    drawerState: DrawerState? = null,
    scope: CoroutineScope? = null,
    openDrawer: (() -> Unit)? = null

) {
    val bottomSheetState = rememberModalBottomSheetState()
    var showSheet by remember { mutableStateOf(false) }

    val selectedItem = menuViewModel.menuUiState.collectAsState().value.selectedItem

    val backStackEntry by navController.currentBackStackEntryAsState()
    val route = backStackEntry?.destination?.route

    val currentTitleRes = when {
        route?.startsWith("table_details_screen") == true ->
            R.string.title_table_details

        route?.contains(ReservationNavigation.ReservationConfirmationScreen.name) == true ->
            ReservationNavigation.ReservationConfirmationScreen.title
        route != null && ReservationNavigation.values()
            .any { it.name == route } ->
            ReservationNavigation.valueOf(route).title

        route?.startsWith(ReservationNavigation.MenuScreen.name) == true ->
            ReservationNavigation.MenuScreen.title

        else ->
            ReservationNavigation.ReservationFormScreen.title
    }

    val showShareIcon = when {
        route == ReservationNavigation.ZoneTableSelection.name -> true
        route?.startsWith("table_details_screen") == true -> true
        else -> false
    }


    val reservationUiState by reservationTableViewModel.reservationViewModel.uiState.collectAsState()
    val guestCount = reservationUiState.guestCount

    val context = LocalContext.current
    val tableViewModel: TableViewModel = viewModel(
        factory = TableViewModelFactory(context.applicationContext as Application)
    )



    val tableUiState by tableViewModel.uiState.collectAsState()

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
    val isActive by reservationTableViewModel.isReservationInProgress.collectAsState()


    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    Scaffold(
        topBar = {
            if (route != ReservationNavigation.OrderSuccessScreen.name) {
                val isFirstScreen = route == null || route == ReservationNavigation.ReservationFormScreen.name
                val context = LocalContext.current
                val scope = rememberCoroutineScope()
                ReservationAppBar(
                    currentScreenTitle = currentTitleRes,
                    canNavigateBack = navController.previousBackStackEntry != null,
                    navigateUp = { navController.navigateUp() },
                    showDrawer = isFirstScreen && isFirstScreenDrawer,
                    openDrawer = { openDrawer?.invoke() ?: scope?.launch { drawerState?.open() } },

                    showShare = showShareIcon,
                    onShareClick = {
                        when {
                            route == ReservationNavigation.ZoneTableSelection.name -> {

                                reservationTableViewModel.triggerCapture(CapturePurpose.Share)

                            }

                            route?.startsWith("table_details_screen") == true -> {
                                val tableId = backStackEntry?.arguments?.getString(TableIdArg)
                                val selectedTable = tableUiState.tables.firstOrNull { it.tableId == tableId }

                                val tableInfoText = buildString {
                                    append("Table ${selectedTable?.label ?: "-"} (${selectedTable?.tableId ?: "-"})\n")
                                    append("Seats: ${selectedTable?.seatCount ?: "-"}\n")
                                    append("Zone: ${selectedTable?.zone ?: "-"}\n")
                                    append("Price per seat: RM 10.00\n")
                                    append("Total Price: RM ${"%.2f".format((selectedTable?.seatCount?.times(10) ?: 0).toFloat())}\n")
//                                    append("Description: ${selectedTable.description ?: "-"}\n")
                                }

                                if (tableUiState.preparedFiles.isNotEmpty()) {
                                    shareTableInfo(context, tableInfoText, tableUiState.preparedFiles)
                                } else {
                                     scope.launch {
                                        val files = selectedTable?.imageUrls?.mapNotNull { tableViewModel.getFileFromCoil(context, it) } ?: emptyList()
                                        shareTableInfo(context, tableInfoText, files)
                                    }
                                }
                            }
                        }


                    }
                )

            }
        },
        bottomBar = {

            val baseRoute = currentRoute?.substringBefore("/")

            val allowedScreens = listOf(
                ReservationNavigation.MenuScreen.name,
                ReservationNavigation.CartInfoScreen.name,
                ReservationNavigation.MenuDetailsScreen.name,
                ReservationNavigation.ReservationFormScreen.name
            )

            // 2. Add a check for table details specifically if needed
            val isAllowed = allowedScreens.contains(baseRoute) || currentRoute?.startsWith("table_details_screen") == true

            if (isActive && isAllowed) {
                GlobalContinueReservationBar(
                    viewModel = reservationTableViewModel,
                    onContinue = {
                        val id = reservationUiState.reservationId
                        // 3. Ensure ID is not empty before navigating
                        if (!id.isNullOrEmpty()) {
                            navController.navigate("${ReservationNavigation.ReservationConfirmationScreen.name}/$id") {
                                // Prevent multiple copies of the same screen on the stack
                                popUpTo(ReservationNavigation.ReservationFormScreen.name)
                                launchSingleTop = true
                            }
                        }
                    }
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
                    reservationTableViewModel
                )
            }

            composable(ReservationNavigation.ZoneTableSelection.name) {

                TableSelectionScreen(
                    reservationTableViewModel = reservationTableViewModel,
                    guestCount = guestCount,
                    onNavigateToDetails = { tableId ->
                        navController.navigate("table_details_screen/$tableId")
                    },
                    onReserve = {

                        reservationTableViewModel.reservationViewModel.generateReservationID { generatedId ->

                            navController.navigate("${ReservationNavigation.ReservationConfirmationScreen.name}/$generatedId")
                        }

                    }
                )


            }



            composable(
                route = "${ReservationNavigation.ReservationConfirmationScreen.name}/{resId}"
            ) { backStackEntry ->
                 val generatedId = backStackEntry.arguments?.getString("resId") ?: ""

                ReservationConfirmationScreen(
                    reservationId = generatedId,
                    reservationTableViewModel = reservationTableViewModel,
                    onNavigateToTableDetails = { tableId ->
                        navController.navigate("table_preview/$tableId")
                    },
                    onConfirmed = {
                        navController.navigate(ReservationNavigation.MenuScreen.name)

                    }


                )
            }


            composable(TableDetailsRoute) { backStackEntry ->
                val tableId = backStackEntry.arguments?.getString(TableIdArg)
                if (tableId != null) {
                    TableDetailScreen(
                        tableId = tableId,

                        reservationTableViewModel,
                        onNavigateUp = { navController.navigateUp() },

                        isReadOnly = false
                    )
                }
            }
            composable("table_preview/{tableId}") { backStackEntry ->
                val tableId = backStackEntry.arguments?.getString("tableId")
                TableDetailScreen(
                    tableId = tableId ?: "",
                    reservationTableViewModel = reservationTableViewModel,
                    onNavigateUp = { navController.navigateUp() },
                    isReadOnly = true // Hides buttons and time slots
                )
            }

            composable(ReservationNavigation.MenuScreen.name) {
                MenuScreen(
                    menuViewModel = menuViewModel,
                    orderViewModel = orderViewModel,
                    allowOrdering = true,
                    onRequireReservation = {},
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
                        reservationViewModel = reservationTableViewModel.reservationViewModel,
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
                                    reservationTableViewModel.reservationViewModel.saveReservation { reservationID ->
                                        orderViewModel.confirmOrderAndProceed(reservationID)
                                        reservationTableViewModel.saveReservationWithTables(reservationUiState.selectedTables,reservationID)
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



//fun shareBitmap(context: Context, bitmap: Bitmap) {
//    val file = File(context.cacheDir, "shared_image.png")
//    FileOutputStream(file).use { out ->
//        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
//    }
//
//    val uri = FileProvider.getUriForFile(
//        context,
//        "${context.packageName}.fileprovider",
//        file
//    )
//
//    val shareText = """
//        Check out the indoor layout of YumYum Restaurant!
//        Comfortable indoor seating with a cozy atmosphere.
//    """.trimIndent()
//
//    val intent = Intent(Intent.ACTION_SEND).apply {
//        type =  "image/*"
//
//
//        putExtra(Intent.EXTRA_STREAM, uri)
//        putExtra(Intent.EXTRA_TEXT, shareText)
//        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//    }
//
//    context.startActivity(Intent.createChooser(intent, "Share via"))
//}

fun shareTableInfo(context: Context, text: String, files: List<File>) {
    val contentUris = ArrayList<Uri>()

    files.forEach { file ->
        try {
            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            contentUris.add(contentUri)
        } catch (e: Exception) {
            Log.e("ShareError", "FileProvider failed for: ${file.name}")
        }
    }

    val intent = when {
        contentUris.size > 1 -> {
            Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                type = "image/*"
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, contentUris)
            }
        }
        contentUris.size == 1 -> {
            Intent(Intent.ACTION_SEND).apply {
                type = "image/*"
                putExtra(Intent.EXTRA_STREAM, contentUris[0])
            }
        }
        else -> {
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
            }
        }
    }.apply {
        putExtra(Intent.EXTRA_TEXT, text)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    context.startActivity(Intent.createChooser(intent, "Share Table Info"))
}

