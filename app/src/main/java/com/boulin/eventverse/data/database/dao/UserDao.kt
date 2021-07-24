package com.boulin.eventverse.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.boulin.eventverse.data.model.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdateUser(user: User)

    @Query("SELECT * from user where uid = :uid")
    fun getUser(uid: String): Flow<User?>

    @Query("DELETE from user where uid = :uid")
    fun deleteUser(uid: String)
}