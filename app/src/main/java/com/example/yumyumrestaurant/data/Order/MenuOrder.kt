package com.example.yumyumrestaurant.data.Order

import com.google.firebase.firestore.PropertyName

data class MenuOrder(
    @get:PropertyName("OrderID") @set:PropertyName("OrderID") var orderID: String = "",
    @get:PropertyName("MenuItemID") @set:PropertyName("MenuItemID") var menuItemID: String = "",
    @get:PropertyName("Quantity") @set:PropertyName("Quantity") var quantity: Int = 0,
    @get:PropertyName("Remark") @set:PropertyName("Remark") var remark: String = ""
)