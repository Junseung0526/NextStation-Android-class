package com.example.nextstation.domain.model

data class District(
    val name: String,
    val arsPrefix: String,
    val idPrefix: String
)

val seoulDistricts = listOf(
    District("종로구", "01", "100"),
    District("중구", "02", "101"),
    District("용산구", "03", "102"),
    District("성동구", "04", "103"),
    District("광진구", "05", "104"),
    District("동대문구", "06", "105"),
    District("중랑구", "07", "106"),
    District("성북구", "08", "107"),
    District("강북구", "09", "108"),
    District("도봉구", "10", "109"),
    District("노원구", "11", "110"),
    District("은평구", "12", "111"),
    District("서대문구", "13", "112"),
    District("마포구", "14", "113"),
    District("양천구", "15", "114"),
    District("강서구", "16", "115"),
    District("구로구", "17", "116"),
    District("금천구", "18", "117"),
    District("영등포구", "19", "118"),
    District("동작구", "20", "119"),
    District("관악구", "21", "120"),
    District("서초구", "22", "121"),
    District("강남구", "23", "122"),
    District("송파구", "24", "123"),
    District("강동구", "25", "124")
)
