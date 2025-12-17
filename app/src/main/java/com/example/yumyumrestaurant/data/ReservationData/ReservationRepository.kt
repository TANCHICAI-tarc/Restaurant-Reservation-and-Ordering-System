package com.example.yumyumrestaurant.data.ReservationData

import com.example.yumyumrestaurant.data.TableData.TableDao
import com.example.yumyumrestaurant.data.TableData.TableEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import java.time.LocalDate

class ReservationRepository(private val reservationDao: ReservationDao) {

    private val firestore = FirebaseFirestore.getInstance()

    val allReservations: Flow<List<ReservationEntity>> = reservationDao.getAllReservations()

    // Insert locally and in Firestore
    suspend fun insertTable(reservation: ReservationEntity) {
        reservationDao.insertReservation(reservation)
        firestore.collection("Reservations").document(reservation.reservationId).set(reservation).await()
    }

    suspend fun generateReservationID(): String {
        val documentRef = firestore.collection("Counters").document("reservation_counter")

        return firestore.runTransaction { transaction ->
            val snapshot = transaction.get(documentRef)
            val lastRes = snapshot.getLong("lastReservation") ?: 0
            val newRes = lastRes + 1

            transaction.update(documentRef, "lastReservation", newRes)

            String.format("R%04d", newRes)
        }.await()
    }


    // Fetch all from Firestore and save to Room
    suspend fun syncTablesFromFirebase() {
        val snapshot = firestore.collection("Reservations").get().await()
        val reservations = snapshot.documents.mapNotNull {
            it.toObject(ReservationEntity::class.java)?.copy(reservationId = it.id)
        }
        reservationDao.clearAllReservations()
        reservations.forEach { reservation ->
            reservationDao.insertReservation(reservation)
        }
    }

    suspend fun getConfirmedReservationsForTableOnDate(tableId: String, date: LocalDate): List<ReservationEntity> {
        return reservationDao.getConfirmedReservationsForTableOnDate(tableId,date)
    }



//    suspend fun updateTable(table: ReservationEntity) {
//        reservationDao.updateTable(table)
//        firestore.collection("Reservations").document(table.reservationId).set(table).await()
//    }


}

