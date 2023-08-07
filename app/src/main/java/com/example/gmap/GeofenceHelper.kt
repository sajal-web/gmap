package com.example.gmap

import android.app.PendingIntent
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest

class GeofenceHelper(base: Context?) : ContextWrapper(base) {

    lateinit var pendingIntent:PendingIntent

    fun getGeofencingRequest(geofence: Geofence): GeofencingRequest {
        return GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()
    }

    fun getGeofence(
        requestId: String,
        lat: Double,
        lng: Double,
        radius: Float,
        transitionTypes: Int
    ): Geofence {
        return Geofence.Builder()
            .setCircularRegion(lat, lng, radius)
            .setRequestId(requestId)
            .setTransitionTypes(transitionTypes)
            .setLoiteringDelay(3000) // 3 seconds
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .build()
    }


    fun getGeofencePendingIntent(): PendingIntent {
        val intent = Intent(this, GeofenceBroadcastReceiver::class.java)
        pendingIntent =  PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
        return pendingIntent
    }

    }
