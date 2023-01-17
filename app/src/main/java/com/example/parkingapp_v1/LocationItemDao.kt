package com.example.parkingapp_v1

import androidx.lifecycle.LiveData
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationItemDao {
    @Query("SELECT * FROM location_table ORDER BY id ASC")
    fun allLocation(): Flow<List<Location>>

    @Query("SELECT * FROM location_table ORDER BY id DESC LIMIT 1")
    fun getLastLocation(): Flow<Location>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocationItem(location: Location)

    @Query("SELECT * FROM location_table WHERE id = :location_id")
    fun getLocationFromID(location_id: Int) :Flow<Location>

    @Query("DELETE FROM location_table WHERE id = :location_id")
    suspend fun deleteLocationItem(location_id: Int)

    @Query("DELETE FROM location_table")
    suspend fun deleteAllItem()
}