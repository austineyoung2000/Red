package com.elitedarkkaiser.redmagic

import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.telephony.TelephonyManager
import androidx.work.WorkManager

class CallModeService : Service() {
    private val handler = Handler(Looper.getMainLooper())
    private var callModeActive = false

    private val pollRunnable = object : Runnable {
        override fun run() {
            try {
                val prefs = getSharedPreferences(PrefsKeys.HW_PREFS, Context.MODE_PRIVATE)
                val enabled = prefs.getBoolean("call_mode_enabled", false)

                if (!enabled) {
                    if (callModeActive) {
                        val gameWasActive = prefs.getBoolean("game_mode_led_override_active", false)
                        prefs.edit()
                            .putBoolean("call_mode_led_override_active", false)
                            .putBoolean("force_game_mode_reapply", gameWasActive)
                            .apply()
                        if (!gameWasActive) {
                            restoreNormalProfile()
                        }
                        callModeActive = false
                    }
                    handler.postDelayed(this, 300L)
                    return
                }

                val inCall = isInAnyCall()

                if (inCall && !callModeActive) {
                    callModeActive = true
                    prefs.edit().putBoolean("call_mode_led_override_active", true).apply()
                    WorkManager.getInstance(this@CallModeService).cancelUniqueWork("fan_led_restore_short")
                    WorkManager.getInstance(this@CallModeService).cancelUniqueWork("fan_led_restore_long")
                    WorkManager.getInstance(this@CallModeService).cancelUniqueWork("fan_led_restore")
                    stopService(Intent(this@CallModeService, FanLedService::class.java))
                    applyCallProfile()
                    handler.postDelayed({
                        if (callModeActive && isInAnyCall()) {
                            applyCallProfile()
                        }
                    }, 350L)
                    handler.postDelayed({
                        if (callModeActive && isInAnyCall()) {
                            applyCallProfile()
                        }
                    }, 1000L)
                } else if (!inCall && callModeActive) {
                    val gameWasActive = prefs.getBoolean("game_mode_led_override_active", false)
                    prefs.edit()
                        .putBoolean("call_mode_led_override_active", false)
                        .putBoolean("force_game_mode_reapply", gameWasActive)
                        .apply()
                    if (!gameWasActive) {
                        restoreNormalProfile()
                    }
                    callModeActive = false
                }
            } catch (_: Throwable) {
            } finally {
                handler.postDelayed(this, 300L)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        handler.post(pollRunnable)
    }

    override fun onDestroy() {
        handler.removeCallbacks(pollRunnable)
        if (callModeActive) {
            val prefs = getSharedPreferences(PrefsKeys.HW_PREFS, Context.MODE_PRIVATE)
            val gameWasActive = prefs.getBoolean("game_mode_led_override_active", false)
            prefs.edit()
                .putBoolean("call_mode_led_override_active", false)
                .putBoolean("force_game_mode_reapply", gameWasActive)
                .apply()
            if (!gameWasActive) {
                restoreNormalProfile()
            }
            callModeActive = false
        }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun isInAnyCall(): Boolean {
        val audio = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (
            audio.mode == AudioManager.MODE_IN_CALL ||
            audio.mode == AudioManager.MODE_IN_COMMUNICATION
        ) {
            return true
        }

        return try {
            val telephony = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            telephony.callState == TelephonyManager.CALL_STATE_RINGING ||
                telephony.callState == TelephonyManager.CALL_STATE_OFFHOOK
        } catch (_: Throwable) {
            false
        }
    }

    private fun applyFanLed(effect: String, color: Int) {
        HardwareController.setFanLedEnabled(true)
        if (effect.startsWith("preset:")) {
            HardwareController.setFanLedStockPreset(effect.removePrefix("preset:"))
        } else {
            HardwareController.setFanLedEffect(effect, color)
        }
    }

    private fun applyCallProfile() {
        val prefs = getSharedPreferences(PrefsKeys.HW_PREFS, Context.MODE_PRIVATE)

        val fanEnabled = prefs.getBoolean("call_mode_fan_enabled", false)
        val fanLevel = prefs.getInt("call_mode_fan_level", 0)

        val pumpEnabled = prefs.getBoolean("call_mode_pump_enabled", false)
        val pumpProfile = prefs.getString("call_mode_pump_profile", "slow") ?: "slow"

        val fanLedEnabled = prefs.getBoolean("call_mode_fan_led_enabled", true)
        val fanLedEffect = prefs.getString("call_mode_fan_led_effect", "steady") ?: "steady"
        val fanLedColor = prefs.getInt("call_mode_fan_led_color", prefs.getInt("fan_led_color", 1))

        val logoLedEnabled = prefs.getBoolean("call_mode_logo_led_enabled", true)
        val logoLedEffect = prefs.getString("call_mode_logo_led_effect", "steady") ?: "steady"
        val logoLedColor = prefs.getInt("call_mode_logo_led_color", 7)

        val shoulderLedEnabled = prefs.getBoolean("call_mode_shoulder_led_enabled", true)
        val shoulderLedEffect = prefs.getString("call_mode_shoulder_led_effect", "steady") ?: "steady"
        val shoulderLedColor = prefs.getInt("call_mode_shoulder_led_color", 7)

        if (fanEnabled) HardwareController.setFanLevel(fanLevel) else HardwareController.enableFan(false)
        if (pumpEnabled) HardwareController.setPumpProfile(pumpProfile) else HardwareController.enablePump(false)

        if (fanLedEnabled) applyFanLed(fanLedEffect, fanLedColor) else HardwareController.setFanLedEnabled(false)

        if (logoLedEnabled) {
            HardwareController.setLogoLedEnabled(true)
            HardwareController.setLogoLedEffect(logoLedEffect, logoLedColor)
        } else {
            HardwareController.setLogoLedEnabled(false)
        }

        if (shoulderLedEnabled) {
            HardwareController.setShoulderLedEnabled(true)
            HardwareController.setShoulderLedEffect(shoulderLedEffect, shoulderLedColor)
        } else {
            HardwareController.setShoulderLedEnabled(false)
        }

        android.util.Log.i(
            "RedmagicCallMode",
            "applied call profile fan=$fanEnabled/$fanLevel pump=$pumpEnabled/$pumpProfile fanLed=$fanLedEnabled/$fanLedEffect/$fanLedColor logo=$logoLedEnabled/$logoLedEffect/$logoLedColor shoulder=$shoulderLedEnabled/$shoulderLedEffect/$shoulderLedColor"
        )
    }

    private fun restoreNormalProfile() {
        val prefs = getSharedPreferences(PrefsKeys.HW_PREFS, Context.MODE_PRIVATE)

        val fanEnabled = prefs.getBoolean("fan_enabled", false)
        val fanLevel = prefs.getInt("fan_level", 0)
        val pumpEnabled = prefs.getBoolean("pump_enabled", false)
        val pumpProfile = prefs.getString("pump_profile", "quick") ?: "quick"

        if (fanEnabled) {
            HardwareController.setFanLevel(fanLevel)
        } else {
            HardwareController.enableFan(false)
        }

        if (pumpEnabled) {
            HardwareController.setPumpProfile(pumpProfile)
        } else {
            HardwareController.enablePump(false)
        }

        val anyLedEnabled = NormalLedApplier.apply(prefs)
        if (anyLedEnabled) {
            startService(Intent(this, FanLedService::class.java))
        }

        android.util.Log.i("RedmagicCallMode", "restored normal profile after call")
    }
}
