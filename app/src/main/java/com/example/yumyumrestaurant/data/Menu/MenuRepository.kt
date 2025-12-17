package com.example.yumyumrestaurant.data.Menu

import com.example.yumyumrestaurant.OrderProcess.MenuItemUiState

class MenuRepository(
    private val menuDataSource: MenuDataSource
) {
    suspend fun getMenuItems(): List<MenuItemUiState> {
        return menuDataSource.fetchMenuItems()
    }
}