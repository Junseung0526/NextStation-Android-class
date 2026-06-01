package com.example.nextstation

import android.app.Application
import android.util.Log
import com.kakao.vectormap.KakaoMapSdk
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class NextStationApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Kakao Map SDK with App Key from BuildConfig (synced from .env)
        KakaoMapSdk.init(this, BuildConfig.KAKAO_APP_KEY)
    }
}
