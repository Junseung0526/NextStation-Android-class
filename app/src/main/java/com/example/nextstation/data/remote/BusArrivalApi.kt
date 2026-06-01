package com.example.nextstation.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface BusArrivalApi {
    // Station Info Service
    @GET("stationinfo/getStationByUid?resultType=json")
    suspend fun getBusArrival(
        @Query("serviceKey", encoded = true) serviceKey: String,
        @Query("arsId") arsId: String
    ): BusArrivalResponse

    @GET("stationinfo/getStationByName?resultType=json")
    suspend fun getStationByName(
        @Query("serviceKey", encoded = true) serviceKey: String,
        @Query("stSrch") stSrch: String
    ): StationSearchResponse

    // Arrival Info Service
    @GET("arrive/getArrInfoByRouteAll?resultType=json")
    suspend fun getArrInfoByRouteAll(
        @Query("serviceKey", encoded = true) serviceKey: String,
        @Query("busRouteId") busRouteId: String
    ): BusArrivalResponse

    @GET("arrive/getArrInfoByRoute?resultType=json")
    suspend fun getArrInfoByRoute(
        @Query("serviceKey", encoded = true) serviceKey: String,
        @Query("stId") stId: String,
        @Query("busRouteId") busRouteId: String,
        @Query("ord") ord: String
    ): BusArrivalResponse

    @GET("arrive/getLowArrInfoByStId?resultType=json")
    suspend fun getLowArrInfoByStId(
        @Query("serviceKey", encoded = true) serviceKey: String,
        @Query("stId") stId: String
    ): BusArrivalResponse

    @GET("arrive/getLowArrInfoByRoute?resultType=json")
    suspend fun getLowArrInfoByRoute(
        @Query("serviceKey", encoded = true) serviceKey: String,
        @Query("stId") stId: String,
        @Query("busRouteId") busRouteId: String,
        @Query("ord") ord: String
    ): BusArrivalResponse
}

data class StationSearchResponse(
    val msgHeader: MsgHeader,
    val msgBody: StationSearchBody
)

data class StationSearchBody(
    val itemList: List<StationSearchItem>?
)

data class StationSearchItem(
    val stId: String,
    val stNm: String,
    val arsId: String,
    val gpsX: String? = null,
    val gpsY: String? = null
)

data class BusArrivalResponse(
    val msgHeader: MsgHeader,
    val msgBody: MsgBody
)

data class MsgHeader(
    val headerMsg: String,
    val headerCd: String
)

data class MsgBody(
    val itemList: List<BusArrivalItem>?
)

data class BusArrivalItem(
    val rtNm: String,           // Bus number (Route Name)
    val arrmsg1: String,        // First arrival message
    val arrmsg2: String,        // Second arrival message
    val stNm: String,           // Station name
    val stId: String? = null,   // Station ID
    val arsId: String? = null,  // Station ID (ARS)
    val busRouteId: String? = null, // Route ID
    val plainNo1: String? = null,   // Vehicle number
    val traTime1: String? = null,   // Travel time in seconds
    val reride_Num1: String? = "0", // Congestion (0: No data, 3: Easy, 4: Normal, 5: Congested)
    val busType1: String? = "0",    // Bus type (0: Normal, 1: Low, 2: Articulated)
    val isLast1: String? = "0",     // Last bus flag
    val full1: String? = "0",       // Full bus flag
    val gpsX: String? = null,
    val gpsY: String? = null
)
