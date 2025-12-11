package com.example.yumyumrestaurant.data.RegionData

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.yumyumrestaurant.data.TableData.TableEntity


@Dao
interface RegionDao {

    // Get all tables
    @Query("SELECT * FROM Regions")
    fun getAllRegions():kotlinx.coroutines.flow.Flow<List<RegionEntity>>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRegion(table: RegionEntity)


    // Clear all tables
    @Query("DELETE FROM Regions")
    suspend fun clearAllRegion()
}
