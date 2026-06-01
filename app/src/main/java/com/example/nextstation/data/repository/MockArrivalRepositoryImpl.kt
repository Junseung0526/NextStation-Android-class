package com.example.nextstation.data.repository

import com.example.nextstation.data.local.ArrivalDao
import com.example.nextstation.data.local.ArrivalEntity
import com.example.nextstation.domain.model.ArrivalInfo
import com.example.nextstation.domain.model.RealTimeArrival
import com.example.nextstation.domain.model.RouteInfo
import com.example.nextstation.domain.model.StationInfo
import com.example.nextstation.domain.repository.ArrivalRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MockArrivalRepositoryImpl @Inject constructor(
    private val dao: ArrivalDao
) : ArrivalRepository {

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
        delay(500) // Simulate network delay
        listOf(
            RealTimeArrival(
                busNumber = "360",
                arrivalMessage = "5분 30초 후 [3정류장 전]",
                stationName = "강남역사거리",
                congestion = 4,
                busType = 1,
                isLast = false,
                isFull = false,
                vehicleNumber = "서울70사1234",
                travelTimeSeconds = 330,
                gpsX = 127.0276,
                gpsY = 37.4979
            ),
            RealTimeArrival(
                busNumber = "740",
                arrivalMessage = "12분 10초 후 [6정류장 전]",
                stationName = "강남역사거리",
                congestion = 5,
                busType = 0,
                isLast = false,
                isFull = false,
                vehicleNumber = "서울74아5678",
                travelTimeSeconds = 730,
                gpsX = 127.0276,
                gpsY = 37.4979
            )
        )
    }

    override suspend fun getEstimatedTravelTime(startX: Double, startY: Double, endX: Double, endY: Double): Int? = withContext(Dispatchers.IO) {
        delay(300)
        // Mock travel time calculation (returns 25 minutes in seconds)
        1500 
    }

    override suspend fun searchRoutesToDestination(destination: String): List<RouteInfo> = withContext(Dispatchers.IO) {
        delay(800)
        listOf(
            RouteInfo(
                busNumber = "360",
                busRouteId = "100100057",
                busType = "1",
                stId = "121000100",
                stNm = "$destination 정류소 (Mock)",
                arsId = "22010",
                firstArrivalMessage = "5분 30초 후 [3정류장 전]",
                firstArrivalTimeSeconds = 330,
                firstCongestion = 4,
                gpsX = 127.0276,
                gpsY = 37.4979
            ),
            RouteInfo(
                busNumber = "740",
                busRouteId = "100100112",
                busType = "0",
                stId = "121000100",
                stNm = "$destination 정류소 (Mock)",
                arsId = "22010",
                firstArrivalMessage = "12분 10초 후 [6정류장 전]",
                firstArrivalTimeSeconds = 730,
                firstCongestion = 5,
                gpsX = 127.0276,
                gpsY = 37.4979
            )
        )
    }

    override suspend fun searchStations(name: String): List<StationInfo> = withContext(Dispatchers.IO) {
        delay(400)
        listOf(
            StationInfo(
                stationId = "121000100",
                stationName = "$name 정류장 1 (Mock)",
                arsId = "22010",
                districtName = "강남구"
            ),
            StationInfo(
                stationId = "121000101",
                stationName = "$name 정류장 2 (Mock)",
                arsId = "22011",
                districtName = "서초구"
            )
        )
    }
}
