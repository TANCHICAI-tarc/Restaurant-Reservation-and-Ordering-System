package com.example.yumyumrestaurant.data.ReservationData

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.yumyumrestaurant.data.ReservationTableData.Reservation_Table_Entity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate


@Dao
interface ReservationDao {

    // Get all tables
    @Query("SELECT * FROM Reservations")
    fun getAllReservations():kotlinx.coroutines.flow.Flow<List<ReservationEntity>>

    @Query("UPDATE Reservations SET reservationStatus = :status WHERE reservationId = :id")
    suspend fun updateReservationStatus(id: String, status: String)

    // Get table by ID
    @Query("SELECT * FROM Reservations WHERE reservationId = :reservationId LIMIT 1")
    suspend fun getReservationIdById(reservationId: String): ReservationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReservation(reservation: ReservationEntity)

    @Query("DELETE FROM Reservations")
    suspend fun clearAllReservations()




    @Query("""
        SELECT Reservations.* FROM Reservations 
    INNER JOIN Reservation_Table ON Reservations.reservationId = Reservation_Table.reservationId
    WHERE Reservation_Table.tableId = :tableId 
      AND Reservations.date = :date 
      AND Reservations.reservationStatus = 'Confirmed'
    """)
    suspend fun getConfirmedReservationsForTableOnDate(tableId: String, date: String): List<ReservationEntity>
}
