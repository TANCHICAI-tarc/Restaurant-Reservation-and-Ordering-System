package com.example.yumyumrestaurant.OrderProcess

data class OrderedMenuUi(
    val menuItemID: String,
    val foodName: String,
    val price: Double,
    val quantity: Int,
    val remark: String
)
