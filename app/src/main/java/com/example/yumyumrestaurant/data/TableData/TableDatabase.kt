package com.example.yumyumrestaurant.data.TableData//package com.example.yumyumrestaurant.data.TableData
//
//import android.content.Context
//import androidx.room.Database
//import androidx.room.Room
//import androidx.room.RoomDatabase
//import androidx.room.TypeConverters
//import com.example.yumyumrestaurant.data.Converters
//import com.example.yumyumrestaurant.data.TableData.TableDao
//import com.example.yumyumrestaurant.data.TableData.TableEntity
//
//@Database(entities = [TableEntity::class], version = 1, exportSchema = false)
//@TypeConverters(Converters::class)
//abstract class TableDatabase : RoomDatabase() {
//
//    abstract fun tableDao(): TableDao
//
//    companion object {
//        @Volatile
//        private var Instance: TableDatabase? = null
//
//        fun getTableDatabase(context: Context): TableDatabase {
//            return Instance ?: synchronized(this) {
//                Room.databaseBuilder(
//                    context.applicationContext,
//                    TableDatabase::class.java,
//                    "Tables"
//                )
//                    .fallbackToDestructiveMigration()
//                    .build()
//                    .also { Instance = it }
//            }
//        }
//    }
//}
