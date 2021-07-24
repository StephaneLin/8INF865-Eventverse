package com.boulin.eventverse.data.database.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class StringListConverter {
    @TypeConverter
    fun stringListToString(value: List<String>?) = Gson().toJson(value)

    @TypeConverter
    fun stringToStringList(value: String?): List<String>? {
        val itemType = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson<List<String>>(value, itemType)
    }
}