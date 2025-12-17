package com.example.yumyumrestaurant.OrderProcess.CustomerOrder

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.yumyumrestaurant.OrderProcess.MenuViewModel
import com.example.yumyumrestaurant.OrderProcess.OrderViewModel
import com.example.yumyumrestaurant.R

@Composable
fun MenuDetailsScreen(
    menuViewModel: MenuViewModel,
    orderViewModel: OrderViewModel,
    onBack: () -> Unit
) {
    val selectedItem = menuViewModel.menuUiState.collectAsState().value.selectedItem!!
    val quantity = remember  { mutableStateOf(1) }
    val remarks = remember  { mutableStateOf("") }

    Column(
        modifier = Modifier
            .padding(bottom = 32.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        AsyncImage(
            model = selectedItem.image,
            contentDescription = selectedItem.foodName,
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp),
            contentScale = ContentScale.Crop
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = selectedItem.foodName,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = selectedItem.description,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Text(
            text = "Components",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp)
        )

        Spacer(Modifier.height(4.dp))

        Column(modifier = Modifier.padding(start = 16.dp)) {
            selectedItem.components.forEach { component ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "•",
                        fontSize = 14.sp
                    )

                    Spacer(Modifier.width(8.dp))

                    Text(
                        text = component,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
        }

        Spacer(Modifier.height(20.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedButton(
                onClick = { if (quantity.value > 1) quantity.value-- },
                enabled = quantity.value > 1,
                shape = CircleShape,
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.size(40.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = if (quantity.value > 1) Color.Black else Color.Gray
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.Remove,
                    contentDescription = stringResource(R.string.decrease)
                )
            }

            // Quantity Text
            Text(
                text = quantity.value.toString(),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            // Increase button
            OutlinedButton(
                onClick = { quantity.value++ },
                shape = CircleShape,
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.size(40.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.Black
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringResource(R.string.increase)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = remarks.value,
            onValueChange = { remarks.value = it },
            placeholder = { Text("Add remarks / special notes") },
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .height(100.dp)
        )

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = {
                orderViewModel.addToCart(
                    menuItem = selectedItem,
                    quantity = quantity.value,
                    specialNotes = remarks.value
                )
                onBack()
            },
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .height(55.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Add to Cart")
        }
    }
}