package com.example.yumyumrestaurant.data.TableData


import com.example.yumyumrestaurant.data.ReservationTableData.Reservation_Table_Entity
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await

class TableRepository(private val tableDao: TableDao? = null) {

    private val firestore = FirebaseFirestore.getInstance()

    val allTables: Flow<List<TableEntity>> = tableDao?.getAllTables() ?: flowOf(emptyList())

    suspend fun insertTable(table: TableEntity) {
        tableDao?.insertTable(table)
        firestore.collection("Tables").document(table.tableId).set(table).await()
    }


    suspend fun getTableForReservation(reservationID: String): List<TableEntity> {
        val linkSnapshot = firestore
            .collection("Reservation_Table")
            .whereEqualTo("reservationId", reservationID)
            .get()
            .await()

        val tableIds = linkSnapshot.toObjects(Reservation_Table_Entity::class.java)
            .map { it.tableId }

        if (tableIds.isEmpty()) return emptyList()

        val tableSnapshot = firestore
            .collection("Tables")
            .whereIn(FieldPath.documentId(), tableIds)
            .get()
            .await()

        return tableSnapshot.toObjects(TableEntity::class.java)
    }

    // Fetch all from Firestore and save to Room
    suspend fun syncTablesFromFirebase() {
        val snapshot = firestore.collection("Tables").get().await()
        val tables = snapshot.documents.mapNotNull {
            it.toObject(TableEntity::class.java)?.copy(tableId = it.id)
        }
        tableDao?.clearAllTables()
        tables.forEach { table ->
            tableDao?.insertTable(table)
        }
    }

    suspend fun getTableById(tableId: String): TableEntity? {
        return tableDao?.getTableById(tableId)
    }


    suspend fun updateTable(table: TableEntity) {
        tableDao?.updateTable(table)
        firestore.collection("Tables").document(table.tableId).set(table).await()
    }


}
