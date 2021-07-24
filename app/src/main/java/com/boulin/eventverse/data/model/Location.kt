package com.boulin.eventverse.data.model

import java.io.Serializable
import android.location.Location as AndroidLocation

/**
 * Represents a location
 */
data class Location (
    val name: String,
    val longitude: Double,
    val latitude: Double,
): Serializable {
    fun getAndroidLocation(): AndroidLocation {
        val androidLocation = AndroidLocation("")

        androidLocation.latitude = latitude.toDouble()
        androidLocation.longitude = longitude.toDouble()

        return androidLocation
    }
}