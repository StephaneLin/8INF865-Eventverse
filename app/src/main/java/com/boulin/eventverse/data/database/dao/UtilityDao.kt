package com.boulin.eventverse.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.boulin.eventverse.data.model.User
import com.boulin.eventverse.data.model.Utility
import kotlinx.coroutines.flow.Flow

@Dao
interface UtilityDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdateUtility(utility: Utility)

    @Query("SELECT * from utility where id = :id")
    fun getUtility(id: String): Utility?
}