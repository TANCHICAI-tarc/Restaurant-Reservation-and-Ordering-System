package com.example.yumyumrestaurant.ReservationTable

import android.graphics.Bitmap
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.yumyumrestaurant.data.ReservationData.ReservationEntity
import com.example.yumyumrestaurant.data.ReservationTableData.Reservation_Table_Entity
import com.example.yumyumrestaurant.data.TableData.TableEntity
import com.example.yumyumrestaurant.data.TableData.TableStatus
import java.io.File
import java.time.LocalDate
import java.time.LocalTime

@RequiresApi(Build.VERSION_CODES.O)
data class ReservationTableUiState(


    val tables: List<TableEntity> = emptyList(),
    val reservations: List<ReservationEntity> = emptyList(),
    val reservationsTables: List<Reservation_Table_Entity> = emptyList(),
    val selectedDate: LocalDate = LocalDate.now(),
    val selectedTime: LocalTime = LocalTime.now(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val tableStatuses: Map<String, TableStatus> = emptyMap(),
    val capturedFiles: List<File> = emptyList(),
    val captureRequested: Boolean = false,
    val capturedBitmaps: Map<String, Bitmap> = emptyMap(),
    var tableStatus:String="",
    var isCapturing:Boolean=false,
    val selectedTables: List<TableEntity> = emptyList(),



)
