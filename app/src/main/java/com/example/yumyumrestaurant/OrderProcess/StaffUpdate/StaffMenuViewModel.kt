package com.example.yumyumrestaurant.OrderProcess.StaffUpdate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yumyumrestaurant.OrderProcess.MenuItemUiState
import com.example.yumyumrestaurant.data.Menu.MenuDataSource
import com.example.yumyumrestaurant.data.Menu.MenuRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class StaffMenuViewModel(
    private val menuRepository: MenuRepository = MenuRepository(MenuDataSource())
): ViewModel() {
    val menuItems = MutableStateFlow<List<MenuItemUiState>>(emptyList())

    init {
        loadMenu()
    }

    fun loadMenu() {
        viewModelScope.launch {
            menuItems.value = menuRepository.getMenuItems()
        }
    }

    fun addMenuItem(menuItem: MenuItemUiState) {
        viewModelScope.launch {
            //menuRepository.addMenuItem(menuItem)
            loadMenu()
        }
    }

    fun updateAvailability(menuID: String, isAvailable: Boolean) {
        viewModelScope.launch {
            //menuRepository.updateAvailability(menuID, isAvailable)
            loadMenu()
        }
    }

    fun deleteMenuItem(menuID: String) {
        viewModelScope.launch {
            //menuRepository.deleteMenuItem(menuID)
            loadMenu()
        }
    }
}