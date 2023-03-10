package com.example.parkingapp_v1

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Location::class], version = 4, exportSchema = false)
abstract class LocationDatabase: RoomDatabase() {

    abstract fun locationItemDao(): LocationItemDao

    companion object{
        @Volatile
        private var INSTANCE: LocationDatabase? = null

        fun getDatabase(context: Context):LocationDatabase{
            return INSTANCE ?: synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LocationDatabase::class.java,
                    "location_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}