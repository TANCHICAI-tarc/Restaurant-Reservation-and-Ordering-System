package com.example.yumyumrestaurant

import android.app.Application
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.yumyumrestaurant.OrderProcess.CustomerOrder.MenuScreen
import com.example.yumyumrestaurant.OrderProcess.MenuViewModel
import com.example.yumyumrestaurant.OrderProcess.OrderViewModel
import com.example.yumyumrestaurant.Reservation.ReservationDetailScreen
import com.example.yumyumrestaurant.Reservation.ReservationViewModel
import com.example.yumyumrestaurant.ReservationTable.ReservationTableViewModel
import com.example.yumyumrestaurant.ReservationTable.ReservationTableViewModelFactory
import com.example.yumyumrestaurant.StaffUpdate.StaffOperationViewModel
import com.example.yumyumrestaurant.TableDetailScreen.TableDetailScreen
import com.example.yumyumrestaurant.TableSelectionScreen.TableViewModel
import com.example.yumyumrestaurant.data.ReservationData.ReservationRepository
import com.example.yumyumrestaurant.data.ReservationTableData.ReservationTableRepository
import com.example.yumyumrestaurant.data.ReservationTableData.Reservation_TableDatabase
import com.example.yumyumrestaurant.data.TableData.TableRepository
import com.example.yumyumrestaurant.shareFilterScreen.SharedFilterViewModel
import com.example.yumyumrestaurant.ui.AboutViewModel
import com.example.yumyumrestaurant.ui.ResetViewModel
import com.example.yumyumrestaurant.ui.UserViewModel
import com.example.yumyumrestaurant.ui.UserLoginViewModel
import com.example.yumyumrestaurant.ui.UserSignInViewModel
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        FirebaseApp.initializeApp(this)
        setContent {
            AppScreen()
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppScreen(){
    val navController = rememberNavController()
    val userViewModel: UserViewModel = viewModel()
    val userLoginViewModel: UserLoginViewModel = viewModel()
    val userSignInViewModel: UserSignInViewModel = viewModel()
    val resetViewModel: ResetViewModel= viewModel()
    NavHost(
        navController = navController,
        startDestination = "Home"
    ) {
        composable("Home") {
            Home(navController)
        }

        composable("Login") {
            Login(navController, userLoginViewModel)
        }

        composable("ForgotPassword"){
            ForgotPassword(navController, resetViewModel)
        }

        composable("ResetPassword"){
            ResetPassword(navController, resetViewModel)
        }

        composable("ResetSuccess"){
            ResetSuccess(navController)
        }

        composable("Register") {
            Register(navController, userSignInViewModel)
        }

        composable("SuccessRegister"){
            SuccessRegister(navController)
        }

        composable("UserPage"){
            UserPage(navController, userViewModel)
        }

        composable("AdminPage"){
            AdminPage(navController)
        }

        composable("Report") {
            Report(navController = navController)
        }




        composable(
            route = "reservation_detail/{resId}",
            arguments = listOf(navArgument("resId") { type = NavType.StringType })
        ) { backStackEntry ->
            val resId = backStackEntry.arguments?.getString("resId") ?: ""
            val context = LocalContext.current
            val application = context.applicationContext as Application


            val database = Reservation_TableDatabase.getReservationTableDatabase(application)
            val reservationTableRepository = ReservationTableRepository(database.reservationTableDao())
            val reservationRepository = ReservationRepository(database.reservationDao())
            val tableRepository = TableRepository(database.tableDao())


            val reservationTableViewModel: ReservationTableViewModel = viewModel(
                factory = ReservationTableViewModelFactory(
                    tableViewModel = viewModel(),
                    reservationViewModel = viewModel(),
                    reservationTableRepository = reservationTableRepository,
                    reservationRepository = reservationRepository,
                    tableRepository = tableRepository
                )
            )

            val staffViewModel: StaffOperationViewModel = viewModel()
            val orderViewModel: OrderViewModel = viewModel()

            ReservationDetailScreen(
                reservationId = resId,
                reservationTableViewModel = reservationTableViewModel,
                staffViewModel = staffViewModel,
                orderViewModel = orderViewModel,
                onNavigateToTableDetails = { tableId ->

                    navController.navigate("table_preview/$tableId")
                },
                onNavigateUp = { navController.navigateUp() }
            )
        }


        composable(
                    route = "table_preview/{tableId}",
                    arguments = listOf(navArgument("tableId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val tableId = backStackEntry.arguments?.getString("tableId") ?: ""


                    val context = LocalContext.current
                    val application = context.applicationContext as Application
                    val database = Reservation_TableDatabase.getReservationTableDatabase(application)

                    val reservationTableViewModel: ReservationTableViewModel = viewModel(
                        factory = ReservationTableViewModelFactory(
                            tableViewModel = viewModel(),
                            reservationViewModel = viewModel(),
                            reservationTableRepository = ReservationTableRepository(database.reservationTableDao()),
                            reservationRepository = ReservationRepository(database.reservationDao()),
                            tableRepository = TableRepository(database.tableDao())
                        )
                    )

                    TableDetailScreen(
                        tableId = tableId,
                        reservationTableViewModel = reservationTableViewModel,
                        onNavigateUp = { navController.navigateUp() },
                        isReadOnly = true
                    )
                }


    }
}

data class UserMenuItem(
    val title: String,
    val iconResId: Int,
    val destination: String
)

const val USER_HOME = "UserHome"
const val VIEW_MENU = "ViewMenu"
const val RESERVATION_FLOW = "Reservation"
const val VIEW_RESERVATION = "ViewReservation"
const val USER_PROFILE = "UserProfile"

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserPage(navController: NavHostController, userViewModel: UserViewModel) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var currentScreen by rememberSaveable { mutableStateOf("UserHome") }
    val aboutViewModel: AboutViewModel = viewModel()

    val menuItems = listOf(
        UserMenuItem("Home", R.drawable.home_24dp_1f1f1f_fill0_wght400_grad0_opsz24, "UserHome"),
        UserMenuItem("Table Reservation", R.drawable.table_restaurant_24dp_1f1f1f_fill0_wght400_grad0_opsz24, "Reservation"),
        UserMenuItem("View Menu", R.drawable.food_16544106,"ViewMenu"),
        UserMenuItem("View Reservation", R.drawable.view_list_24dp_1f1f1f_fill0_wght400_grad0_opsz24, "ViewReservation"),
        UserMenuItem("Profile", R.drawable.person_24dp_1f1f1f_fill0_wght400_grad0_opsz24, "UserProfile"),
        UserMenuItem("About", R.drawable.info_24dp_1f1f1f_fill0_wght400_grad0_opsz24, "UserAbout"),
        UserMenuItem("Logout", R.drawable.logout_24dp_1f1f1f_fill0_wght400_grad0_opsz24, "Logout"),
        UserMenuItem("DeleteAccount", R.drawable.close_24dp_1f1f1f_fill0_wght400_grad0_opsz24, "DeleteAccount"),
    )

    BackHandler {
        if (currentScreen != USER_HOME) {
            currentScreen = USER_HOME
        } else {
            navController.navigate("Home") {
                popUpTo(0) { inclusive = true }
            }
        }
    }
    val sharedFilterViewModel: SharedFilterViewModel = viewModel()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .fillMaxSize()
                    .background(Color.White)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(30.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .background(Color.Blue)
                        .padding(16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Column {
                        Text(
                            text = "YumYum Restaurant",
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Text(
                            text = "Welcome User!",
                            fontSize = 24.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                menuItems.forEach { item ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                scope.launch { drawerState.close() }
                                when (item.destination) {
                                    "Logout" -> {
                                        Firebase.auth.signOut()
                                        navController.navigate("Home") {
                                            popUpTo(0) { inclusive = true }
                                        }
                                    }
                                    else -> {
                                        currentScreen = item.destination
                                    }
                                }
                            }
                            .padding(vertical = 12.dp, horizontal = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = item.iconResId),
                                contentDescription = item.title,
                                modifier = Modifier
                                    .size(24.dp),
                                colorFilter = if (currentScreen == item.destination) {
                                    androidx.compose.ui.graphics.ColorFilter.tint(Color(0xFF2196F3))
                                } else {
                                    androidx.compose.ui.graphics.ColorFilter.tint(Color.Gray)
                                }
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = item.title,
                                fontSize = 16.sp,
                                color = if (currentScreen == item.destination) Color.Blue else Color.Black,
                                fontWeight = if (currentScreen == item.destination) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }

                    if (item != menuItems.last()) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                // HIDE the outer TopAppBar when the Reservation flow is active.
                // TableReservationNavigation provides its own internal TopAppBar
                // which correctly handles the hamburger icon vs. back button.
                if (currentScreen != RESERVATION_FLOW) {
                    TopAppBar(
                        title = {
                            Text(
                                text = when (currentScreen) {
                                    USER_HOME -> "Home"
                                    VIEW_MENU -> "ViewMenu"
                                    VIEW_RESERVATION -> "View My Reservation"
                                    USER_PROFILE -> "Profile"
                                    "MainUserProfile" -> "MainUserProfile"
                                    "UserEdit" -> "UserEdit"
                                    "DeleteAccount" -> "Delete Account"
                                    else -> "YumYum Restaurant"
                                }
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = "Menu")
                            }
                        },actions = {
                            when (currentScreen) {
                                VIEW_RESERVATION -> {
                                    IconButton(onClick = { sharedFilterViewModel.toggleFilterDialog(true) }) {
                                        Icon(Icons.Default.Tune, contentDescription = "Filter")
                                    }
                                }

                                else -> {}
                            }
                        }

                    )
                }
            }
        ) { innerPadding ->
            when (currentScreen) {
                USER_HOME  -> UserHome(navController, Modifier.padding(innerPadding))
                RESERVATION_FLOW -> Reservation(
                    userViewModel,
                    drawerState = drawerState,
                    scope = scope,
                    openDrawer = { scope.launch { drawerState.open() } },
                    onNavigateToViewReservation = {
                        currentScreen = VIEW_RESERVATION
                    },

                    sharedFilterViewModel
                )
                VIEW_MENU-> {
                    val menuViewModel: MenuViewModel = viewModel()
                    val orderViewModel: OrderViewModel = viewModel()

                    MenuScreen(
                        modifier = Modifier.padding(innerPadding),
                        menuViewModel = menuViewModel,
                        orderViewModel = orderViewModel,
                        allowOrdering = false,
                        onRequireReservation = {
                            currentScreen = RESERVATION_FLOW
                        },
                        onNavigateToMenuDetails = {}, // blocked anyway
                        onNavigateToCart = {} // blocked anyway
                    )
                }
                VIEW_RESERVATION -> ViewReservation(navController, userViewModel,sharedFilterViewModel,Modifier.padding(innerPadding))
                USER_PROFILE -> UserProfile(navController, onLogout = {
                    Firebase.auth.signOut()
                    navController.navigate("Home") {
                        popUpTo(0) { inclusive = true }
                    }
                })

                "MainUserProfile" -> MainUserProfile(navController, userViewModel, onLogout = {
                    Firebase.auth.signOut()
                    navController.navigate("Home") {
                        popUpTo(0) { inclusive = true }
                    }
                })
                "UserEdit" -> UserEdit(navController, userViewModel, onLogout = {
                    Firebase.auth.signOut()
                    navController.navigate("Home") {
                        popUpTo(0) { inclusive = true }
                    }
                })
                "UserAbout" -> UserAbout(navController, Modifier.padding(innerPadding), aboutViewModel)
                "DeleteAccount" -> DeleteAccount(navController, Modifier.padding(innerPadding), userViewModel)
                else -> UserHome(navController, Modifier.padding(innerPadding))
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun UserProfile(navController: NavHostController, onLogout: () -> Unit){
    val profileNavController = rememberNavController()
    val userViewModel: UserViewModel = viewModel()

    NavHost(
        navController = profileNavController,
        startDestination = "MainUserProfile"
    ) {
        composable("MainUserProfile") {
            MainUserProfile(profileNavController, userViewModel, onLogout)
        }

        composable("UserEdit") {
            UserEdit(profileNavController, userViewModel, onLogout)
        }
    }
}

data class AdminMenuItem(
    val title: String,
    val iconResId: Int,
    val destination: String
)

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPage(navController: NavHostController) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var currentScreen by remember { mutableStateOf("AdminHome") }
    val userViewModel: UserViewModel = viewModel()
    val aboutViewModel: AboutViewModel = viewModel()

    val menuItems = listOf(
        AdminMenuItem("Home", R.drawable.home_24dp_1f1f1f_fill0_wght400_grad0_opsz24, "AdminHome"),
        AdminMenuItem("Manage Reservation", R.drawable.table_restaurant_24dp_1f1f1f_fill0_wght400_grad0_opsz24, "ManageReservation"),
        AdminMenuItem("Profile", R.drawable.person_24dp_1f1f1f_fill0_wght400_grad0_opsz24, "Profile"),
        AdminMenuItem("About", R.drawable.info_24dp_1f1f1f_fill0_wght400_grad0_opsz24, "AdminAbout"),
        AdminMenuItem("Report", R.drawable.table_chart_24dp_1f1f1f_fill0_wght400_grad0_opsz24, "Report"),
        AdminMenuItem("Logout", R.drawable.logout_24dp_1f1f1f_fill0_wght400_grad0_opsz24, "Logout")
    )

    BackHandler( enabled = currentScreen != "AdminHome") {
        currentScreen = "AdminHome"
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .fillMaxSize()
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                Spacer(modifier = Modifier.height(30.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .background(Color.Red)
                        .padding(16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Column {
                        Text(
                            text = "YumYum Restaurant",
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Text(
                            text = "Welcome Admin!",
                            fontSize = 24.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                menuItems.forEach { item ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                scope.launch { drawerState.close() }
                                when (item.destination) {
                                    "Logout" -> {
                                        Firebase.auth.signOut()
                                        navController.navigate("Home") {
                                            popUpTo(0) { inclusive = true }
                                        }
                                    }
                                    else -> {
                                        currentScreen = item.destination
                                    }
                                }
                            }
                            .padding(vertical = 12.dp, horizontal = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = item.iconResId),
                                contentDescription = item.title,
                                modifier = Modifier
                                    .size(24.dp),
                                colorFilter = if (currentScreen == item.destination) {
                                    androidx.compose.ui.graphics.ColorFilter.tint(Color(0xFF2196F3))
                                } else {
                                    androidx.compose.ui.graphics.ColorFilter.tint(Color.Gray)
                                }
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = item.title,
                                fontSize = 16.sp,
                                color = if (currentScreen == item.destination) Color.Blue else Color.Black,
                                fontWeight = if (currentScreen == item.destination) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }

                    if (item != menuItems.last()) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                if (currentScreen != "ManageReservation") {
                    TopAppBar(
                        title = {
                            Text(
                                text = when (currentScreen) {
                                    "AdminHome" -> "Home"
                                    "Profile" -> "Profile"
                                    "MainAdminProfile" -> "MainAdminProfile"
                                    "AdminEdit" -> "AdminEdit"
                                    "AdminAbout" -> "About"
                                    "Report" -> "Report"
                                    else -> "YumYum Restaurant"
                                }
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = "Menu")
                            }
                        }
                    )
                }
            }
        ) { innerPadding ->
            when (currentScreen) {
                "AdminHome" -> AdminHome(navController, Modifier.padding(innerPadding))
                "ManageReservation" -> ManageReservation(navController, drawerState, scope)
                "Profile" -> Profile(navController, onLogout = {
                    Firebase.auth.signOut()
                    navController.navigate("Home") {
                        popUpTo(0) { inclusive = true }
                    }
                })
                "MainAdminProfile" -> MainAdminProfile(navController, userViewModel, onLogout = {
                    Firebase.auth.signOut()
                    navController.navigate("Home") {
                        popUpTo(0) { inclusive = true }
                    }
                })
                "AdminEdit" -> AdminEdit(navController, userViewModel, onLogout = {
                    Firebase.auth.signOut()
                    navController.navigate("Home") {
                        popUpTo(0) { inclusive = true }
                    }
                })
                "AdminAbout" -> AdminAbout(navController, Modifier.padding(innerPadding), aboutViewModel)
                "Report" -> Report(navController, Modifier.padding(innerPadding))
                else -> AdminHome(navController, Modifier.padding(innerPadding))
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Profile(navController: NavHostController, onLogout: () -> Unit){
    val profileNavController = rememberNavController()
    val userViewModel: UserViewModel = viewModel()

    NavHost(
        navController = profileNavController,
        startDestination = "MainAdminProfile"
    ) {
        composable("MainAdminProfile") {
            MainAdminProfile(profileNavController, userViewModel, onLogout)
        }

        composable("AdminEdit") {
            AdminEdit(profileNavController, userViewModel, onLogout)
        }
    }
}