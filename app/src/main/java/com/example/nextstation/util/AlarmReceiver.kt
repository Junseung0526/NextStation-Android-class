package com.example.nextstation.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Vibrator
import android.os.VibratorManager
import android.os.VibrationEffect
import android.telephony.SmsManager
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import java.util.Locale

class AlarmReceiver : BroadcastReceiver() {

    private var tts: TextToSpeech? = null

    override fun onReceive(context: Context, intent: Intent) {
        val destination = intent.getStringExtra("destination") ?: "목적지"
        val phoneNumber = intent.getStringExtra("phoneNumber")
        val message = intent.getStringExtra("message") ?: "곧 도착 예정입니다."

        Log.d("AlarmReceiver", "Alarm triggered for $destination")

        // 1. Vibration
        triggerVibration(context)

        // 2. TTS
        speakArrival(context, destination)

        // 3. SMS
        if (!phoneNumber.isNullOrBlank()) {
            sendSms(context, phoneNumber, message)
        }

        Toast.makeText(context, "$destination 도착 알림!", Toast.LENGTH_LONG).show()
    }

    private fun triggerVibration(context: Context) {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 500, 200, 500), -1))
        } else {
            vibrator.vibrate(longArrayOf(0, 500, 200, 500), -1)
        }
    }

    private fun speakArrival(context: Context, destination: String) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.KOREAN
                tts?.speak("잠시 후 $destination 에 도착합니다. 하차 준비를 해주세요.", TextToSpeech.QUEUE_FLUSH, null, "ArrivalID")
            }
        }
    }

    private fun sendSms(context: Context, phoneNumber: String, message: String) {
        try {
            val smsManager: SmsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.getSystemService(SmsManager::class.java)
            } else {
                SmsManager.getDefault()
            }
            smsManager.sendTextMessage(phoneNumber, null, message, null, null)
            Log.d("AlarmReceiver", "SMS sent to $phoneNumber")
        } catch (e: Exception) {
            Log.e("AlarmReceiver", "Failed to send SMS", e)
        }
    }
}