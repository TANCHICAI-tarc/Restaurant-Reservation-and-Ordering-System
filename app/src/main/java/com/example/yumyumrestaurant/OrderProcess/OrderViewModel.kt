package com.example.yumyumrestaurant.OrderProcess

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yumyumrestaurant.Reservation.ReservationViewModel
import com.example.yumyumrestaurant.data.Menu.MenuDataSource
import com.example.yumyumrestaurant.data.Order.OrderDataSource
import com.example.yumyumrestaurant.data.Order.OrderRepository
import com.example.yumyumrestaurant.data.ReservationData.ReservationEntity
import com.example.yumyumrestaurant.data.ReservationData.ReservationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class OrderViewModel(
    private val orderRepository: OrderRepository = OrderRepository(OrderDataSource(), MenuDataSource())
) : ViewModel() {
    private val _orderUiState = MutableStateFlow(OrderUiState())
    val orderUiState: StateFlow<OrderUiState> = _orderUiState.asStateFlow()

    private val _navigateToSuccess = MutableStateFlow(false)
    val navigateToSuccess: StateFlow<Boolean> = _navigateToSuccess.asStateFlow()

    private val _showConfirmDialog = MutableStateFlow(false)
    val showConfirmDialog: StateFlow<Boolean> = _showConfirmDialog

    fun addToCart(menuItem: MenuItemUiState, quantity: Int, specialNotes: String) {
        val currentItems = _orderUiState.value.selectedItems.toMutableList()

        // Look for existing item
        val existingIndex = currentItems.indexOfFirst {
            it.menuItem == menuItem && it.specialNotes == specialNotes
        }

        if (existingIndex != -1) {
            // Update item using copy (because data classes are immutable)
            val existing = currentItems[existingIndex]
            val updated = existing.copy(
                quantity = existing.quantity + quantity,
                specialNotes = specialNotes
            )
            currentItems[existingIndex] = updated

        } else {
            // Add new item
            currentItems.add(
                OrderItemUiState(
                    menuItem = menuItem,
                    quantity = quantity,
                    specialNotes = specialNotes
                )
            )
        }

        updateOrderState(currentItems)
    }

    fun removeItem(orderItem: OrderItemUiState) {
        val updatedList = _orderUiState.value.selectedItems.filterNot {
            it.menuItem == orderItem.menuItem && it.specialNotes == orderItem.specialNotes
        }
        updateOrderState(updatedList)
    }

    fun updateItemQuantity(orderItem: OrderItemUiState, newQuantity: Int) {
        val currentItems = _orderUiState.value.selectedItems.toMutableList()
        val index = currentItems.indexOfFirst {
            it.menuItem == orderItem.menuItem && it.specialNotes == orderItem.specialNotes
        }

        if (index != -1) {
            val oldItem = currentItems[index]
            currentItems[index] = oldItem.copy(quantity = newQuantity)
            updateOrderState(currentItems)
        }

    }

    fun clearCart() {
        updateOrderState(emptyList())
    }

    fun getTotalItemQuantity(): Int {
        return _orderUiState.value.selectedItems.sumOf { it.quantity }
    }

    fun getItemQuantity(menuItem: MenuItemUiState): Int {
        return _orderUiState.value.selectedItems.find { it.menuItem == menuItem }?.quantity ?: 0
    }
    private fun updateOrderState(items: List<OrderItemUiState>) {
        val subTotal = items.sumOf { it.menuItem.price * it.quantity }
        val taxRate = 0.06
        val taxAmount = subTotal * taxRate
        val finalTotal = subTotal + taxAmount

        _orderUiState.value = _orderUiState.value.copy(
            selectedItems = items,
            totalPrice = finalTotal,
            subTotal = subTotal,
            taxAmount = taxAmount
        )
    }

    fun setPaymentOption(option: String) {
        _orderUiState.value = _orderUiState.value.copy(paymentOption = option)
    }

    fun updateCardNumber(number: String) {
        val error = number.length != 16
        _orderUiState.value = _orderUiState.value.copy(
            cardNumber = number,
            cardNumberError = error
        )
        validatePayment()
    }

    fun updateExpMonth(month: String) {
        val intMonth = month.toIntOrNull()
        val state = _orderUiState.value
        val expYear = state.expYear.toIntOrNull()
        val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR) % 100
        val currentMonth = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH) + 1

        val error = when {
            intMonth == null || intMonth !in 1..12 -> true
            expYear != null && (expYear < currentYear || (expYear == currentYear && intMonth < currentMonth)) -> true
            else -> false
        }

        _orderUiState.value = state.copy(
            expMonth = month,
            expMonthError = error
        )
        validatePayment()
    }

    fun updateExpYear(year: String) {
        val intYear = year.toIntOrNull()
        val state = _orderUiState.value
        val expMonth = state.expMonth.toIntOrNull()
        val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR) % 100
        val currentMonth = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH) + 1
        val maxFutureYear = (currentYear + 10) % 100

        val error = when {
            intYear == null || year.length != 2 -> true
            intYear < currentYear -> true
            intYear > maxFutureYear -> true
            intYear == currentYear && expMonth != null && expMonth < currentMonth -> true
            else -> false
        }

        _orderUiState.value = state.copy(
            expYear = year,
            expYearError = error
        )
        validatePayment()
    }

    fun updateCvv(cvv: String) {
        val error = cvv.length !in 3..4
        _orderUiState.value = _orderUiState.value.copy(
            cvv = cvv,
            cvvError = error
        )
        validatePayment()
    }

    private fun validatePayment() {
        val state = _orderUiState.value
        val isValid = when (state.paymentOption) {
            "Credit Card" -> !state.cardNumberError &&
                    !state.expMonthError &&
                    !state.expYearError &&
                    !state.cvvError &&
                    state.cardNumber.isNotBlank() &&
                    state.expMonth.isNotBlank() &&
                    state.expYear.isNotBlank() &&
                    state.cvv.isNotBlank()
            "E-Wallet" -> true
            else -> false
        }
        _orderUiState.value = _orderUiState.value.copy(isPaymentValid = isValid)
    }

    fun canConfirmPayment(): Boolean {
        return _orderUiState.value.isPaymentValid
    }

    fun requestOrderConfirmation() {
        _showConfirmDialog.value = true
    }

    fun dismissOrderConfirmation() {
        _showConfirmDialog.value = false
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun confirmOrderAndProceed(reservationID: String) {
        confirmOrder(reservationID)
    }

    fun startOrdering() {
        _showConfirmDialog.value = false
        _orderUiState.update {
            it.copy(isOrdering = true)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun confirmOrder(
        reservationID: String
    ) {
        val state = _orderUiState.value

        if(!state.isPaymentValid || state.selectedItems.isEmpty()) {
            return
        }

        _orderUiState.value = _orderUiState.value.copy(isOrdering = true)

        viewModelScope.launch {
            try{
                orderRepository.placeOrder(
                    items = state.selectedItems,
                    totalAmount = state.totalPrice,
                    paymentMethod = state.paymentOption,
                    reservationID = reservationID
                )

                _orderUiState.value = _orderUiState.value.copy(
                    orderSuccess = true,
                    isOrdering = false
                )

                _navigateToSuccess.value = true

                clearCart()

            } catch (e: Exception) {
                _orderUiState.value = _orderUiState.value.copy(
                    orderSuccess = false,
                    isOrdering = false
                )
            }
        }
    }

    fun resetNavigation() {
        _navigateToSuccess.value = false
    }




    fun loadOrderItemsByReservationId(resId: String) {
        viewModelScope.launch {

            val items = orderRepository.getItemsByReservationId(resId)


            val subTotal = items. sumOf { item ->
                item.menuItem.price * item.quantity
            }
            val tax = subTotal * 0.06

            // 3. Update the state
            _orderUiState.update { currentState ->
                currentState.copy(
                    selectedItems = items,
                    subTotal = subTotal,
                    taxAmount = tax,
                    totalPrice = subTotal + tax
                )
            }
        }
    }
}