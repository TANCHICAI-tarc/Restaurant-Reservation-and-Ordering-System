package com.example.yumyumrestaurant.data.ReservationData

import android.util.Log
import com.example.yumyumrestaurant.data.CustomerEntity
import com.example.yumyumrestaurant.data.ReservationTableData.Reservation_Table_Entity
import com.example.yumyumrestaurant.data.TableData.TableEntity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await
import java.time.LocalDate

class ReservationRepository(private val reservationDao: ReservationDao?=null) {

    private val firestore = FirebaseFirestore.getInstance()

    val allReservations: Flow<List<ReservationEntity>> = reservationDao?.getAllReservations() ?: flowOf(emptyList())

    suspend fun insertTable(reservation: ReservationEntity) {
        reservationDao?.insertReservation(reservation)
        firestore.collection("Reservations").document(reservation.reservationId).set(reservation).await()
    }

    suspend fun getCustomerById(userId: String): CustomerEntity? {
        return try {

            val querySnapshot = firestore.collection("Users")
                .whereEqualTo("userId", userId)
                .get()
                .await()


            val doc = querySnapshot.documents.firstOrNull() ?: return null
            doc.toObject(CustomerEntity::class.java)?.copy(userId = doc.getString("userId") ?: "")
        } catch (e: Exception) {

            null
        }
    }

    suspend fun cancelReservation(reservation: ReservationEntity) {

        reservationDao?.updateReservationStatus(reservations = reservation)


    }
    suspend fun generateReservationID(): String {
        val documentRef = firestore.collection("Counters").document("reservation_counter")

        return firestore.runTransaction { transaction ->
            val snapshot = transaction.get(documentRef)
            val lastRes = snapshot.getLong("lastReservation") ?: 0
            val newRes = lastRes + 1


            transaction.set(documentRef, mapOf("lastReservation" to newRes), SetOptions.merge())

            String.format("R%04d", newRes)
        }.await()
    }



    // Fetch all from Firestore and save to Room
    suspend fun syncTablesFromFirebase() {
        val snapshot = firestore.collection("Reservations").get().await()
        val reservations = snapshot.documents.mapNotNull {
            it.toObject(ReservationEntity::class.java)?.copy(reservationId = it.id)
        }
        reservationDao?.clearAllReservations()
        reservations.forEach { reservation ->
            reservationDao?.insertReservation(reservation)
        }
    }

    suspend fun getConfirmedReservationsForTableOnDate(tableId: String, date: String): List<ReservationEntity>? {
        return reservationDao?.getConfirmedReservationsForTableOnDate(tableId,date)
    }


    suspend fun getAllReservationsFromFirebase(): List<ReservationEntity> {
        val snapshot = firestore.collection("Reservations").get().await()
        return snapshot.documents.mapNotNull {
            it.toObject(ReservationEntity::class.java)?.copy(reservationId = it.id)
        }
    }
    suspend fun getReservationById(resId: String): ReservationEntity? {
        // If using Room:
        return reservationDao?.getReservationById(resId)
    }
}

