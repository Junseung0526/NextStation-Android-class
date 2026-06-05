package com.example.nextstation

import android.app.Application
import android.util.Log
import com.example.nextstation.util.EmulatorDetector
import com.kakao.vectormap.KakaoMapSdk
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class NextStationApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Kakao Map SDK only if NOT running on emulator
        if (!EmulatorDetector.isEmulator()) {
            KakaoMapSdk.init(this, BuildConfig.KAKAO_APP_KEY)
        } else {
            Log.d("NextStationApp", "Running on emulator: skipping Kakao Map SDK initialization to avoid libK3fAndroid.so load.")
        }
    }
}
