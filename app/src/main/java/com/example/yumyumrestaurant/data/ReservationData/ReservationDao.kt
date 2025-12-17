package com.example.yumyumrestaurant.data.ReservationData

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.yumyumrestaurant.data.ReservationWithTables
import com.example.yumyumrestaurant.data.TableData.TableEntity
import java.time.LocalDate


@Dao
interface ReservationDao {

    // Get all tables
    @Query("SELECT * FROM Reservations")
    fun getAllReservations():kotlinx.coroutines.flow.Flow<List<ReservationEntity>>



    // Get table by ID
    @Query("SELECT * FROM Reservations WHERE reservationId = :reservationId LIMIT 1")
    suspend fun getReservationIdById(reservationId: String): ReservationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReservation(table: ReservationEntity)

    @Query("DELETE FROM Reservations")
    suspend fun clearAllReservations()



    @Query("""
        SELECT R.*
        FROM Reservations R
        INNER JOIN Reservation_Table AS X ON R.reservationId = X.reservationId
        WHERE X.tableId = :tableId 
          AND R.date = :date 
          AND R.reservationStatus = 'CONFIRMED'
    """)
    suspend fun getConfirmedReservationsForTableOnDate(tableId: String, date: LocalDate): List<ReservationEntity>
}
