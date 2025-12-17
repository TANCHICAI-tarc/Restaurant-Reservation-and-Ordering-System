package com.example.yumyumrestaurant.data.TableData

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import androidx.compose.ui.graphics.Color
import com.example.yumyumrestaurant.data.Converters

enum class TableStatus { AVAILABLE, SELECTED, RESERVED }

enum class ZoneType { INDOOR, OUTDOOR }

@Entity(tableName = "Tables")
@TypeConverters(Converters::class)
data class TableEntity(
    @PrimaryKey val tableId: String = "",
    val label: String = "",
    val zone: String = "",
    val xAxis: Float = 0f,
    val yAxis: Float = 0f,
    val radius: Float = 0f,
    val seatCount: Int = 0,
    val type: String = "",
    var status: String = "AVAILABLE",
    var imageUrls: List<String> = emptyList(),

    )

