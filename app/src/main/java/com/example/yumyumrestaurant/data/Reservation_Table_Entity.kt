package com.example.yumyumrestaurant.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Junction
import androidx.room.Relation
import com.example.yumyumrestaurant.data.ReservationData.ReservationEntity
import com.example.yumyumrestaurant.data.TableData.TableEntity


@Entity(
    tableName = "Reservation_Table",
    primaryKeys = ["reservationId", "tableId"],
    indices = [Index(value = ["tableId"])]
)
data class Reservation_Table_Entity(
    val reservationId: String,
    val tableId: String
)


data class ReservationWithTables(
    @Embedded val reservation: ReservationEntity,
    @Relation(
        parentColumn = "reservationId",
        entityColumn = "tableId",
        associateBy = Junction(Reservation_Table_Entity::class)
    )
    val tables: List<TableEntity>
)


data class TableWithReservations(
    @Embedded val table: TableEntity,
    @Relation(
        parentColumn = "tableId",
        entityColumn = "reservationId",
        associateBy = Junction(Reservation_Table_Entity::class)
    )
    val reservations: List<ReservationEntity>
)