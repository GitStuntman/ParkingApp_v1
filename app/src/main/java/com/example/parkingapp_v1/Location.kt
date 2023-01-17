package com.example.parkingapp_v1


import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.InputStream

@Entity(tableName = "location_table")
data class Location(
    @ColumnInfo(name = "mapImage") var mapImage: String,
    @ColumnInfo(name = "coordinates") var coordinates: String,
    @ColumnInfo(name = "location") var location: String,
    @ColumnInfo(name = "date") var date: String?,
    @ColumnInfo(name = "time") var time: String?,
    @PrimaryKey(autoGenerate = true) val id :Int = 0)

