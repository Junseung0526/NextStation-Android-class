package com.example.nextstation.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.*
import android.speech.tts.TextToSpeech
import android.telephony.SmsManager
import androidx.core.app.NotificationCompat
import com.example.nextstation.MainActivity
import com.example.nextstation.R
import java.util.*

class ArrivalService : Service(), TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isTtsInitialized = false
    private var pendingMessage: String? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        tts = TextToSpeech(this, this)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        val destination = intent?.getStringExtra("destination") ?: "목적지"
        
        when (action) {
            ACTION_START -> {
                val notification = createNotification(destination, "도착 알림이 예약되었습니다.")
                startForeground(NOTIFICATION_ID, notification)
            }
            ACTION_ARRIVED -> {
                val phoneNumber = intent.getStringExtra("phoneNumber")
                val message = intent.getStringExtra("message") ?: "곧 도착 예정입니다."
                
                handleArrival(destination, phoneNumber, message)
            }
        }

        return START_STICKY
    }

    private fun handleArrival(destination: String, phoneNumber: String?, message: String) {
        // 1. Update Notification
        val notification = createNotification(destination, "잠시 후 도착합니다! 하차 준비를 해주세요.")
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, notification)

        // 2. Vibration
        triggerVibration()

        // 3. TTS
        speakArrival(destination)

        // 4. SMS
        if (!phoneNumber.isNullOrBlank()) {
            sendSms(phoneNumber, message)
        }
        
        // Stop service after some time or keep it until user dismisses?
        // For MVP, we can stop it after a delay or let it be.
    }

    private fun triggerVibration() {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 500, 200, 500), -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(longArrayOf(0, 500, 200, 500), -1)
        }
    }

    private fun speakArrival(destination: String) {
        val text = "잠시 후 $destination 에 도착합니다. 하차 준비를 해주세요."
        if (isTtsInitialized) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "ArrivalID")
        } else {
            pendingMessage = text
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.KOREAN
            isTtsInitialized = true
            pendingMessage?.let {
                tts?.speak(it, TextToSpeech.QUEUE_FLUSH, null, "ArrivalID")
                pendingMessage = null
            }
        }
    }

    private fun sendSms(phoneNumber: String, message: String) {
        try {
            val smsManager: SmsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                getSystemService(SmsManager::class.java)
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }
            smsManager.sendTextMessage(phoneNumber, null, message, null, null)
        } catch (e: Exception) {
            // Log error safely
        }
    }

    private fun createNotification(destination: String, contentText: String): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("안심 하차")
            .setContentText("$destination: $contentText")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Arrival Service Channel",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }

    companion object {
        const val CHANNEL_ID = "arrival_service_channel"
        const val NOTIFICATION_ID = 1
        const val ACTION_START = "com.example.nextstation.ACTION_START"
        const val ACTION_ARRIVED = "com.example.nextstation.ACTION_ARRIVED"
    }
}