package com.elitedarkkaiser.redmagic

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.telephony.TelephonyManager
import android.content.IntentFilter
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper

class FanLedService : Service() {

    companion object {
        private const val CHANNEL_ID = "fan_led_service_channel"
        private const val NOTIF_ID = 1102
    }

    private val handler = Handler(Looper.getMainLooper())

    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_SCREEN_OFF -> {
                    turnOffAllManagedLeds()
                }
                Intent.ACTION_SCREEN_ON,
                Intent.ACTION_USER_PRESENT -> {
                    handler.postDelayed({
                        reapplySavedLedState()
                    }, 1500)
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIF_ID, buildNotification("Fan LED persistence active"))
        registerFanLedReceiver()
        reapplySavedLedState()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        reapplySavedLedState()
        return START_STICKY
    }

    override fun onDestroy() {
        try {
            unregisterReceiver(screenReceiver)
        } catch (_: Throwable) {
        }
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun registerFanLedReceiver() {
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_USER_PRESENT)
        }
        registerReceiver(screenReceiver, filter)
    }

    private fun isCallActiveOrRinging(): Boolean {
        return try {
            val audio = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            if (
                audio.mode == AudioManager.MODE_IN_CALL ||
                audio.mode == AudioManager.MODE_IN_COMMUNICATION
            ) {
                return true
            }

            val telephony = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            telephony.callState == TelephonyManager.CALL_STATE_RINGING ||
                telephony.callState == TelephonyManager.CALL_STATE_OFFHOOK
        } catch (_: Throwable) {
            false
        }
    }

    private fun reapplySavedLedState() {
        val prefs = getSharedPreferences(PrefsKeys.HW_PREFS, Context.MODE_PRIVATE)

        if (!LedOwnership.normalAllowed(prefs) || isCallActiveOrRinging()) {
            android.util.Log.i(
                "RedmagicNormalLed",
                "Normal LED profile blocked because another mode owns LEDs"
            )
            stopSelf()
            return
        }

        val anyLedEnabled = NormalLedApplier.apply(prefs)

        if (anyLedEnabled) {
            updateNotification("Normal LED profile active")
        } else {
            stopSelf()
        }
    }

    private fun turnOffAllManagedLeds() {
        HardwareController.setFanLedEnabled(false)
        HardwareController.setLogoLedEnabled(false)
        HardwareController.setShoulderLedEnabled(false)
    }

    private fun buildNotification(text: String): Notification {
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
        } else {
            Notification.Builder(this)
        }

        return builder
            .setContentTitle("RedMagic Control")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_menu_manage)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(text: String) {
        val nm = getSystemService(NotificationManager::class.java)
        nm.notify(NOTIF_ID, buildNotification(text))
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "LED Persistence Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps custom LED settings synced with screen state"
            }

            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(channel)
        }
    }
}
