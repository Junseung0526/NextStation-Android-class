package com.example.nextstation.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.nextstation.service.ArrivalService

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val destination = intent.getStringExtra("destination") ?: "목적지"
        val phoneNumber = intent.getStringExtra("phoneNumber")
        val message = intent.getStringExtra("message") ?: "곧 도착 예정입니다."

        val serviceIntent = Intent(context, ArrivalService::class.java).apply {
            action = ArrivalService.ACTION_ARRIVED
            putExtra("destination", destination)
            putExtra("phoneNumber", phoneNumber)
            putExtra("message", message)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }
}