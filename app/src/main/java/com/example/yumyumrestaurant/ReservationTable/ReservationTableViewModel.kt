package com.example.yumyumrestaurant.ReservationTable

import android.app.Application
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Typeface
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.stayeasehotel.data.regiondata.RegionDatabase
import com.example.yumyumrestaurant.Reservation.ReservationViewModel
import com.example.yumyumrestaurant.TableSelectionScreen.TableUiState
import com.example.yumyumrestaurant.TableSelectionScreen.TableViewModel
import com.example.yumyumrestaurant.data.RegionData.RegionRepository
import com.example.yumyumrestaurant.data.ReservationData.ReservationRepository
import com.example.yumyumrestaurant.data.ReservationTableData.ReservationTableRepository
import com.example.yumyumrestaurant.data.ReservationTableData.Reservation_TableDatabase
import com.example.yumyumrestaurant.data.TableData.TableEntity
import com.example.yumyumrestaurant.data.TableData.TableRepository
import com.example.yumyumrestaurant.data.TableData.TableStatus
import kotlinx.coroutines.flow.MutableSharedFlow

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

sealed class CapturePurpose {
    object Share : CapturePurpose()
    object ReviewOnly : CapturePurpose()
}


class ReservationTableViewModelFactory(
    private val tableViewModel: TableViewModel,
    private val reservationViewModel: ReservationViewModel,

    private val reservationTableRepository: ReservationTableRepository

) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReservationTableViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ReservationTableViewModel(tableViewModel, reservationViewModel,reservationTableRepository) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
    }
}

class ReservationTableViewModel(
    private val tableVM: TableViewModel,
    private val reservationVM: ReservationViewModel,
    private val reservationTableRepository: ReservationTableRepository

) : ViewModel() {
    private val _isReservationInProgress = MutableStateFlow(false)
    val isReservationInProgress = _isReservationInProgress.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    private val _reservationTableUiState = MutableStateFlow(ReservationTableUiState())
    val reservationTableUiState: StateFlow<ReservationTableUiState> = _reservationTableUiState
    val reservationViewModel get() = reservationVM
    val tableViewModel get() = tableVM

    val reservationUiState = reservationVM.uiState
    val tableUiState = tableViewModel.uiState


    private val _captureTrigger = MutableSharedFlow<CapturePurpose>()
    val captureTrigger = _captureTrigger.asSharedFlow()



    private val _capturedFiles = MutableStateFlow<List<File>>(emptyList())
    val capturedFiles: StateFlow<List<File>> = _capturedFiles.asStateFlow()


    fun triggerCapture(purpose: CapturePurpose) {

        viewModelScope.launch {
            _captureTrigger.emit(purpose)


        }


    }



    init {
        viewModelScope.launch {
            reservationTableRepository.syncReservationsTablesFromFirebase()
        }
        viewModelScope.launch {
            reservationTableRepository.allReservationsTables.collect { reservationsTables ->
                _reservationTableUiState.update { currentState ->
                    currentState.copy(reservationsTables = reservationsTables)
                }
            }

        }

    }


    fun refreshTableStatuses(
        date: LocalDate,
        startTime: LocalTime,
        durationMinutes: Int
    ) {
        val start = LocalDateTime.of(date, startTime)
        val end = start.plusMinutes(durationMinutes.toLong())

        val tables = tableViewModel.uiState.value.tables
        val reservations = reservationViewModel.uiState.value.reservations
        val reservationTableLinks = reservationTableUiState.value.reservationsTables

        val updatedTables = tables.map { table ->


            val reservationIdsForTable = reservationTableLinks
                .filter { it.tableId == table.tableId }
                .map { it.reservationId }


            val reservationsForTable = reservations
                .filter { it.reservationId in reservationIdsForTable }


            val isReserved = reservationsForTable.any { reservation ->
                val reservationStart = LocalDateTime.of(
                    LocalDate.parse(reservation.date),
                    LocalTime.parse(reservation.startTime)
                )

                val reservationEndWithBuffer =
                    reservationStart
                        .plusMinutes(reservation.durationMinutes.toLong())
                        .plusMinutes(60)

                start < reservationEndWithBuffer && reservationStart < end
            }

            table.copy(
                status = if (isReserved) TableStatus.RESERVED else TableStatus.AVAILABLE
            )
        }

        tableViewModel.updateTables(updatedTables)

    }

    fun updateCapturedFiles(files: List<File>) {
        _capturedFiles.value = files
    }

    fun saveBitmapsToCache(
        context: Context,
        bitmaps: List<Bitmap>
    ): List<File> {
        val savedFiles = mutableListOf<File>()
        try {
            bitmaps.forEachIndexed { index, originalBitmap ->
                val file = File(context.cacheDir, "reservation_plan_$index.jpg")
                val softwareBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, false)

                FileOutputStream(file).use {
                    softwareBitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
                }
                savedFiles.add(file)
            }
        } catch (e: Exception) {
            Log.e("SaveError", "${e.message}")
        }
        return savedFiles
    }


    fun setCapturing(isCapturing: Boolean) {
        _reservationTableUiState.update {
            it.copy(isCapturing = isCapturing)
        }

    }





    fun saveReservationWithTables(selectedTables: List<TableEntity>, reservationId: String) {


        viewModelScope.launch {
            selectedTables.forEach { table ->
                reservationTableRepository.insertReservationTableCrossRef(
                    reservationId = reservationId,
                    tableId = table.tableId
                )
            }
        }
    }

    fun shareBitmaps(
        context: Context,
        bitmaps: List<Bitmap>,
        zones: List<String>,
        reservationTableViewModel: ReservationTableViewModel
    ) {
        viewModelScope.launch {
            try {

                val contentUris = ArrayList<Uri>()
                val scaleFactor = 2.0f

                val timestamp = java.time.LocalDateTime.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))

                bitmaps.forEachIndexed { index, originalBitmap ->
                    val softwareBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, false)

                    val newWidth = (softwareBitmap.width * scaleFactor).toInt()
                    val newHeight = (softwareBitmap.height * scaleFactor).toInt()


                    val finalBitmap =
                        Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888)
                    val canvas = android.graphics.Canvas(finalBitmap)
                    canvas.drawColor(android.graphics.Color.WHITE)


                    val matrix = android.graphics.Matrix()
                    matrix.postScale(scaleFactor, scaleFactor)


                    val filterPaint = android.graphics.Paint().apply { isFilterBitmap = true }
                    canvas.drawBitmap(softwareBitmap, matrix, filterPaint)


                    val footerPaint = android.graphics.Paint().apply {
                        color = android.graphics.Color.GRAY
                        textSize = 30f * scaleFactor // Scale font size with image
                        isAntiAlias = true
                        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    }
                    canvas.drawText(
                        "Generated on: $timestamp",
                        40f * scaleFactor,
                        newHeight - (40f * scaleFactor),
                        footerPaint
                    )


                    val file = File(context.cacheDir, "reservation_plan_$index.jpg")
                    FileOutputStream(file).use {

                        finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
                    }
                    contentUris.add(
                        FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            file
                        )
                    )
                }


                val resState = reservationTableViewModel.reservationUiState.value
                val tableUiState = reservationTableViewModel.tableUiState.value

                val tablesByZone = tableUiState.selectedTables
                    .groupBy { it.zone.uppercase() }
                    .map { (zone, tables) ->
                        "${
                            zone.lowercase().replaceFirstChar { it.uppercase() }
                        }: ${tables.joinToString(", ") { it.label }}"
                    }
                    .joinToString(" | ")


                val zoneDescription = if (zones.size > 1) {
                    "Indoor and Outdoor sections"
                } else {
                    "${zones[0].lowercase().replaceFirstChar { it.uppercase() }} section"
                }

                val shareMessage = """
            🍽️ YumYum Restaurant Reservation
            
            📅 Date: ${resState.selectedDate}
            ⏰ Time: ${resState.selectedStartTime} - ${resState.selectedEndTime}
            👥 Guests: ${resState.guestCount}
            🪑 Tables: $tablesByZone
            
            📍 Attached is the floor plan for the $zoneDescription.
            
            _________________________
            📝 Generated on: $timestamp
        """.trimIndent()

                val intent = if (contentUris.size > 1) {
                    Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                        type = "image/jpeg"
                        putParcelableArrayListExtra(Intent.EXTRA_STREAM, contentUris)
                    }
                } else {
                    Intent(Intent.ACTION_SEND).apply {
                        type = "image/jpeg"
                        putExtra(Intent.EXTRA_STREAM, contentUris[0])
                    }
                }

                intent.apply {
                    putExtra(Intent.EXTRA_TEXT, shareMessage)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                context.startActivity(Intent.createChooser(intent, "Share Reservation"))
            } catch (e: Exception) {
                Log.e("ShareError", "Error: ${e.message}")
            }
        }
    }

    suspend fun saveCapturedBitmaps(
        context: Context,
        bitmaps: List<Bitmap>
    ): List<File> = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        if (bitmaps.isEmpty()) return@withContext emptyList<File>()

        val savedFiles = mutableListOf<File>()
        val scaleFactor = 2.0f
        val timestamp = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))

        bitmaps.forEachIndexed { index, originalBitmap ->
            try {
                // 1. Ensure we have a software-backed copy
                val softwareBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, false)

                // 2. Prepare the high-resolution canvas
                val newWidth = (softwareBitmap.width * scaleFactor).toInt()
                val newHeight = (softwareBitmap.height * scaleFactor).toInt()
                val finalBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888)

                val canvas = android.graphics.Canvas(finalBitmap)
                canvas.drawColor(android.graphics.Color.WHITE)


                val matrix = Matrix()
                matrix.postScale(scaleFactor, scaleFactor)
                val filterPaint = Paint().apply { isFilterBitmap = true }
                canvas.drawBitmap(softwareBitmap, matrix, filterPaint)


                val footerPaint = Paint().apply {
                    color = android.graphics.Color.GRAY
                    textSize = 25f * scaleFactor
                    isAntiAlias = true
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                }
                canvas.drawText(
                    "Generated on: $timestamp",
                    40f * scaleFactor,
                    newHeight - (40f * scaleFactor),
                    footerPaint
                )

                // 5. Save to Disk
                val file = File(context.cacheDir, "reservation_plan_$index.jpg")
                FileOutputStream(file).use { out ->
                    finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
                    out.flush() // Force write completion
                }

                savedFiles.add(file)


            } catch (e: Exception) {

            }
        }

        return@withContext savedFiles
    }



}






