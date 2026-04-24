package com.elitedarkkaiser.redmagic

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.BatteryManager
import android.os.Handler
import android.os.IBinder
import android.os.Looper

class MicroPumpService : Service() {
    companion object {
        private const val CHANNEL_ID = "micro_pump_channel"
        private const val NOTIF_ID = 2402
        private const val POLL_MS = 15000L
        private const val ON_TEMP_F = 95f
        private const val OFF_TEMP_F = 88f
    }

    private val handler = Handler(Looper.getMainLooper())
    private var pumpOn = false

    private val pollRunnable = object : Runnable {
        override fun run() {
            applyRule()
            handler.postDelayed(this, POLL_MS)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIF_ID, buildNotification("Micro Pump smart control active"))
        applyRule()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        MicroPumpController.saveSmart(this, true)
        MicroPumpController.saveEnabled(this, true)
        handler.removeCallbacks(pollRunnable)
        handler.post(pollRunnable)
        return START_STICKY
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        if (MicroPumpController.isSmartSaved(this)) {
            val intent = Intent(this, MicroPumpService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(intent)
            else startService(intent)
        }
        super.onDestroy()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        if (MicroPumpController.isSmartSaved(this)) {
            val intent = Intent(this, MicroPumpService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(intent)
            else startService(intent)
        }
        super.onTaskRemoved(rootIntent)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun applyRule() {
        if (MicroPumpController.isForceActive(this)) {
            MicroPumpController.setEnabled(this, true)
            pumpOn = true
            val remainingMin = ((MicroPumpController.forceUntil(this) - System.currentTimeMillis()) / 60000L).coerceAtLeast(0L)
            updateNotification("Micro Pump forced ON • ${remainingMin} min left")
            return
        } else if (!MicroPumpController.isSmartSaved(this)) {
            MicroPumpController.setEnabled(this, false)
            stopSelf()
            return
        }

        val tempF = DashboardSnapshot.readCpuTempF().toFloatOrNull()
        if (tempF == null) {
            updateNotification("Micro Pump active • Temp unknown")
            return
        }

        val batteryManager = getSystemService(BATTERY_SERVICE) as BatteryManager
        val charging = batteryManager.isCharging

        val onThreshold = if (charging) 90f else ON_TEMP_F
        val offThreshold = if (charging) 84f else OFF_TEMP_F

        val mode = when {
            tempF >= 105f -> "Performance"
            tempF >= 95f -> "Balanced"
            charging && tempF >= 90f -> "Balanced"
            else -> "Silent"
        }

        if (!pumpOn && tempF >= onThreshold) {
            MicroPumpController.setEnabled(this, true)
            pumpOn = true
        } else if (pumpOn && tempF <= offThreshold) {
            MicroPumpController.setEnabled(this, false)
            pumpOn = false
        }

        updateNotification(
            "Micro Pump: ${if (pumpOn) "ON" else "OFF"} • $mode • ${tempF}°F • ${if (charging) "Charging" else "Battery"}"
        )
    }

    private fun buildNotification(text: String): Notification {
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) Notification.Builder(this, CHANNEL_ID)
        else Notification.Builder(this)

        return builder
            .setContentTitle("RedMagic Control")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_menu_manage)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(text: String) {
        getSystemService(NotificationManager::class.java).notify(NOTIF_ID, buildNotification(text))
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getSystemService(NotificationManager::class.java).createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "Micro Pump", NotificationManager.IMPORTANCE_LOW)
            )
        }
    }
}
