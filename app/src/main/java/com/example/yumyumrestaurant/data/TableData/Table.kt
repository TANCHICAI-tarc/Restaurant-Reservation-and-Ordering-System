package com.example.yumyumrestaurant.data.TableData

import androidx.compose.ui.graphics.Color




// Represents the status of a table
enum class TableStatus {
    Available,
    Reserved,
    Selected
}

enum class ZoneType { INDOOR, OUTDOOR}



sealed class FloorItem {
    data class Table(
        val id: String,
        val label: String,
        val zone:ZoneType,

        val x: Float,
        val y: Float,
        val radius: Float,
        val seatCount: Int,
        val status: TableStatus = TableStatus.Available
    ) : FloorItem()



    data class Region(
        val label: String,
        val zone:ZoneType,

        val x: Float,
        val y: Float,
        val width: Float,
        val height: Float,
        val color: Color
    ) : FloorItem()
}


