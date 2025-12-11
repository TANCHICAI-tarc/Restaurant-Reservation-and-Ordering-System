package com.example.yumyumrestaurant.data.RegionData

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.yumyumrestaurant.data.Converters
import com.example.yumyumrestaurant.data.TableData.ZoneType



@Entity(tableName = "Regions")
@TypeConverters(Converters::class)
data class RegionEntity(
    @PrimaryKey val regionId: String = "",
    val label: String = "",
    val zone: String = "",
    val xAxis: Float = 0.0f,
    val yAxis: Float = 0.0f,
    val width: Float = 0.0f,
    val height: Float = 0.0f,
    val color: Long = 0xFFA9A9A9 // DarkGray
)



