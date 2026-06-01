package com.example.nextstation.domain.repository

import com.example.nextstation.domain.model.ArrivalInfo
import com.example.nextstation.domain.model.RealTimeArrival
import kotlinx.coroutines.flow.Flow

interface ArrivalRepository {
    fun getArrivalHistory(): Flow<List<ArrivalInfo>>
    suspend fun insertArrivalInfo(info: ArrivalInfo)
    suspend fun deleteArrivalInfo(info: ArrivalInfo)
    suspend fun getArrivalInfoById(id: Int): ArrivalInfo?
    suspend fun getRealTimeArrival(arsId: String): List<RealTimeArrival>
    suspend fun getEstimatedTravelTime(startX: Double, startY: Double, endX: Double, endY: Double): Int?
    suspend fun searchStations(name: String): List<com.example.nextstation.domain.model.StationInfo>
    suspend fun searchRoutesToDestination(destination: String): List<com.example.nextstation.domain.model.RouteInfo>
    }