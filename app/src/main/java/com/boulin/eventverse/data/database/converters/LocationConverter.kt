package com.boulin.eventverse.data.database.converters

import androidx.room.TypeConverter
import com.boulin.eventverse.data.model.Location
import com.google.gson.Gson

class LocationConverter {
    @TypeConverter
    fun locationToString(value: Location?) = Gson().toJson(value)

    @TypeConverter
    fun stringToLocation(value: String?) = Gson().fromJson(value, Location::class.java)
}