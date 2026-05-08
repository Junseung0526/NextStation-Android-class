package com.example.nextstation.domain.model

data class RealTimeArrival(
    val busNumber: String,
    val arrivalMessage: String,
    val stationName: String,
    val congestion: Int = 0,     // 0: N/A, 3: Easy, 4: Normal, 5: Congested
    val busType: Int = 0,        // 0: Normal, 1: Low, 2: Articulated
    val isLast: Boolean = false,
    val isFull: Boolean = false,
    val vehicleNumber: String? = null,
    val travelTimeSeconds: Int = 0
)
