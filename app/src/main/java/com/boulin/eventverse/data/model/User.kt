package com.boulin.eventverse.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.util.*


/**
 * Represents a user in the system
 */
@Entity(tableName = "user")
class User (
    @PrimaryKey(autoGenerate = false) val uid: String,
    val isOrganizer: Boolean,
    val signinDate : Date,
    val urlPicture: String,
    val name: String,
    val surname: String,
    val createdEvents: List<String>, // list of ids of events created
    val likedEvents: List<String>, // list of ids of liked events
    val preferences: UserPreferences
    ): Serializable {

    /**
     * Represents user preferences
     */
    data class UserPreferences(
        val radius: Float,
        val weeks: Int,
        val target: TargetAudience,
        val location: Location // location used for "near me events"
    ): Serializable

    /**
     * Target audience that a user can be part of
     */
    enum class TargetAudience {
        @SerializedName("0")
        ALL, // for everyone
        @SerializedName("1")
        CHILDREN, // for children
        @SerializedName("2")
        TEENAGER // for teenager
    }

    /**
     * Represents input parameters used to create a user (the rest are based on the google user)
     */
    data class UserInputParams(
        val isOrganizer: Boolean,
        val organizerCode: String?
    )

    /**
     * Represents user mutable parameters (those which can be updated in the profile)
     */
    data class UserMutableParams(
        val name: String?,
        val surname: String?,
        val preferences: UserPreferences?
    )
}