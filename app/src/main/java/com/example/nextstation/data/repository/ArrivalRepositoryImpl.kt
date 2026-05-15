package com.example.nextstation.data.repository

import com.example.nextstation.BuildConfig
import com.example.nextstation.data.local.ArrivalDao
import com.example.nextstation.data.local.ArrivalEntity
import com.example.nextstation.data.remote.BusArrivalApi
import com.example.nextstation.domain.model.ArrivalInfo
import com.example.nextstation.domain.model.RealTimeArrival
import com.example.nextstation.domain.repository.ArrivalRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ArrivalRepositoryImpl @Inject constructor(
    private val dao: ArrivalDao,
    private val api: BusArrivalApi,
    private val tmapApi: com.example.nextstation.data.remote.TmapApi
) : ArrivalRepository {

    private val SERVICE_KEY = BuildConfig.BUS_SERVICE_KEY
    private val TMAP_KEY = BuildConfig.TMAP_API_KEY

    override fun getArrivalHistory(): Flow<List<ArrivalInfo>> {
        return dao.getArrivalHistory()
            .map { entities ->
                entities.map { it.toDomain() }
            }
            .flowOn(Dispatchers.IO)
    }

    override suspend fun insertArrivalInfo(info: ArrivalInfo) = withContext(Dispatchers.IO) {
        dao.insertArrivalInfo(ArrivalEntity.fromDomain(info))
    }

    override suspend fun deleteArrivalInfo(info: ArrivalInfo) = withContext(Dispatchers.IO) {
        dao.deleteArrivalInfo(ArrivalEntity.fromDomain(info))
    }

    override suspend fun getArrivalInfoById(id: Int): ArrivalInfo? = withContext(Dispatchers.IO) {
        dao.getArrivalInfoById(id)?.toDomain()
    }

    override suspend fun getRealTimeArrival(arsId: String): List<RealTimeArrival> = withContext(Dispatchers.IO) {
        try {
            val response = api.getBusArrival(SERVICE_KEY, arsId)
            response.msgBody.itemList?.map {
                RealTimeArrival(
                    busNumber = it.rtNm,
                    arrivalMessage = it.arrmsg1,
                    stationName = it.stNm,
                    congestion = it.reride_Num1?.toIntOrNull() ?: 0,
                    busType = it.busType1?.toIntOrNull() ?: 0,
                    isLast = it.isLast1 == "1",
                    isFull = it.full1 == "1",
                    vehicleNumber = it.plainNo1,
                    travelTimeSeconds = it.traTime1?.toIntOrNull() ?: 0
                )
            } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getEstimatedTravelTime(startX: Double, startY: Double, endX: Double, endY: Double): Int? = withContext(Dispatchers.IO) {
        try {
            val response = tmapApi.getRouteTime(TMAP_KEY, startX, startY, endX, endY)
            response.features?.firstOrNull()?.properties?.totalTime
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun searchRoutesToDestination(destination: String): List<com.example.nextstation.domain.model.RouteInfo> = withContext(Dispatchers.IO) {
        try {
            // 1. Search for stations matching the destination name
            val stationResponse = api.getStationByName(SERVICE_KEY, destination)
            val stations = stationResponse.msgBody.itemList ?: return@withContext emptyList()

            val allRoutes = mutableListOf<com.example.nextstation.domain.model.RouteInfo>()

            // 2. For each station, get the arrival info (which contains route details)
            // Limit to top 5 stations to avoid too many API calls at once
            for (station in stations.take(5)) {
                if (station.arsId == null || station.arsId == "0") continue
                
                try {
                    val arrivalResponse = api.getBusArrival(SERVICE_KEY, station.arsId)
                    arrivalResponse.msgBody.itemList?.forEach { item ->
                        allRoutes.add(
                            com.example.nextstation.domain.model.RouteInfo(
                                busNumber = item.rtNm,
                                busRouteId = item.busRouteId ?: "",
                                busType = item.busType1 ?: "0",
                                stId = item.stId ?: "",
                                stNm = item.stNm,
                                arsId = item.arsId ?: "",
                                firstArrivalMessage = item.arrmsg1,
                                firstArrivalTimeSeconds = item.traTime1?.toIntOrNull() ?: 0,
                                firstCongestion = item.reride_Num1?.toIntOrNull() ?: 0
                            )
                        )
                    }
                } catch (e: Exception) {
                    // Skip failed station requests
                }
            }
            
            // Deduplicate by bus number and station name
            allRoutes.distinctBy { it.busNumber + it.stNm }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun searchStations(name: String): List<com.example.nextstation.domain.model.StationInfo> = withContext(Dispatchers.IO) {
        try {
            val response = api.getStationByName(SERVICE_KEY, name)
            response.msgBody.itemList?.map {
                com.example.nextstation.domain.model.StationInfo(
                    stationId = it.stId,
                    stationName = it.stNm,
                    arsId = it.arsId,
                    districtName = com.example.nextstation.domain.model.getDistrictFromArs(it.arsId)
                )
            } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
