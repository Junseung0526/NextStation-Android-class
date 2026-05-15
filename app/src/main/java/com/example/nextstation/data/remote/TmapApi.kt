package com.example.nextstation.data.remote

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface TmapApi {
    @GET("tmap/routes?version=1&format=json")
    suspend fun getRouteTime(
        @Header("appKey") appKey: String,
        @Query("startX") startX: Double,
        @Query("startY") startY: Double,
        @Query("endX") endX: Double,
        @Query("endY") endY: Double,
        @Query("totalValue") totalValue: Int = 2 // Returns travel time and distance
    ): TmapRouteResponse
}

data class TmapRouteResponse(
    val features: List<Feature>?
)

data class Feature(
    val properties: Properties?
)

data class Properties(
    val totalTime: Int?, // in seconds
    val totalDistance: Int? // in meters
)
