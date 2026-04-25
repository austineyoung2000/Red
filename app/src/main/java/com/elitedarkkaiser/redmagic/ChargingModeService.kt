package com.elitedarkkaiser.redmagic

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import android.os.Handler
import android.os.IBinder
import android.os.Looper

class ChargingModeService : Service() {
    private val handler = Handler(Looper.getMainLooper())
    private var chargingModeActive = false

    private val pollRunnable = object : Runnable {
        override fun run() {
            try {
                val prefs = getSharedPreferences(PrefsKeys.HW_PREFS, Context.MODE_PRIVATE)
                val enabled = prefs.getBoolean("charging_mode_enabled", false)
                val charging = isCharging()

                if (enabled && charging) {
                    val callOwns = prefs.getBoolean(PrefsKeys.CALL_LED_OWNER, false)

                    if (!chargingModeActive) {
                        chargingModeActive = true
                        prefs.edit().putBoolean(PrefsKeys.CHARGING_LED_OWNER, true).apply()
                        stopService(Intent(this@ChargingModeService, FanLedService::class.java))
                    }

                    if (!callOwns) {
                        ModeHardwareApplier.apply(
                            this@ChargingModeService,
                            ChargingModeProfileBuilder.build(prefs)
                        )
                    }
                } else if (chargingModeActive) {
                    val gameWasActive = prefs.getBoolean(PrefsKeys.GAME_LED_OWNER, false)

                    prefs.edit()
                        .putBoolean(PrefsKeys.CHARGING_LED_OWNER, false)
                        .putBoolean("force_game_mode_reapply", gameWasActive)
                        .apply()

                    chargingModeActive = false

                    if (gameWasActive) {
                        startService(Intent(this@ChargingModeService, GameModeService::class.java))
                    } else {
                        NormalLedRestoreRunner.restore(this@ChargingModeService)
                    }
                }
            } catch (_: Throwable) {
            } finally {
                handler.postDelayed(this, 1500L)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        handler.post(pollRunnable)
    }

    override fun onDestroy() {
        handler.removeCallbacks(pollRunnable)
        if (chargingModeActive) {
            getSharedPreferences(PrefsKeys.HW_PREFS, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(PrefsKeys.CHARGING_LED_OWNER, false)
                .apply()
            chargingModeActive = false
        }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun isCharging(): Boolean {
        val batteryManager = getSystemService(BATTERY_SERVICE) as BatteryManager
        return batteryManager.isCharging
    }
}
