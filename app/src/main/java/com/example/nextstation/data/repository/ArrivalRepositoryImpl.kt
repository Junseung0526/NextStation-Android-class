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
    private val api: BusArrivalApi
) : ArrivalRepository {

    private val SERVICE_KEY = BuildConfig.BUS_SERVICE_KEY

    override fun getArrivalHistory(): Flow<List<ArrivalInfo>> {
        return dao.getArrivalHistory()
            .map { entities ->
                entities.map { it.toDomain() }
            }
            .flowOn(Dispatchers.IO) // Ensure mapping happens on IO thread
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
        // Mock data for testing since API key might not be active yet
        listOf(
            RealTimeArrival(
                busNumber = "100",
                arrivalMessage = "5분 30초 후 [3번째 전]",
                stationName = "서울역",
                congestion = 3,
                busType = 1,
                travelTimeSeconds = 330
            ),
            RealTimeArrival(
                busNumber = "701",
                arrivalMessage = "12분 10초 후 [5번째 전]",
                stationName = "숭례문",
                congestion = 4,
                busType = 0,
                travelTimeSeconds = 730
            ),
            RealTimeArrival(
                busNumber = "N15",
                arrivalMessage = "곧 도착",
                stationName = "남대문시장",
                congestion = 5,
                busType = 0,
                isLast = true,
                travelTimeSeconds = 60
            )
        )
        /* Original API Logic (Commented out for later)
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
        */
    }

    override suspend fun searchRoutesToDestination(destination: String): List<com.example.nextstation.domain.model.RouteInfo> = withContext(Dispatchers.IO) {
        // Mock data for destination-based search
        listOf(
            com.example.nextstation.domain.model.RouteInfo(
                busNumber = "140",
                busRouteId = "100100016",
                busType = "0",
                stId = "100000001",
                stNm = "서울역",
                arsId = "01001",
                firstArrivalMessage = "5분 20초 후",
                firstArrivalTimeSeconds = 320,
                firstCongestion = 3
            ),
            com.example.nextstation.domain.model.RouteInfo(
                busNumber = "470",
                busRouteId = "100100073",
                busType = "1",
                stId = "102000001",
                stNm = "한남동",
                arsId = "03001",
                firstArrivalMessage = "8분 45초 후",
                firstArrivalTimeSeconds = 525,
                firstCongestion = 4
            ),
            com.example.nextstation.domain.model.RouteInfo(
                busNumber = "741",
                busRouteId = "100100411",
                busType = "0",
                stId = "122000001",
                stNm = "강남역",
                arsId = "23001",
                firstArrivalMessage = "12분 10초 후",
                firstArrivalTimeSeconds = 730,
                firstCongestion = 5
            )
        )
    }

    override suspend fun searchStations(name: String): List<com.example.nextstation.domain.model.StationInfo> = withContext(Dispatchers.IO) {
        // Mock data for testing
        listOf(
            com.example.nextstation.domain.model.StationInfo(
                stationId = "100000001",
                stationName = "서울역",
                arsId = "01001",
                districtName = "중구"
            ),
            com.example.nextstation.domain.model.StationInfo(
                stationId = "100000002",
                stationName = "강남역",
                arsId = "23001",
                districtName = "강남구"
            ),
            com.example.nextstation.domain.model.StationInfo(
                stationId = "100000003",
                stationName = "홍대입구역",
                arsId = "14001",
                districtName = "마포구"
            )
        )
        /* Original API Logic (Commented out for later)
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
        */
    }
    }