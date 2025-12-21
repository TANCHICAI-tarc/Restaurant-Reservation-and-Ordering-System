package com.example.yumyumrestaurant.data.ReservationTableData

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.yumyumrestaurant.data.Converters
import com.example.yumyumrestaurant.data.ReservationData.ReservationDao
import com.example.yumyumrestaurant.data.ReservationData.ReservationEntity
import com.example.yumyumrestaurant.data.TableData.TableDao
import com.example.yumyumrestaurant.data.TableData.TableEntity

@Database(
    entities = [
        TableEntity::class,
        ReservationEntity::class,
        Reservation_Table_Entity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class Reservation_TableDatabase : RoomDatabase() {

    abstract fun tableDao(): TableDao
    abstract fun reservationDao(): ReservationDao
    abstract fun reservationTableDao(): ReservationTableDao

    companion object {
        @Volatile
        private var INSTANCE: Reservation_TableDatabase? = null

        fun getReservationTableDatabase(context: Context): Reservation_TableDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    Reservation_TableDatabase::class.java,
                    "Reservation_Table"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
    }
}