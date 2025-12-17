package com.example.yumyumrestaurant.data.Order

import com.example.yumyumrestaurant.OrderProcess.OrderItemUiState

class OrderRepository(
    private val orderDataSource: OrderDataSource
) {

    suspend fun placeOrder(
        items: List<OrderItemUiState>,
        totalAmount: Double,
        paymentMethod: String,
        reservationID: String
    ) {
        val orderID = orderDataSource.generateOrderID()
        val paymentID = orderDataSource.generatePaymentID()

        orderDataSource.saveOrder(
            orderID = orderID,
            totalAmount = totalAmount,
            reservationID = reservationID,
            paymentID = paymentID
        )

        orderDataSource.saveMenuOrder(
            orderID = orderID,
            items = items
        )

        orderDataSource.savePayment(
            paymentID = paymentID,
            method = paymentMethod,
            amount = totalAmount
        )
    }

    suspend fun getAllOrders(): List<Order> {
        return orderDataSource.getOrders()
    }

    suspend fun getMenuOrders(orderID: String): List<MenuOrder> {
        return orderDataSource.getMenuOrders(orderID)
    }

    suspend fun getPayment(paymentID: String): Payment? {
        return orderDataSource.getPayment(paymentID)
    }

}