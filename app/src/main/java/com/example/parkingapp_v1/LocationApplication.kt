package com.example.parkingapp_v1

import android.app.Application

class LocationApplication :Application() {
    private val database by lazy { LocationDatabase.getDatabase(this) }
    val repository by lazy { LocationRepository(database.locationItemDao()) }
}