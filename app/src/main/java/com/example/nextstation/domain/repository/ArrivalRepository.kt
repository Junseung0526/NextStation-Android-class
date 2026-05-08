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
}