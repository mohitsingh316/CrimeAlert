package com.example.crimealert.activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.telephony.SmsManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.FirebaseFirestore

class SOSActivity(private val context: Context) {

    private var fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    private val firestore = FirebaseFirestore.getInstance()
    private val emergencyCallNumber = "02668262233" // The number to call during SOS

    fun triggerSOS(contactNumbers: List<String>) {
        // Check location permissions
        if (ActivityCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("SOSActivity", "Location permission not granted")
            return
        }

        // Fetch the current location
        fusedLocationClient.getCurrentLocation(
            LocationRequest.PRIORITY_HIGH_ACCURACY, null
        ).addOnSuccessListener { location ->
            if (location != null) {
                val latitude = location.latitude
                val longitude = location.longitude
                val locationUrl = "https://maps.google.com/?q=$latitude,$longitude"
                Log.d("SOSActivity", "Current location is: $locationUrl")
                sendSmsToContacts(contactNumbers, locationUrl)
                makeEmergencyCall()
            } else {
                Log.d("SOSActivity", "Location not available")
            }
        }.addOnFailureListener { exception ->
            Log.e("SOSActivity", "Error fetching location: ${exception.message}")
        }
    }

    private fun sendSmsToContacts(contactNumbers: List<String>, locationUrl: String) {
        // Check SMS permission
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("SOSActivity", "SMS permission not granted")
            return
        }

        val smsManager = SmsManager.getDefault()

        // Send SMS to each contact
        for (number in contactNumbers) {
            if (number.isNotEmpty()) {
                try {
                    smsManager.sendTextMessage(number, null, "Emergency! My location: $locationUrl", null, null)
                    Log.d("SOSActivity", "SMS sent to $number with location: $locationUrl")
                } catch (e: Exception) {
                    Log.e("SOSActivity", "SMS failed to send to $number: ${e.message}")
                }
            } else {
                Log.e("SOSActivity", "Invalid phone number: $number")
            }
        }
    }

    private fun makeEmergencyCall() {
        // Check call permission
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("SOSActivity", "Call permission not granted")
            return
        }

        // Initiate a call to the emergency number
        val callIntent = Intent(Intent.ACTION_CALL)
        callIntent.data = Uri.parse("tel:$emergencyCallNumber")
        try {
            context.startActivity(callIntent)
            Log.d("SOSActivity", "Calling emergency number: $emergencyCallNumber")
        } catch (e: Exception) {
            Log.e("SOSActivity", "Failed to make call: ${e.message}")
        }
    }
}
