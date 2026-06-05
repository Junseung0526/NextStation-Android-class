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
import com.example.nextstation.domain.repository.ArrivalRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class ArrivalService : Service(), TextToSpeech.OnInitListener {

    @Inject
    lateinit var repository: ArrivalRepository

    private var tts: TextToSpeech? = null
    private var isTtsInitialized = false
    private var pendingMessage: String? = null

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var pollingJob: Job? = null
    private var serviceStartTime = 0L
    private var estimatedTravelSeconds = 1500

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        tts = TextToSpeech(this, this)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        val destination = intent?.getStringExtra("destination") ?: "목적지"

        // Android 8.0+ requires calling startForeground() within 5 seconds of startForegroundService()
        val notification = createNotification(destination, "알림 서비스를 준비 중입니다...", CHANNEL_SILENT_ID)
        startForeground(NOTIFICATION_ID, notification)
        
        when (action) {
            ACTION_START -> {
                val arsId = intent.getStringExtra("arsId") ?: ""
                val busNumber = intent.getStringExtra("busNumber") ?: ""
                val leadMinutes = intent.getIntExtra("leadMinutes", 5)
                val phoneNumber = intent.getStringExtra("phoneNumber")
                val message = intent.getStringExtra("message") ?: "곧 도착 예정입니다."
                val rawGpsX = intent.getDoubleExtra("gpsX", 0.0)
                val rawGpsY = intent.getDoubleExtra("gpsY", 0.0)
                val gpsX = if (rawGpsX == 0.0) 127.0276 else rawGpsX
                val gpsY = if (rawGpsY == 0.0) 37.4979 else rawGpsY
                
                estimatedTravelSeconds = intent.getIntExtra("travelSeconds", 1500)
                serviceStartTime = System.currentTimeMillis()

                isServiceRunningFlow.value = true
                activeDestinationFlow.value = destination
                activeBusNumberFlow.value = busNumber
                activeArsIdFlow.value = arsId
                activeLeadMinutesFlow.value = leadMinutes
                activeGpsXFlow.value = gpsX
                activeGpsYFlow.value = gpsY
                remainingMinutesState.value = estimatedTravelSeconds / 60

                val startNotification = createNotification(destination, "${busNumber}번 버스 도착 실시간 감시 중...", CHANNEL_SILENT_ID)
                val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                manager.notify(NOTIFICATION_ID, startNotification)

                if (arsId.isNotBlank() && busNumber.isNotBlank()) {
                    startPolling(destination, arsId, busNumber, leadMinutes, phoneNumber, message)
                }
            }
            ACTION_ARRIVED -> {
                val phoneNumber = intent.getStringExtra("phoneNumber")
                val message = intent.getStringExtra("message") ?: "곧 도착 예정입니다."
                
                handleArrival(destination, phoneNumber, message)
            }
            ACTION_STOP -> {
                stopSelf()
            }
        }

        return START_STICKY
    }

    private fun startPolling(
        destination: String,
        arsId: String,
        busNumber: String,
        leadMinutes: Int,
        phoneNumber: String?,
        message: String
    ) {
        pollingJob?.cancel()
        pollingJob = serviceScope.launch {
            while (isActive) {
                val arrivals = repository.getRealTimeArrival(arsId)
                val targetArrival = arrivals.firstOrNull { it.busNumber == busNumber }

                if (targetArrival != null) {
                    val remainingSeconds = targetArrival.travelTimeSeconds

                    // Trigger alarm if remaining time is within the lead minutes threshold
                    if (remainingSeconds in 0..(leadMinutes * 60)) {
                        handleArrival(destination, phoneNumber, message)
                        break // Stop polling after arrival trigger
                    }

                    // Update notification with remaining time
                    val remainingMinutes = remainingSeconds / 60
                    remainingMinutesState.value = remainingMinutes
                    
                    val updateNotification = createNotification(
                        destination,
                        "실시간 감시 중: ${targetArrival.busNumber}번 버스 도착까지 약 ${remainingMinutes}분 남음",
                        CHANNEL_SILENT_ID,
                        showStopAction = true
                    )
                    val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    manager.notify(NOTIFICATION_ID, updateNotification)
                } else {
                    val elapsedSeconds = ((System.currentTimeMillis() - serviceStartTime) / 1000).toInt()
                    val calculatedSeconds = estimatedTravelSeconds - elapsedSeconds
                    val fallbackSeconds = if (calculatedSeconds < 60) 60 else calculatedSeconds
                    val remainingMinutes = fallbackSeconds / 60
                    remainingMinutesState.value = remainingMinutes

                    if (fallbackSeconds in 0..(leadMinutes * 60)) {
                        handleArrival(destination, phoneNumber, message)
                        break
                    }

                    val updateNotification = createNotification(
                        destination,
                        "실시간 감시 중: 도착 예정까지 약 ${remainingMinutes}분 (예측 모델 동작 중)",
                        CHANNEL_SILENT_ID,
                        showStopAction = true
                    )
                    val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    manager.notify(NOTIFICATION_ID, updateNotification)
                }

                delay(30000L) // Poll every 30 seconds
            }
        }
    }

    private fun handleArrival(destination: String, phoneNumber: String?, message: String) {
        // 1. Update Notification with Stop action on Alert Channel (makes sound/vibration)
        val notification = createNotification(
            destination, 
            "잠시 후 도착합니다! 하차 준비를 해주세요.",
            CHANNEL_ALERT_ID,
            showStopAction = true
        )
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

    private fun createNotification(
        destination: String, 
        contentText: String,
        channelId: String,
        showStopAction: Boolean = false
    ): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(this, channelId)
            .setContentTitle("NextStation")
            .setContentText("$destination: $contentText")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(if (channelId == CHANNEL_ALERT_ID) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_LOW)
            .setOnlyAlertOnce(true)

        if (showStopAction) {
            val stopIntent = Intent(this, ArrivalService::class.java).apply {
                action = ACTION_STOP
            }
            val stopPendingIntent = PendingIntent.getService(
                this, 1, stopIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            builder.addAction(android.R.drawable.ic_menu_close_clear_cancel, "알림 끄기", stopPendingIntent)
        }

        return builder.build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val silentChannel = NotificationChannel(
                CHANNEL_SILENT_ID,
                "실시간 이동 경로 (묵음)",
                NotificationManager.IMPORTANCE_LOW
            )
            val alertChannel = NotificationChannel(
                CHANNEL_ALERT_ID,
                "도착 알림 (소리/진동)",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(silentChannel)
            manager.createNotificationChannel(alertChannel)
        }
    }

    override fun onDestroy() {
        isServiceRunningFlow.value = false
        activeDestinationFlow.value = ""
        activeBusNumberFlow.value = ""
        activeArsIdFlow.value = ""
        activeLeadMinutesFlow.value = 5
        activeGpsXFlow.value = 0.0
        activeGpsYFlow.value = 0.0
        remainingMinutesState.value = null

        serviceScope.cancel() // Cancel coroutines including the polling loop
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }

    companion object {
        const val CHANNEL_SILENT_ID = "arrival_service_channel_silent"
        const val CHANNEL_ALERT_ID = "arrival_service_channel_alert"
        const val NOTIFICATION_ID = 1
        const val ACTION_START = "com.example.nextstation.ACTION_START"
        const val ACTION_ARRIVED = "com.example.nextstation.ACTION_ARRIVED"
        const val ACTION_STOP = "com.example.nextstation.ACTION_STOP"

        val isServiceRunningFlow = MutableStateFlow(false)
        val activeDestinationFlow = MutableStateFlow("")
        val activeBusNumberFlow = MutableStateFlow("")
        val activeArsIdFlow = MutableStateFlow("")
        val activeLeadMinutesFlow = MutableStateFlow(5)
        val activeGpsXFlow = MutableStateFlow(0.0)
        val activeGpsYFlow = MutableStateFlow(0.0)
        val remainingMinutesState = MutableStateFlow<Int?>(null)
    }
}
