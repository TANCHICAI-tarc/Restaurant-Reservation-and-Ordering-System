package com.example.yumyumrestaurant.data.ReservationTableData

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow


@Dao
interface ReservationTableDao {


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReservationTable(reservationTable: Reservation_Table_Entity)

    @Query("SELECT * FROM Reservation_Table")
    fun getAllReservationsTables(): Flow<List<Reservation_Table_Entity>>

    @Query("DELETE FROM Reservation_Table")
    suspend fun clearAllReservationsTables()


}