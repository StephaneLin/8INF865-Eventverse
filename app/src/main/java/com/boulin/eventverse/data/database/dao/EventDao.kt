package com.boulin.eventverse.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.boulin.eventverse.data.model.Event
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdateEvent(user: Event)

    @Query("SELECT * from event")
    fun getAllEvents(): Flow<List<Event>>

    @Query("SELECT * from event where id = :id")
    fun getEventById(id: String): Flow<Event?>

    @Query("DELETE from event where id = :id")
    fun deleteEvent(id: String)

    @Query("DELETE from event")
    fun deleteEvents()
}