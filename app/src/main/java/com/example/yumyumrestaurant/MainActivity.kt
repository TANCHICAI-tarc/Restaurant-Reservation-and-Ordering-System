package com.example.yumyumrestaurant

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.yumyumrestaurant.OrderProcess.MenuViewModel
import com.example.yumyumrestaurant.OrderProcess.OrderViewModel
import com.example.yumyumrestaurant.OrderProcess.StaffUpdate.StaffMenuViewModel
import com.example.yumyumrestaurant.Reservation.ReservationViewModel
import com.example.yumyumrestaurant.ui.theme.YumyumrestaurantTheme

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            YumyumrestaurantTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val staffMenuViewModel: StaffMenuViewModel = viewModel()
                    val navController = rememberNavController()
                    //StaffMenuNavigation(staffMenuViewModel, navController)
                    YumYumRestaurantApp()

                    //CustomerOrderNavigation(menuViewModel,orderViewModel, navController)
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun YumYumRestaurantApp() {

    val navController = rememberNavController()
    val menuViewModel: MenuViewModel = viewModel()
    val orderViewModel: OrderViewModel = viewModel()
    val reservationViewModel: ReservationViewModel = viewModel()

    TableReservationNavigation(
        reservationViewModel = reservationViewModel,
        menuViewModel = menuViewModel,
        orderViewModel = orderViewModel,
        navController = navController,
        modifier = Modifier.fillMaxSize()
    )
}

/*@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    YumyumrestaurantTheme {
        Greeting("Android")
    }
}*/