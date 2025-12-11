package com.example.yumyumrestaurant.data




import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.yumyumrestaurant.data.TableData.TableEntity
import com.example.yumyumrestaurant.data.TableData.TableDao
import com.example.yumyumrestaurant.data.ReservationData.ReservationEntity
import com.example.yumyumrestaurant.data.ReservationData.ReservationDao

@Database(
    entities = [
        TableEntity::class,
        ReservationEntity::class,
        Reservation_Table_Entity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class Reservation_TableDatabase : RoomDatabase() {

    abstract fun tableDao(): TableDao
    abstract fun reservationDao(): ReservationDao

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
