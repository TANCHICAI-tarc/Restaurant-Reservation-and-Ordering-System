package com.example.yumyumrestaurant.TableSelectionScreen


import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.*
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.stayeasehotel.data.regiondata.RegionDatabase

import com.example.yumyumrestaurant.data.RegionData.RegionEntity
import com.example.yumyumrestaurant.data.RegionData.RegionRepository
import com.example.yumyumrestaurant.data.ReservationTableData.Reservation_TableDatabase
import com.example.yumyumrestaurant.data.TableData.TableEntity
import com.example.yumyumrestaurant.data.TableData.TableRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.pow
import kotlin.math.sqrt

sealed class TapResult {
    object PatioDoor : TapResult()
    data class TableTapped(val table: TableEntity) : TapResult()
    object None : TapResult()
}

class TableViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TableViewModel::class.java)) {
            return TableViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


@RequiresApi(Build.VERSION_CODES.O)
class TableViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(TableUiState())
    val uiState: StateFlow<TableUiState> = _uiState.asStateFlow()

    private val tableRepository : TableRepository
    private val regionRepository : RegionRepository


    init {

        val tableDao = Reservation_TableDatabase.getReservationTableDatabase(application).tableDao()
        tableRepository = TableRepository(tableDao)
        val regionDao = RegionDatabase.getRegionDatabase(application).regionDao()
        regionRepository = RegionRepository(regionDao)

        viewModelScope.launch {
            tableRepository.syncTablesFromFirebase() // sync Firebase -> Room
        }


        viewModelScope.launch {
            tableRepository.allTables.collect { tables ->
                _uiState.update { currentState ->
                    currentState.copy(tables = tables)
                }
            }

        }

        viewModelScope.launch {
            regionRepository.allRegions.collect { regions ->
                _uiState.update { currentState ->
                    currentState.copy(regions = regions)
                }
            }
        }

    }



    fun prepareShareFiles(context: Context, urls: List<String>) {
        viewModelScope.launch(Dispatchers.IO) { // Use IO thread
            // Use async to start all downloads/compressions at the same time
            val deferredFiles = urls.map { url ->
                async { getFileFromCoil(context, url) }
            }

            val files = deferredFiles.awaitAll().filterNotNull()

            _uiState.update { it.copy(preparedFiles = files) }
        }
    }

    fun clearPreparedFiles() {
        _uiState.update { it.copy(preparedFiles = emptyList()) }
    }
    suspend fun getFileFromCoil(context: Context, url: String): File? = withContext(Dispatchers.IO) {
        val loader = context.imageLoader
        val request = ImageRequest.Builder(context)
            .data(url)
            .allowHardware(false) // Important: Non-hardware bitmaps are easier to copy
            .build()

        val result = loader.execute(request)
        if (result is SuccessResult) {
            val bitmap = (result.drawable as? BitmapDrawable)?.bitmap
            if (bitmap != null) {
                // Create a temp file in the cache directory
                val file = File(context.cacheDir, "temp_share_${url.hashCode()}.jpg")
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
                }
                return@withContext file
            }
        }
        null
    }

    fun updateTables(newTables: List<TableEntity>) {
        Log.d("TableVM", "Tables updated! First table status: ${newTables.firstOrNull()?.status}")
        _uiState.update { it.copy(tables = newTables) }
    }

    fun onCanvasTap(
        x: Float,
        y: Float,
        boxWidth: Float,
        boxHeight: Float,
        zone: String
    ): TapResult {

        // Check regions first
        val region = findTappedRegion(x, y, boxWidth, boxHeight, zone)
        if (region?.label == "Patio\nDoor") {
            return TapResult.PatioDoor
        }

        // Check tables
        val table = findTappedTable(x, y, boxWidth, boxHeight, zone)
        if (table != null) {
            return TapResult.TableTapped(table)
        }

        return TapResult.None
    }


    fun loadTableById(tableId: String) {
        viewModelScope.launch {
            val table = tableRepository.getTableById(tableId)
            if (table != null) {
                _uiState.update { it.copy(selectedTable = table) }
            }
        }
    }


    fun distance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        return sqrt((x1 - x2).pow(2) + (y1 - y2).pow(2))
    }

    fun findNearRegions(
        table: TableEntity,
        regions: List<RegionEntity>,
        maxDistance: Float = 0.35f
    ): List<String> {
        val tableX = table.xAxis
        val tableY = table.yAxis

        return regions
            .filter { it.zone.uppercase() == table.zone.uppercase() }
            .map { region ->
                val centerX = region.xAxis + region.width / 2
                val centerY = region.yAxis + region.height / 2
                val dist = distance(tableX, tableY, centerX, centerY)

                region to dist
            }
            .filter { (_, dist) ->
                val result = dist <= maxDistance
                result
            }
            .sortedBy { it.second }
            .map { it.first.label }
    }


    fun findTappedTable(
        mapX: Float,
        mapY: Float,
        boxPxWidth: Float,
        boxPxHeight: Float,
        zone: String
    ): TableEntity? {
        return uiState.value.tables
            .filter { it.zone.uppercase() == zone }
            .find { t ->
                val px = t.xAxis * boxPxWidth
                val py = t.yAxis * boxPxHeight
                val dist = sqrt((mapX - px).pow(2) + (mapY - py).pow(2))
                dist < (t.radius + 60f)
            }
    }


    fun findTappedRegion(
        mapX: Float,
        mapY: Float,
        boxPxWidth: Float,
        boxPxHeight: Float,
        zone: String
    ): RegionEntity? {
        return uiState.value.regions
            .filter { it.zone.uppercase() == zone }
            .find { r ->
                val left = r.xAxis * boxPxWidth
                val top = r.yAxis * boxPxHeight
                val width = r.width * boxPxWidth
                val height = r.height * boxPxHeight

                mapX in left..(left + width) &&
                        mapY in top..(top + height)
            }
    }


    fun toggleTable(table: TableEntity) {
        val current = uiState.value.selectedTables.toMutableSet()
        if (current.contains(table)) current.remove(table)
        else current.add(table)
        _uiState.update { it.copy(selectedTables = current) }
    }

    fun removeTable(table: TableEntity) {
        val current = uiState.value.selectedTables.toMutableSet()
        current.remove(table)
        _uiState.update { it.copy(selectedTables = current) }
    }

    fun clearSelection() {
        _uiState.update { it.copy(selectedTables = emptySet()) }
    }



    fun requestClearAll() {
         if (_uiState.value.selectedTables.isNotEmpty()) {
            _uiState.update { it.copy(showClearAllConfirmation = true) }
        }
    }

    fun dismissClearAll() {
        _uiState.update { it.copy(showClearAllConfirmation = false) }
    }

    fun confirmClearAll() {
        _uiState.update {
            it.copy(
                selectedTables = emptySet(),
                showClearAllConfirmation = false
            )
        }
    }





}