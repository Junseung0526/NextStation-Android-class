package com.example.nextstation.domain.model

data class StationInfo(
    val stationId: String,
    val stationName: String,
    val arsId: String,
    val districtName: String
)

fun getDistrictFromArs(arsId: String): String {
    if (arsId.length < 2) return "서울"
    val prefix = arsId.take(2)
    return seoulDistricts.find { it.arsPrefix == prefix }?.name ?: "서울"
}
