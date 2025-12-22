package com.example.yumyumrestaurant.data.ReservationData

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.yumyumrestaurant.data.ReservationTableData.Reservation_Table_Entity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate


@Dao
interface ReservationDao {

    // Get all tables
    @Query("SELECT * FROM Reservations")
    fun getAllReservations():kotlinx.coroutines.flow.Flow<List<ReservationEntity>>


    @Update
    suspend fun updateReservationStatus(reservations: ReservationEntity)
    // Get table by ID
    @Query("SELECT * FROM Reservations WHERE reservationId = :reservationId LIMIT 1")
    suspend fun getReservationIdById(reservationId: String): ReservationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReservation(reservation: ReservationEntity)

    @Query("DELETE FROM Reservations")
    suspend fun clearAllReservations()

    @Query("SELECT * FROM reservations WHERE reservationId = :resId")
    suspend fun getReservationById(resId: String): ReservationEntity?


    @Query("""
        SELECT Reservations.* FROM Reservations 
    INNER JOIN Reservation_Table ON Reservations.reservationId = Reservation_Table.reservationId
    WHERE Reservation_Table.tableId = :tableId 
      AND Reservations.date = :date 
      AND Reservations.reservationStatus = 'Confirmed'
    """)
    suspend fun getConfirmedReservationsForTableOnDate(tableId: String, date: String): List<ReservationEntity>
}
