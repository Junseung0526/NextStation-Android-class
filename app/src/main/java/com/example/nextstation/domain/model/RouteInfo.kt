package com.example.nextstation.domain.model

data class RouteInfo(
    val busNumber: String,
    val busRouteId: String,
    val busType: String, // 0: 일반, 1: 저상, 2: 굴절
    val stId: String,
    val stNm: String,
    val arsId: String,
    val firstArrivalMessage: String = "",
    val firstArrivalTimeSeconds: Int = 0,
    val firstCongestion: Int = 0,
    val gpsX: Double? = null,
    val gpsY: Double? = null
)

data class DestinationPath(
    val destinationName: String,
    val recommendedRoutes: List<RouteInfo>
)
