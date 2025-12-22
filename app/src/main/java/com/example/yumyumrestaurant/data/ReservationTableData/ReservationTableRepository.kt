package com.example.yumyumrestaurant.data.ReservationTableData

import androidx.room.Query
import com.example.yumyumrestaurant.data.ReservationData.ReservationEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import java.time.LocalDate


class ReservationTableRepository(private val reservationTableDao: ReservationTableDao) {

    private val firestore = FirebaseFirestore.getInstance()

    val allReservationsTables: Flow<List<Reservation_Table_Entity>> = reservationTableDao.getAllReservationsTables()


    suspend fun syncReservationsTablesFromFirebase() {
        val snapshot = firestore.collection("Reservation_Table").get().await()
        val reservationsTables = snapshot.documents.mapNotNull { doc ->
            doc.toObject(Reservation_Table_Entity::class.java)
        }

        reservationTableDao.clearAllReservationsTables()
        reservationsTables.forEach { reservationsTables ->
            reservationTableDao.insertReservationTable(reservationsTables)
        }
    }

    suspend fun insertReservationTableCrossRef(reservationId: String, tableId: String,tableLocationMap:List<String>) {
        val reservationTable = Reservation_Table_Entity(
            reservationId = reservationId,
            tableId = tableId,
            tableLocationMap = tableLocationMap
        )
        reservationTableDao.insertReservationTable(reservationTable)
        firestore.collection("Reservation_Table")
            .document(reservationId + tableId)
            .set(reservationTable)
            .await()
    }



    suspend fun getLinksByReservationId(resId: String): List<Reservation_Table_Entity> {
        return reservationTableDao.getLinksByReservationId(resId)
    }

    suspend fun getTableIdsByReservationId(resId: String): List<String> {
        return reservationTableDao.getTableIdsByReservationId(resId)
    }







}