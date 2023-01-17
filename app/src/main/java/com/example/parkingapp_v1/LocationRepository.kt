package com.example.parkingapp_v1

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import kotlinx.coroutines.flow.Flow

class LocationRepository(private val locationItemDao: LocationItemDao) {

    val allLocation: Flow<List<Location>> = locationItemDao.allLocation()
    val lastLocation: Flow<Location> = locationItemDao.getLastLocation()
    lateinit var currentLocation: Flow<Location>

    @WorkerThread
    suspend fun insertLocationItem(locationItem: Location){
       locationItemDao.insertLocationItem(locationItem)
    }
    @WorkerThread
    suspend fun deleteLocationItem(location_id: Int){
        locationItemDao.deleteLocationItem(location_id)
    }
    @WorkerThread
    suspend fun deleteAllItem(){
        locationItemDao.deleteAllItem()
    }

    @WorkerThread
     fun getLocationFromID(location_id: Int) {
        currentLocation = locationItemDao.getLocationFromID(location_id)
    }

}