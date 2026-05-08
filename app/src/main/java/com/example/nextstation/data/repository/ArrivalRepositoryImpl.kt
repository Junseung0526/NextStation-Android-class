package com.example.nextstation.data.repository

import com.example.nextstation.data.local.ArrivalDao
import com.example.nextstation.data.local.ArrivalEntity
import com.example.nextstation.data.remote.BusArrivalApi
import com.example.nextstation.domain.model.ArrivalInfo
import com.example.nextstation.domain.model.RealTimeArrival
import com.example.nextstation.domain.repository.ArrivalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ArrivalRepositoryImpl @Inject constructor(
    private val dao: ArrivalDao,
    private val api: BusArrivalApi
) : ArrivalRepository {

    private val SERVICE_KEY = "YOUR_API_KEY_HERE" // User needs to provide this

    override fun getArrivalHistory(): Flow<List<ArrivalInfo>> {
        return dao.getArrivalHistory().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertArrivalInfo(info: ArrivalInfo) {
        dao.insertArrivalInfo(ArrivalEntity.fromDomain(info))
    }

    override suspend fun deleteArrivalInfo(info: ArrivalInfo) {
        dao.deleteArrivalInfo(ArrivalEntity.fromDomain(info))
    }

    override suspend fun getArrivalInfoById(id: Int): ArrivalInfo? {
        return dao.getArrivalInfoById(id)?.toDomain()
    }

    override suspend fun getRealTimeArrival(arsId: String): List<RealTimeArrival> {
        return try {
            val response = api.getBusArrival(SERVICE_KEY, arsId)
            response.msgBody.itemList?.map {
                RealTimeArrival(
                    busNumber = it.rtNm,
                    arrivalMessage = it.arrmsg1,
                    stationName = it.stNm
                )
            } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}