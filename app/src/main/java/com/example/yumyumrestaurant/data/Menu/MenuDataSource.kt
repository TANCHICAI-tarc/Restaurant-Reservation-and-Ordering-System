package com.example.yumyumrestaurant.data.Menu

import android.view.MenuItem
import com.example.yumyumrestaurant.OrderProcess.MenuItemUiState
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class MenuDataSource {
    private val db = FirebaseFirestore.getInstance()

    suspend fun fetchMenuItems(): List<MenuItemUiState> {
        return db.collection("Menu")
            .get()
            .await()
            .toObjects(MenuItemUiState::class.java)
    }

    suspend fun getMenuItemById(menuItemID: String): MenuItemUiState? {
        val snapshot = db.collection("Menu")
            .document(menuItemID)
            .get()
            .await()

        return snapshot.toObject(MenuItemUiState::class.java)
    }


}