package com.krishiai.app.data.model

data class User(
    val uid: String = "",
    val fullName: String = "",
    val mobileNumber: String = "",
    val email: String = "",
    val role: String = "", // "Farmer" or "Buyer"
    val district: String = "",
    val taluk: String = "", // Used primarily for Farmers
    val city: String = "",  // Used primarily for Buyers
    val businessName: String = "", // Optional business/shop/farm name
    val profilePhotoUrl: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
