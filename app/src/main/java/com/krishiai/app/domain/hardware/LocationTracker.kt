package com.krishiai.app.domain.hardware

import android.location.Location

interface LocationTracker {
    suspend fun getCurrentLocation(): Location?
    suspend fun getLocationAddress(location: Location): AddressResult?
}

data class AddressResult(
    val district: String,
    val taluk: String,
    val fullAddress: String
)
