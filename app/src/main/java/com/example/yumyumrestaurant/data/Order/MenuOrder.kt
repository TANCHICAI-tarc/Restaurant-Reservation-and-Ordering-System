package com.example.yumyumrestaurant.data.Order

data class MenuOrder(
    val orderID: String = "",
    val menuItemID: String = "",
    val quantity: Int = 0,
    val remark: String = ""
)
