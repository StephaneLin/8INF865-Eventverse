package com.boulin.eventverse.data.database.converters

import androidx.room.TypeConverter
import com.boulin.eventverse.data.model.User
import com.google.gson.Gson

class UserPreferencesConverter {
    @TypeConverter
    fun preferencesToString(value: User.UserPreferences?) = Gson().toJson(value)

    @TypeConverter
    fun stringToPreferences(value: String?) = Gson().fromJson(value, User.UserPreferences::class.java)
}