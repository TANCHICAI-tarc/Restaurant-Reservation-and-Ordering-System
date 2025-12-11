package com.example.yumyumrestaurant.TableSelectionScreen

import androidx.compose.ui.geometry.Offset

data class InteractionUiState(
    val scale: Float = 1f,
    val offset: Offset = Offset.Zero,
    val moveDistance: Float = 0f,
    val isMultiTouch: Boolean = false
)
