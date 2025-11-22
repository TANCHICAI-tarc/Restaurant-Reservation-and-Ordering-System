package com.example.yumyumrestaurant

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.yumyumrestaurant.ReservationFormScreen.ReservationFormScreenViewModel
import com.example.yumyumrestaurant.TableSelectionScreen.TableSelectionScreen
import com.example.yumyumrestaurant.data.ReservationFormScreen
import com.example.yumyumrestaurant.ui.theme.YumYumRestaurantTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            YumYumRestaurantTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {

                    YumYumRestaurantApp()
                }

            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun YumYumRestaurantApp() {
    // 1. Initialize NavController
    val navController = rememberNavController()

    // 2. Initialize the ViewModel
    // This uses the Hilt or default ViewModel mechanism for scoping.
    val reservationViewModel: ReservationFormScreenViewModel = viewModel()

    // 3. Call the TableReservationNavigation function
    TableReservationNavigation(

        navController = navController,
        modifier = Modifier.fillMaxSize()
    )
}
