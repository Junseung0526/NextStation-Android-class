package com.example.nextstation.domain.model

data class ArrivalInfo(
    val id: Int = 0,
    val destinationName: String,
    val arrivalTime: Long, // Epoch timestamp
    val phoneNumber: String,
    val message: String,
    val isCompleted: Boolean = false
)