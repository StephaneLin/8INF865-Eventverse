package com.boulin.eventverse.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Represents utility data useful for miscellaneous things
 */
@Entity(tableName = "utility")
class Utility (
    @PrimaryKey(autoGenerate = false) val id: String,
    val lastEventFetch: Date
) {

    companion object {
        const val UNIQUE_ID = "Utility.UNIQUE_ID"
        const val MAX_FETCH_INTERVAL = 600000 // max interval is 10 minutes
    }
}