package com.example.stayeasehotel.data.regiondata

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.yumyumrestaurant.data.Converters

import com.example.yumyumrestaurant.data.RegionData.RegionDao
import com.example.yumyumrestaurant.data.RegionData.RegionEntity

@Database(entities = [RegionEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class RegionDatabase : RoomDatabase() {

    abstract fun regionDao(): RegionDao

    companion object {
        @Volatile
        private var Instance: RegionDatabase? = null

        fun getRegionDatabase(context: Context): RegionDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    RegionDatabase::class.java,
                    "Regions"   // database name changed
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { Instance = it }
            }
        }
    }
}