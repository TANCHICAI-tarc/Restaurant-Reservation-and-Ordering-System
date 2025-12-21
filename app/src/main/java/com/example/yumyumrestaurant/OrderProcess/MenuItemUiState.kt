package com.example.yumyumrestaurant.OrderProcess

import com.google.firebase.firestore.DocumentId

data class MenuItemUiState(
    @DocumentId val menuID: String = "",
    val foodName: String = "",
    val price: Double = 0.0,
    val description: String = "",
    val components: List<String> = emptyList(),
    val category: String = "",
    val image: String? = null,
    val availability: Boolean = true,
    val chefRecommend: Boolean = false
)