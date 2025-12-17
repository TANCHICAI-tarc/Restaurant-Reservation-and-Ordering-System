package com.example.yumyumrestaurant.OrderProcess

data class OrderUiState(
    val selectedItems: List<OrderItemUiState> = emptyList(),
    val totalPrice: Double = 0.0,
    val subTotal: Double = 0.0,
    val taxAmount: Double = 0.0,
    val paymentOption: String = "Credit Card",
    val cardNumber: String = "",
    val expMonth: String = "",
    val expYear: String = "",
    val cvv: String = "",
    val isOrdering: Boolean = false,
    val isPaymentValid: Boolean = false,
    val orderSuccess: Boolean = false,
    val cardNumberError: Boolean = false,
    val expMonthError: Boolean = false,
    val expYearError: Boolean = false,
    val cvvError: Boolean = false,
    val showConfirmDialog: Boolean = false
)