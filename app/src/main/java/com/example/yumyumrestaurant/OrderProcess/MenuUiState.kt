package com.example.yumyumrestaurant.OrderProcess

data class MenuUiState(
    val menuItems: List<MenuItemUiState> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: String? = null,
    val selectedItem: MenuItemUiState? = null,
    val isLoading: Boolean = false
)