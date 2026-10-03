package com.krishiai.app.core.auth

enum class UserRole {
    FARMER,
    BUYER,
    VENDOR,
    AGRI_EXPERT,
    ADMIN,
    UNKNOWN;

    companion object {
        fun fromString(role: String?): UserRole {
            return when (role?.uppercase()) {
                "FARMER" -> FARMER
                "BUYER" -> BUYER
                "VENDOR" -> VENDOR
                "AGRI_EXPERT", "EXPERT" -> AGRI_EXPERT
                "ADMIN" -> ADMIN
                else -> UNKNOWN
            }
        }
    }
}
