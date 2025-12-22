package com.example.yumyumrestaurant.data.Order

import com.example.yumyumrestaurant.OrderProcess.MenuItemUiState
import com.example.yumyumrestaurant.OrderProcess.OrderItemUiState
import com.example.yumyumrestaurant.OrderProcess.OrderUiState
import com.example.yumyumrestaurant.data.Menu.MenuDataSource

class OrderRepository(
    private val orderDataSource: OrderDataSource,
    private val menuDataSource: MenuDataSource
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

    suspend fun getMenuItemById(menuItemID: String): MenuItemUiState? {
        return menuDataSource.getMenuItemById(menuItemID)
    }


    suspend fun getPayment(paymentID: String): Payment? {
        return orderDataSource.getPayment(paymentID)
    }

    suspend fun getItemsByReservationId(resId: String): List<OrderItemUiState> {
        val orderedItems = orderDataSource.getOrderItemsByReservationId(resId)
        val resultList = mutableListOf<OrderItemUiState>()

        for (orderEntry in orderedItems) {
            // Suspend call is allowed here!
            val menuDetail = menuDataSource.getMenuItemById(orderEntry.menuItemID)

            if (menuDetail != null) {
                resultList.add(
                    OrderItemUiState(
                        menuItem = menuDetail,
                        quantity = orderEntry.quantity,
                        specialNotes = orderEntry.remark ?: ""
                    )
                )
            }
        }
        return resultList
    }


}