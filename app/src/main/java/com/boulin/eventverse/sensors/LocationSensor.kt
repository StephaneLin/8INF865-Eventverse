package com.boulin.eventverse.sensors

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContentProviderCompat.requireContext
import com.boulin.eventverse.data.model.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class LocationSensor(val context: Context) : VirtualSensor<Location>() {

    private var fusedLocationProviderClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    override fun register(context: Context) {
    }

    override fun unregister(context: Context) {
    }

    fun update() {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val data = Location(
                    name = "votre position",
                    longitude = location.longitude,
                    latitude = location.latitude
                )
                fireEvent(data)
            }else {
                Toast.makeText(context, "Location null, CHECK ReadMe", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
