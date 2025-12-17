package com.example.yumyumrestaurant.OrderProcess

data class OrderItemUiState(
    val menuItem: MenuItemUiState,
    val quantity: Int = 0,
    val specialNotes: String = ""
)