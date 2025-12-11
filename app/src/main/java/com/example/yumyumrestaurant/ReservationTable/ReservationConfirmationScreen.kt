package com.example.yumyumrestaurant.ReservationTable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.yumyumrestaurant.R

@Composable
fun ReservationConfirmationScreen(
//    reservation: Reser,
//    onConfirm: () -> Unit,
//    onEdit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = stringResource(R.string.title_reservation_confirmation),

            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Reservation ID: R000123")
            Text("Name: John Doe")
            Text("Date: 2025-12-15")
            Text("Time: 19:30")
            Text("Duration: 1h 15m")
            Text("End Time: 20:45")
            Text("Guests: 4")
            Text("Zone: Indoor")
            Text("Table Id: T0001")
            Text("Table Label: T1")
            Text("Seats: 4")
            Text("Description: Round")
            Text("Special Requests: Birthday cake")
            Text("Reservation Status: Confirmed")
        }


        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            OutlinedButton(onClick = {  }) {
                Text("Edit")
            }

            Button(onClick = {  }) {
                Text("Confirm")
            }
        }
    }
}
