package com.krishiai.app.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

interface LocationService {
    suspend fun getCurrentLocation(): Location?
}

class LocationServiceImpl(
    private val context: Context
) : LocationService {

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    override suspend fun getCurrentLocation(): Location? {
        val hasAccessFineLocationPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val hasAccessCoarseLocationPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasAccessFineLocationPermission || !hasAccessCoarseLocationPermission) {
            return null
        }

        return suspendCancellableCoroutine { cont ->
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { location ->
                    cont.resume(location)
                }
                .addOnFailureListener { e ->
                    cont.resumeWithException(e)
                }
                .addOnCanceledListener {
                    cont.cancel()
                }
        }
    }
}
