package com.example.yumyumrestaurant.OrderProcess.CustomerOrder

import android.app.AlertDialog
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Absolute.SpaceBetween
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.yumyumrestaurant.OrderProcess.OrderItemUiState
import com.example.yumyumrestaurant.OrderProcess.OrderUiState
import com.example.yumyumrestaurant.OrderProcess.OrderViewModel
import com.example.yumyumrestaurant.R
import com.example.yumyumrestaurant.Reservation.ReservationUiState
import com.example.yumyumrestaurant.Reservation.ReservationViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartInfoScreen(
    orderViewModel: OrderViewModel,
    reservationViewModel: ReservationViewModel,
    //onCheckOut: () -> Unit,
    onBack: () -> Unit
) {
    val orderState by orderViewModel.orderUiState.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var showPaymentSheet by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            if (orderState.selectedItems.isNotEmpty()) {
                Button(
                    onClick = { showPaymentSheet = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(55.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Checkout - RM ${String.format("%.2f", orderState.totalPrice)}")
                }
            }
        }
    ) { innerPadding ->
        if (orderState.selectedItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(R.drawable.empty_cart),
                        contentDescription = "Empty Cart",
                        modifier = Modifier
                            .size(250.dp)
                            .clip(CircleShape)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Oops!!",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "It seems no food is added to your cart.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { onBack() },
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .width(180.dp)
                            .height(50.dp)
                    ) {
                        Text("Order Now")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        bottom = innerPadding.calculateBottomPadding()
                    )
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                items(orderState.selectedItems) { item ->
                    CartItemCard(
                        item = item,
                        orderViewModel = orderViewModel,
                        context = context,
                        coroutineScope = coroutineScope
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
                item {
                    val reservationUiState by reservationViewModel.uiState.collectAsState()
                    ReservationSummarySection(reservationUiState)
                }
                item {
                    ReceiptSummarySection(orderState)
                    Spacer(modifier = Modifier.height(4.dp))
                }
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Text(
                            text = "Payment Method",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { orderViewModel.setPaymentOption("Credit Card") }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = orderState.paymentOption == "Credit Card",
                                onClick = { orderViewModel.setPaymentOption("Credit Card") }
                            )
                            Text(
                                text = "Credit/Debit Card",
                                modifier = Modifier.padding(start = 12.dp)
                            )
                        }

                        /*Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { orderViewModel.setPaymentOption("E-Wallet") }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = orderState.paymentOption == "E-Wallet",
                                onClick = { orderViewModel.setPaymentOption("E-Wallet") }
                            )
                            Text(
                                text = "E-Wallet (TNG / GrabPay / Boost)",
                                modifier = Modifier.padding(start = 16.dp)
                            )
                        }*/
                    }
                }
            }
        }
    }
    if (showPaymentSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        ModalBottomSheet(
            onDismissRequest = { showPaymentSheet = false },
            sheetState = sheetState
        ) {
            when (orderState.paymentOption) {
                "Credit Card" -> {
                    CreditDebitBottomSheet(
                        orderState = orderState,
                        /*onConfirm = {
                            showPaymentSheet = false
                            onCheckOut()
                        },*/
                        //onDismiss = { showPaymentSheet = false },
                        viewModel = orderViewModel
                    )
                }

            }
        }
    }
}

@Composable
fun CartItemCard(
    item: OrderItemUiState,
    orderViewModel: OrderViewModel,
    context: Context,
    coroutineScope: CoroutineScope
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        )  {
            AsyncImage(
                model = item.menuItem.image,
                contentDescription = item.menuItem.foodName,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.menuItem.foodName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                Text(
                    text = "RM ${String.format("%.2f", item.menuItem.price)}",
                    color = Color.Gray,
                    fontSize = 14.sp
                )

                if (item.specialNotes.isNotEmpty()) {
                    Text(
                        text = "Notes: ${item.specialNotes}",
                        fontSize = 13.sp,
                        color = Color.DarkGray
                    )
                }

                Spacer(Modifier.height(8.dp))

                // Quantity row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(
                        onClick = {
                            if (item.quantity > 1) {
                                orderViewModel.updateItemQuantity(
                                    item,
                                    item.quantity - 1
                                )
                            } else {
                                // Confirm deletion if quantity = 1
                                coroutineScope.launch {
                                    val confirmed = showDeleteConfirmation(context)
                                    if (confirmed) {
                                        orderViewModel.removeItem(item)
                                    }
                                }
                            }
                        },
                        shape = CircleShape,
                        modifier = Modifier.size(32.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(
                            Icons.Filled.Remove,
                            contentDescription = "Decrease",
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Text(
                        item.quantity.toString(),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    OutlinedButton(
                        onClick = {
                            orderViewModel.updateItemQuantity(
                                item,
                                item.quantity + 1
                            )
                        },
                        shape = CircleShape,
                        modifier = Modifier.size(32.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(
                            Icons.Filled.Add,
                            contentDescription = "Increase",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ReservationSummarySection(reservationUiState: ReservationUiState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Text(
            text = "Reservation Summary",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Date", fontSize = 15.sp)
            Text(reservationUiState.selectedDate.toString(), fontSize = 15.sp, fontWeight = FontWeight.Medium)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Time", fontSize = 15.sp)
            Text(
                text = "${reservationUiState.selectedStartTime.format(java.time.format.DateTimeFormatter.ofPattern("hh:mm a"))} - " +
                        "${reservationUiState.selectedStartTime.plusMinutes(reservationUiState.selectedDurationMinutes.toLong()).format(java.time.format.DateTimeFormatter.ofPattern("hh:mm a"))}",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Guests", fontSize = 15.sp)
            Text(reservationUiState.guestCount.toString(), fontSize = 15.sp, fontWeight = FontWeight.Medium)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Zone", fontSize = 15.sp)
            Text(reservationUiState.selectedZone, fontSize = 15.sp, fontWeight = FontWeight.Medium)
        }

        val tablesText = reservationUiState.selectedTables.joinToString { it.label }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Tables", fontSize = 15.sp)
            Text(tablesText, fontSize = 15.sp, fontWeight = FontWeight.Medium)
        }

        if (reservationUiState.specialRequests.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text("Special Requests:", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text(reservationUiState.specialRequests, fontSize = 14.sp, color = Color.Gray)
        }
    }
}
@Composable
fun ReceiptSummarySection(orderState: OrderUiState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 16.dp)
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Text(
            text = "Order Summary",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Each cart item line
        orderState.selectedItems.forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = SpaceBetween
            ) {
                Text(
                    text = "${item.menuItem.foodName} x${item.quantity}",
                    fontSize = 15.sp
                )
                Text(
                    text = "RM %.2f".format(item.menuItem.price * item.quantity),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // If item has remark, show small remark under it
            if (item.specialNotes.isNotBlank()) {
                Text(
                    text = "• ${item.specialNotes}",
                    fontSize = 13.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(start = 8.dp, bottom = 6.dp)
                )
            }
        }

        Divider(Modifier.padding(vertical = 12.dp))

        // Total before tax (optional)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = SpaceBetween
        ) {
            Text("Subtotal", fontSize = 16.sp)
            Text(
                "RM %.2f".format(orderState.subTotal),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(Modifier.height(6.dp))

        // Example tax (if you need)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = SpaceBetween
        ) {
            Text("Service Tax (6%)", fontSize = 16.sp)
            Text(
                "RM %.2f".format(orderState.taxAmount),
                fontSize = 16.sp
            )
        }

        Divider(Modifier.padding(vertical = 12.dp))

        // Final total
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = SpaceBetween
        ) {
            Text(
                "Total Payment",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                "RM %.2f".format(orderState.totalPrice),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun CreditDebitBottomSheet(
    orderState: OrderUiState,
    //onDismiss: () -> Unit,
    //onConfirm: () -> Unit,
    viewModel: OrderViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Enter Credit/Debit Card Details",
            style = MaterialTheme.typography.titleMedium
        )

        // Card Number
        OutlinedTextField(
            value = orderState.cardNumber ?: "",
            onValueChange = { input ->
                val filtered = input.filter { it.isDigit() }.take(16)
                viewModel.updateCardNumber(filtered) // you can implement this in your OrderViewModel
            },
            label = { Text("Card Number") },
            isError = orderState.cardNumberError,
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        if (orderState.cardNumberError) {
            Text(
                text = "Card number must be 16 digits",
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall
            )
        }

        // Expiration Date Row
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = orderState.expMonth,
                onValueChange = { input ->
                    val filtered = input.filter { it.isDigit() }.take(2)
                    viewModel.updateExpMonth(filtered)
                },
                label = { Text("MM") },
                isError = orderState.expMonthError,
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = orderState.expYear,
                onValueChange = { input ->
                    val filtered = input.filter { it.isDigit() }.take(2)
                    viewModel.updateExpYear(filtered)
                },
                label = { Text("YY") },
                isError = orderState.expYearError,
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }
        if (orderState.expMonthError || orderState.expYearError) {
            Text(
                text = "Invalid expiry date",
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall
            )
        }

        // CVV
        OutlinedTextField(
            value = orderState.cvv ?: "",
            onValueChange = { input ->
                val filtered = input.filter { it.isDigit() }.take(4)
                viewModel.updateCvv(filtered)
            },
            label = { Text("CVV") },
            isError = orderState.cvvError,
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        if (orderState.cvvError) {
            Text(
                text = "CVV must be 3 or 4 digits",
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Confirm Button
        Button(
            onClick = { viewModel.requestOrderConfirmation() },
            enabled = viewModel.canConfirmPayment() && !orderState.isOrdering,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (orderState.isOrdering) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Processing...")
                }
            } else {
                Text("Confirm Payment")
            }
        }
    }
}
suspend fun showDeleteConfirmation(context: Context): Boolean =
    suspendCancellableCoroutine { cont ->
        AlertDialog.Builder(context)
            .setTitle("Remove Item")
            .setMessage("Remove this item from cart?")
            .setPositiveButton("Yes") { dialog, _ ->
                cont.resume(true) {}
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ ->
                cont.resume(false) {}
                dialog.dismiss()
            }
            .setOnCancelListener { cont.resume(false) {} }
            .show()
    }