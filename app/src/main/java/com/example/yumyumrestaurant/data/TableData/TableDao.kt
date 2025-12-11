package com.example.yumyumrestaurant.data.TableData

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TableDao {

    // Get all tables
    @Query("SELECT * FROM Tables")
    fun getAllTables():kotlinx.coroutines.flow.Flow<List<TableEntity>>

    // Get latest table by tableId
    @Query("SELECT * FROM Tables ORDER BY tableId DESC LIMIT 1")
    suspend fun getLatestTable(): TableEntity?

    // Insert table
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTable(table: TableEntity)

    // Update table
    @Update
    suspend fun updateTable(table: TableEntity)

    // Delete table
//    @Delete
//    suspend fun deleteTable(table: TableEntity)

    // Clear all tables
    @Query("DELETE FROM Tables")
    suspend fun clearAllTables()

    // Get table by ID
    @Query("SELECT * FROM Tables WHERE tableId = :tableId LIMIT 1")
    suspend fun getTableById(tableId: String): TableEntity?
}
