package com.example.azalea.data

data class User(
    val name: String = "",
    val email: String = "",
    val birthDate: String = "1/1/2000",
    val bloodType: String = "",
    val weight: Double = 0.0,
    val height: Double = 0.0,
    val description: String = "",
    val available: Boolean = true,
    val emergency: Boolean = false,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val emergencyContacts: List<String> = emptyList(),
    val emergencyContactFor: List<String> = emptyList(),
    val emergencyCode: Int = -1,
)
