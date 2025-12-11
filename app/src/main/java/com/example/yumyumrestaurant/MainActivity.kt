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
import com.example.yumyumrestaurant.Reservation.ReservationViewModel
import com.example.yumyumrestaurant.ReservationTable.ReservationConfirmationScreen
//import com.example.yumyumrestaurant.TableSelectionScreen.TableSelectionScreen

import com.example.yumyumrestaurant.ui.theme.YumYumRestaurantTheme
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        FirebaseApp.initializeApp(this)

        setContent {
            YumYumRestaurantTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {

                    YumYumRestaurantApp()
//                    TableSelectionScreen()
//                    ReservationConfirmationScreen()
                }

            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun YumYumRestaurantApp() {

    val navController = rememberNavController()

    TableReservationNavigation(

        navController = navController,
        modifier = Modifier.fillMaxSize()
    )
}
