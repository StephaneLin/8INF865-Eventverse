package com.boulin.eventverse.data.database

import android.content.Context
import androidx.room.*
import com.boulin.eventverse.data.database.converters.DateConverter
import com.boulin.eventverse.data.database.converters.LocationConverter
import com.boulin.eventverse.data.database.converters.StringListConverter
import com.boulin.eventverse.data.database.converters.UserPreferencesConverter
import com.boulin.eventverse.data.database.dao.EventDao
import com.boulin.eventverse.data.database.dao.UserDao
import com.boulin.eventverse.data.database.dao.UtilityDao
import com.boulin.eventverse.data.model.Event
import com.boulin.eventverse.data.model.User
import com.boulin.eventverse.data.model.Utility

@Database(entities = [Event::class, User::class, Utility:: class], version = 2, exportSchema = false)
@TypeConverters(StringListConverter::class, DateConverter::class, LocationConverter::class, UserPreferencesConverter::class)
abstract class EventverseDatabase : RoomDatabase() {

    abstract fun eventDao(): EventDao
    abstract fun userDao(): UserDao
    abstract fun utilityDao(): UtilityDao

    companion object {
        @Volatile
        private var instance: EventverseDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(LOCK) {
            instance ?: buildDatabase(context).also { instance = it }
        }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                EventverseDatabase::class.java,
                "eventverse.db"
            )
            .fallbackToDestructiveMigration()
            .build()
    }
}