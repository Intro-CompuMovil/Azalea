package com.example.azalea.models


data class Notification(
    val senderId: String = "",
    var title: String = "",
    var content: String = "",
    val timestamp: Long = 0
)