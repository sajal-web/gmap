package com.example.gmap

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent

class GeofenceBroadcastReceiver : BroadcastReceiver() {
    companion object {
        private const val GEOFENCE_NOTIFICATION_ID = 123
        private const val TAG = "GeofenceReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
        Toast.makeText(context,"geofence triggered",Toast.LENGTH_SHORT).show()
        Log.d(TAG, "Received geofence transition event")

        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent != null && !geofencingEvent.hasError()) {
            val geofenceTransition = geofencingEvent.geofenceTransition

            // Check if there are triggering geofences
            if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL
            ) {
                val triggeringGeofences = geofencingEvent.triggeringGeofences

                if (triggeringGeofences != null) {
                    for (geofence in triggeringGeofences) {
                        val geofenceRequestId = geofence.requestId
                        when (geofenceTransition) {
                            Geofence.GEOFENCE_TRANSITION_ENTER -> {
                                sendNotification(context, "Entered Geofence: $geofenceRequestId")
                                Log.d(TAG, "Entered geofence: $geofenceRequestId")
                            }
                            Geofence.GEOFENCE_TRANSITION_EXIT -> {
                                sendNotification(context, "Exited Geofence: $geofenceRequestId")
                                Log.d(TAG, "Exited geofence: $geofenceRequestId")
                            }
                            Geofence.GEOFENCE_TRANSITION_DWELL -> {
                                sendNotification(context, "Dwelling in Geofence: $geofenceRequestId")
                                Log.d(TAG, "Dwelling in geofence: $geofenceRequestId")
                            }
                            else -> {
                                Log.d(TAG, "Unknown geofence transition")
                            }
                        }
                    }
                } else {
                    Log.d(TAG, "No triggering geofences")
                }
            }
        } else {
            Log.d(TAG, "Geofencing event error")
        }
    }

    private fun sendNotification(context: Context, message: String) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            val notificationBuilder = NotificationCompat.Builder(context, "GeofenceChannel")
                .setContentTitle("Geofence Notifications")
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)

            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(GEOFENCE_NOTIFICATION_ID, notificationBuilder.build())
        }else{
            Log.d(TAG,"Permission not granted to send notification")
        }

    }

}