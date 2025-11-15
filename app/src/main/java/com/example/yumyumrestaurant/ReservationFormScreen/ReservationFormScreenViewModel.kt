package com.example.yumyumrestaurant.ReservationFormScreen


import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.*


import kotlinx.coroutines.launch

import android.net.Uri
import android.util.Log

import androidx.core.content.FileProvider
import com.example.yumyumrestaurant.data.ReservationFormScreen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


class ReservationFormScreenViewModel(application: Application) : AndroidViewModel(application) {



    private val _uiState = MutableStateFlow(ReservationFormScreenUiState())
    val uiState: StateFlow<ReservationFormScreenUiState> = _uiState.asStateFlow()


//    private val repository: LostItemRepository
//
//    var roomIdList by mutableStateOf<List<String>>(emptyList())
//        private set
//
//    val allLostItems: LiveData<List<LostItemEntity>>
//
//    init {
//        val lostItemDao = HotelLostItemDatabase.getLostItemDatabase(application).lostItemDao()
//        repository = LostItemRepository(lostItemDao)
//
//        allLostItems = repository.allLostItems.asLiveData()
//
//
//
//
//        viewModelScope.launch {
//
//            //  Step 1: Use Room first to generate ID and get room list
//
//
//            roomIdList = repository.getAllRoomIds()
//
//            // Step 2: Then sync from Firestore in background
//            repository.syncFromFirestore()
//        }
//    }


    // Example of updating fields:
//    fun setTitleChange(newTitle: String) {
//        _uiState.update { currentState ->
//            currentState.copy(itemTitle = newTitle)
//        }
//    }
//
//
//
//
//
//    fun setFoundLocationChange(newLocation: String) {
//        _uiState.value = _uiState.value.copy(selectedLocation  = newLocation)
//    }
//
//    fun setDateFoundChange(newDate: String) {
//        _uiState.value = _uiState.value.copy(dateFound = newDate)
//    }
//
//    fun setTimeFoundChange(newTime: String) {
//        _uiState.value = _uiState.value.copy(timeFound = newTime)
//    }





}








