package com.example.nextstation.ui.main

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nextstation.domain.model.ArrivalInfo
import com.example.nextstation.domain.repository.ArrivalRepository
import com.example.nextstation.service.ArrivalService
import com.example.nextstation.util.AlarmReceiver
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: ArrivalRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val history = repository.getArrivalHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setAlarm(destination: String, minutes: Int, phoneNumber: String, message: String) {
        val arrivalTime = System.currentTimeMillis() + (minutes * 60 * 1000L)
        val info = ArrivalInfo(
            destinationName = destination,
            arrivalTime = arrivalTime,
            phoneNumber = phoneNumber,
            message = message
        )

        viewModelScope.launch {
            repository.insertArrivalInfo(info)
        }

        // 1. Start Foreground Service
        val serviceIntent = Intent(context, ArrivalService::class.java).apply {
            putExtra("destination", destination)
            putExtra("arrivalTime", arrivalTime)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }

        // 2. Schedule Alarm
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("destination", destination)
            putExtra("phoneNumber", phoneNumber)
            putExtra("message", message)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    arrivalTime,
                    pendingIntent
                )
            } else {
                // Request permission or use non-exact alarm
                val settingsIntent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                settingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(settingsIntent)
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                arrivalTime,
                pendingIntent
            )
        }
    }
}