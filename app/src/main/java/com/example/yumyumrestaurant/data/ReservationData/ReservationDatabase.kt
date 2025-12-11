//package com.example.yumyumrestaurant.data.ReservationData
//
//
//
//
//
//import android.content.Context
//import androidx.room.Database
//import androidx.room.Room
//import androidx.room.RoomDatabase
//import androidx.room.TypeConverters
//import com.example.yumyumrestaurant.data.Converters
//
//@Database(entities = [ReservationEntity::class], version = 2, exportSchema = false)
//@TypeConverters(Converters::class)
//abstract class ReservationDatabase : RoomDatabase() {
//
//    abstract fun reservationDao(): ReservationDao
//
//    companion object {
//        @Volatile
//        private var Instance: ReservationDatabase? = null
//
//        fun getReservationDatabase(context: Context): ReservationDatabase {
//            return Instance ?: synchronized(this) {
//                Room.databaseBuilder(
//                    context.applicationContext,
//                    ReservationDatabase::class.java,
//                    "Reservations"
//                )
//                    .fallbackToDestructiveMigration()
//                    .build()
//                    .also { Instance = it }
//            }
//        }
//    }
//}
