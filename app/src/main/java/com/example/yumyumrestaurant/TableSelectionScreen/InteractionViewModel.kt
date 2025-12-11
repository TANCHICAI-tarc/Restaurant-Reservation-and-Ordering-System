package com.example.yumyumrestaurant.TableSelectionScreen

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.math.min

class InteractionViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(InteractionUiState())
    val uiState: StateFlow<InteractionUiState> = _uiState.asStateFlow()

    var scale by mutableStateOf(1f)
        private set

    var offset by mutableStateOf(Offset.Zero)
        private set

    fun onScale(newScale: Float) {
        _uiState.update { it.copy(scale = newScale) }
    }

    fun onOffset(newOffset: Offset) {
        _uiState.update { it.copy(offset = newOffset) }
    }

    fun onMoveDistance(value: Float) {
        _uiState.update { it.copy(moveDistance = value) }
    }

    fun resetMove() {
        _uiState.update { it.copy(moveDistance = 0f) }
    }

    fun setMultiTouch(value: Boolean) {
        _uiState.update { it.copy(isMultiTouch = value) }
    }


    fun onPinchGesture(
        zoomFactor: Float,
        pan: Offset,
        centroid: Offset,
        boxWidthPx: Float,
        boxHeightPx: Float
    ) {
        val newScale = (uiState.value.scale * zoomFactor).coerceIn(1f, 4f)

        val newOffset = (uiState.value.offset - centroid) * (newScale / uiState.value.scale) + centroid + pan

        val scaledWidth = boxWidthPx * newScale
        val scaledHeight = boxHeightPx * newScale

        val minOffsetX = min(boxWidthPx - scaledWidth, 0f)
        val minOffsetY = min(boxHeightPx - scaledHeight, 0f)

        val adjustedOffset = Offset(
            newOffset.x.coerceIn(minOffsetX, 0f),
            newOffset.y.coerceIn(minOffsetY, 0f)
        )

        updateScaleOffset(newScale, adjustedOffset)
    }

    fun onDragGesture(dragAmount: Offset, boxWidthPx: Float, boxHeightPx: Float) {
        if (uiState.value.scale <= 1f) return

        val scaledWidth = boxWidthPx * uiState.value.scale
        val scaledHeight = boxHeightPx * uiState.value.scale

        val minOffsetX = min(boxWidthPx - scaledWidth, 0f)
        val minOffsetY = min(boxHeightPx - scaledHeight, 0f)

        val newOffset = Offset(
            (uiState.value.offset.x + dragAmount.x).coerceIn(minOffsetX, 0f),
            (uiState.value.offset.y + dragAmount.y).coerceIn(minOffsetY, 0f)
        )

        updateScaleOffset(uiState.value.scale, newOffset)
    }

    fun addMoveDistance(delta: Offset) {
        val distance = delta.getDistance()
        updateMoveDistance(uiState.value.moveDistance + distance)
    }

    fun updateMoveDistance(distance: Float) {
        _uiState.value = _uiState.value.copy(moveDistance = distance)
    }

    fun updateScaleOffset(newScale: Float, newOffset: Offset) {
        _uiState.value = _uiState.value.copy(scale = newScale, offset = newOffset)
    }

    fun resetMoveDistance() {
        _uiState.value = _uiState.value.copy(moveDistance = 0f)
    }



}
