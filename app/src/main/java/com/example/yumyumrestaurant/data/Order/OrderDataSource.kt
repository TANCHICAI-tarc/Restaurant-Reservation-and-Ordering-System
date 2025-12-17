package com.example.yumyumrestaurant.data.Order

import com.example.yumyumrestaurant.OrderProcess.OrderItemUiState
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class OrderDataSource {
    private val db = FirebaseFirestore.getInstance()

    suspend fun generateOrderID(): String {
        val documentRef = db.collection("Counters").document("order_counter")

        return db.runTransaction { transaction ->
            val snapshot = transaction.get(documentRef)
            val lastOrder = snapshot.getLong("lastOrder") ?: 0
            val newOrder = lastOrder + 1

            transaction.update(documentRef, "lastOrder", newOrder)

            String.format("OD%04d", newOrder)
        }.await()
    }

    suspend fun generatePaymentID(): String {
        val documentRef = db.collection("Counters").document("payment_counter")

        return db.runTransaction { transaction ->
            val snapshot = transaction.get(documentRef)
            val lastPayment = snapshot.getLong("lastPayment") ?: 0
            val newPayment = lastPayment + 1

            transaction.update(documentRef, "lastPayment", newPayment)

            String.format("P%04d", newPayment)
        }.await()
    }

    suspend fun saveOrder(
        orderID: String,
        totalAmount: Double,
        reservationID: String,
        paymentID: String
    ) {
        val orderData = hashMapOf(
            "TotalAmount" to totalAmount,
            "OrderStatus" to "Confirmed",
            "PaymentID" to paymentID,
            "ReservationID" to reservationID,
        )

        db.collection("Orders")
            .document(orderID)
            .set(orderData)
            .await()
    }

    suspend fun saveMenuOrder(
        orderID: String,
        items: List<OrderItemUiState>
    ) {
        val batch = db.batch()
        val menuOrderCollection = db.collection("Menu_Order")

        for (item in items) {
            val doc = menuOrderCollection.document()
            val data = hashMapOf(
                "OrderID" to orderID,
                "MenuItemID" to item.menuItem.menuID,
                "Quantity" to item.quantity,
                "Remark" to item.specialNotes
            )
            batch.set(doc, data)
        }

        batch.commit().await()
    }

    suspend fun savePayment(
        paymentID: String,
        method: String,
        amount: Double
    ) {
        val paymentData = hashMapOf(
            "PaymentMethod" to method,
            "PaymentDate" to Timestamp.now(),
            "AmountPaid" to amount,
            "PaymentStatus" to "Completed"
        )

        db.collection("Payments")
            .document(paymentID)
            .set(paymentData)
            .await()
    }

    suspend fun getOrders(): List<Order> {
        return db.collection("Orders")
            .get()
            .await()
            .toObjects(Order::class.java)
    }

    suspend fun getMenuOrders(orderID: String): List<MenuOrder> {
        return db.collection("Menu_Order")
            .whereEqualTo("OrderID", orderID)
            .get()
            .await()
            .toObjects(MenuOrder::class.java)
    }

    suspend fun getPayment(paymentID: String): Payment? {
        val snapshot = db.collection("Payments")
            .document(paymentID)
            .get()
            .await()

        return snapshot.toObject(Payment::class.java)
    }

    suspend fun updateOrderStatus(orderID: String, newStatus: String) {
        db.collection("Orders")
            .document(orderID)
            .update("OrderStatus", newStatus)
            .await()
    }
}