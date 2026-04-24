package com.elitedarkkaiser.redmagic

import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Handler
import android.os.IBinder
import android.os.Looper

class CallModeService : Service() {
    private val handler = Handler(Looper.getMainLooper())
    private var callModeActive = false

    private val pollRunnable = object : Runnable {
        override fun run() {
            try {
                val prefs = getSharedPreferences("redmagic_hw_controls_prefs", Context.MODE_PRIVATE)
                val enabled = prefs.getBoolean("call_mode_enabled", false)

                if (!enabled) {
                    if (callModeActive) {
                        restoreNormalProfile()
                        prefs.edit().putBoolean("call_mode_led_override_active", false).apply()
                        callModeActive = false
                    }
                    handler.postDelayed(this, 1500L)
                    return
                }

                val inCall = isInAnyCall()

                if (inCall && !callModeActive) {
                    callModeActive = true
                    prefs.edit().putBoolean("call_mode_led_override_active", true).apply()
                    applyCallProfile()
                } else if (!inCall && callModeActive) {
                    restoreNormalProfile()
                    prefs.edit().putBoolean("call_mode_led_override_active", false).apply()
                    callModeActive = false
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
        if (callModeActive) {
            restoreNormalProfile()
            getSharedPreferences("redmagic_hw_controls_prefs", Context.MODE_PRIVATE)
                .edit()
                .putBoolean("call_mode_led_override_active", false)
                .apply()
            callModeActive = false
        }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun isInAnyCall(): Boolean {
        val audio = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        return audio.mode == AudioManager.MODE_IN_CALL ||
            audio.mode == AudioManager.MODE_IN_COMMUNICATION
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
        val prefs = getSharedPreferences("redmagic_hw_controls_prefs", Context.MODE_PRIVATE)

        val fanEnabled = prefs.getBoolean("call_mode_fan_enabled", false)
        val fanLevel = prefs.getInt("call_mode_fan_level", 0)

        val pumpEnabled = prefs.getBoolean("call_mode_pump_enabled", false)
        val pumpProfile = prefs.getString("call_mode_pump_profile", "slow") ?: "slow"

        val fanLedEnabled = prefs.getBoolean("call_mode_fan_led_enabled", true)
        val fanLedEffect = prefs.getString("call_mode_fan_led_effect", "steady") ?: "steady"
        val fanLedColor = prefs.getInt("call_mode_fan_led_color", 7)

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
        val prefs = getSharedPreferences("redmagic_hw_controls_prefs", Context.MODE_PRIVATE)

        val fanEnabled = prefs.getBoolean("fan_enabled", false)
        val fanLevel = prefs.getInt("fan_level", 0)

        val pumpEnabled = prefs.getBoolean("pump_enabled", false)
        val pumpProfile = prefs.getString("pump_profile", "quick") ?: "quick"

        val fanLedEnabled = prefs.getBoolean("fan_led_enabled", false)
        val fanLedEffect = prefs.getString("fan_led_effect", "steady") ?: "steady"
        val fanLedColor = prefs.getInt("fan_led_color", 5)

        val logoLedEnabled = prefs.getBoolean("logo_led_enabled", true)
        val logoLedEffect = prefs.getString("logo_led_effect", "steady") ?: "steady"
        val logoLedColor = prefs.getInt("logo_led_color", 1)

        val shoulderLedEnabled = prefs.getBoolean("shoulder_led_enabled", true)
        val shoulderLedEffect = prefs.getString("shoulder_led_effect", "breathe") ?: "breathe"
        val shoulderLedColor = prefs.getInt("shoulder_led_color", 8)

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

        if (fanLedEnabled || logoLedEnabled || shoulderLedEnabled) {
            startService(Intent(this, FanLedService::class.java))
        }

        android.util.Log.i("RedmagicCallMode", "restored normal profile after call")
    }
}
