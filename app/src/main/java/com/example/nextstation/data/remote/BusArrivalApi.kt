package com.example.nextstation.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface BusArrivalApi {
    @GET("getStationByUid")
    suspend fun getBusArrival(
        @Query("serviceKey") serviceKey: String,
        @Query("arsId") arsId: String
    ): BusArrivalResponse
}

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
    val rtNm: String,         // Bus number
    val arrmsg1: String,      // First arrival message (e.g., "5분 30초 후[2번째 전]")
    val arrmsg2: String,      // Second arrival message
    val stNm: String          // Station name
)
