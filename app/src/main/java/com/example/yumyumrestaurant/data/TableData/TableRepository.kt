package com.example.yumyumrestaurant.data.TableData


import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

class TableRepository(private val tableDao: TableDao) {

    private val firestore = FirebaseFirestore.getInstance()

    val allTables: Flow<List<TableEntity>> = tableDao.getAllTables()

    // Insert locally and in Firestore
    suspend fun insertTable(table: TableEntity) {
        tableDao.insertTable(table)
        firestore.collection("Tables").document(table.tableId).set(table).await()
    }

    // Fetch all from Firestore and save to Room
    suspend fun syncTablesFromFirebase() {
        val snapshot = firestore.collection("Tables").get().await()
        val tables = snapshot.documents.mapNotNull {
            it.toObject(TableEntity::class.java)?.copy(tableId = it.id)
        }
        tableDao.clearAllTables()
        tables.forEach { table ->
            tableDao.insertTable(table)
        }
    }

    suspend fun getTableById(tableId: String): TableEntity? {
        return tableDao.getTableById(tableId)
    }


    suspend fun updateTable(table: TableEntity) {
        tableDao.updateTable(table)
        firestore.collection("Tables").document(table.tableId).set(table).await()
    }


}
