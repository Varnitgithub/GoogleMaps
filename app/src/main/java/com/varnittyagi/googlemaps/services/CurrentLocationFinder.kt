package com.varnittyagi.googlemaps.services

import android.app.Service
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class CurrentLocationFinder : Service() {

    private var firebaseAuth = FirebaseAuth.getInstance()
    private var databaseReference = FirebaseDatabase.getInstance().reference
    private var locationListener: LocationListener? = null

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                Log.d("TAGGGGGG", "onLocationChanged: ${location.latitude} is latitude")
                Log.d("TAGGGGGG", "onLocationChanged: ${location.longitude} is longitude")
                savedataonFirebase(location.latitude.toString(), location.longitude.toString())
            }

            override fun onProviderEnabled(provider: String) {}

            override fun onProviderDisabled(provider: String) {}

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        }

        // You should request location updates here. Replace the following line with your implementation.
        // locationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, locationListener)

        return START_STICKY
    }

    override fun onDestroy() {
        // Remove location updates when the service is no longer needed.
        // locationManager?.removeUpdates(locationListener)
        super.onDestroy()
    }

    private fun savedataonFirebase(latitude: String, longitude: String) {
        val map = HashMap<String, String>()
        map["latitude"] = latitude
        map["longitude"] = longitude
        firebaseAuth.currentUser?.uid?.let {
            databaseReference.child("UserCurrentLocation").child(it).setValue(map)
        }
    }
}
