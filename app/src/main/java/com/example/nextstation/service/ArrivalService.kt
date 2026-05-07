package com.example.nextstation.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.nextstation.MainActivity
import com.example.nextstation.R

class ArrivalService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val destination = intent?.getStringExtra("destination") ?: "목적지"
        val arrivalTime = intent?.getLongExtra("arrivalTime", 0L) ?: 0L

        createNotificationChannel()
        val notification = createNotification(destination)
        startForeground(NOTIFICATION_ID, notification)

        // The service can just stay alive. The AlarmManager will handle the actual trigger.
        // If we wanted to update the notification every minute, we could start a coroutine here.

        return START_STICKY
    }

    private fun createNotification(destination: String): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("안심 하차 작동 중")
            .setContentText("$destination 도착 알림이 예약되었습니다.")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Arrival Service Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "arrival_service_channel"
        const val NOTIFICATION_ID = 1
    }
}