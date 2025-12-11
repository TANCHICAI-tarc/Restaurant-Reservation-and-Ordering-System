package com.example.yumyumrestaurant.TableSelectionScreen

import androidx.compose.ui.geometry.Offset
import com.example.yumyumrestaurant.data.RegionData.RegionEntity
import com.example.yumyumrestaurant.data.ReservationData.ReservationEntity
import com.example.yumyumrestaurant.data.TableData.TableEntity

data class TableUiState(
    val tables: List<TableEntity> = emptyList(),
    val regions: List<RegionEntity> = emptyList(),
    val selectedTable: TableEntity? = null,
    var selectedTables: Set<TableEntity> = emptySet(),
    val reservations: List<ReservationEntity> = emptyList(),


    val scale: Float = 1f,
    val offset: Offset = Offset.Zero,

    )

