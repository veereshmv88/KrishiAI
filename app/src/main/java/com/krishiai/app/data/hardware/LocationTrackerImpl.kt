package com.krishiai.app.data.hardware

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.krishiai.app.domain.hardware.AddressResult
import com.krishiai.app.domain.hardware.LocationTracker
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import javax.inject.Inject
import kotlin.coroutines.resume

class LocationTrackerImpl @Inject constructor(
    private val context: Context
) : LocationTracker {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    override suspend fun getCurrentLocation(): Location? {
        return suspendCancellableCoroutine { cont ->
            fusedLocationClient.lastLocation.addOnCompleteListener { task ->
                if (task.isSuccessful && task.result != null) {
                    cont.resume(task.result)
                } else {
                    cont.resume(null)
                }
            }
        }
    }

    override suspend fun getLocationAddress(location: Location): AddressResult? {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                val district = address.subAdminArea ?: address.locality ?: ""
                val taluk = address.locality ?: address.subLocality ?: ""
                val fullAddress = address.getAddressLine(0) ?: ""
                
                AddressResult(
                    district = district,
                    taluk = taluk,
                    fullAddress = fullAddress
                )
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
