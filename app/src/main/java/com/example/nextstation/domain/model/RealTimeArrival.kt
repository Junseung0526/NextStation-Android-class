package com.example.nextstation.domain.model

data class RealTimeArrival(
    val busNumber: String,
    val arrivalMessage: String,
    val stationName: String
)
