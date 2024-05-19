package com.example.azalea.data

data class User(
    var name: String = "",
    var email: String = "",
    var birthDate: String = "1/1/2000",
    var bloodType: String = "",
    var weight: Double = 0.0,
    var height: Double = 0.0,
    var description: String = "",
    var available: Boolean = true,
    var emergency: Boolean = false,
    var location: String = "0.0,0.0",
    var emergencyContacts: List<String> = emptyList(),
    var emergencyContactFor: List<String> = emptyList(),
    var emergencyCode: Int = -1,
    var emergencyMessage: String = "Ayuda, estoy en peligro",
    var cancelMessage: String = "Ya estoy a salvo",
)
