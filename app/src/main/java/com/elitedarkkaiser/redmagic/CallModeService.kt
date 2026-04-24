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
                        val gameWasActive = prefs.getBoolean(PrefsKeys.GAME_LED_OWNER, false)
                        prefs.edit()
                            .putBoolean(PrefsKeys.CALL_LED_OWNER, false)
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
                    prefs.edit().putBoolean(PrefsKeys.CALL_LED_OWNER, true).apply()
                    FanLedRestoreWork.cancelAll(this@CallModeService)
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
                    val gameWasActive = prefs.getBoolean(PrefsKeys.GAME_LED_OWNER, false)
                    prefs.edit()
                        .putBoolean(PrefsKeys.CALL_LED_OWNER, false)
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
            val gameWasActive = prefs.getBoolean(PrefsKeys.GAME_LED_OWNER, false)
            prefs.edit()
                .putBoolean(PrefsKeys.CALL_LED_OWNER, false)
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


    private fun applyCallProfile() {
        val prefs = getSharedPreferences(PrefsKeys.HW_PREFS, Context.MODE_PRIVATE)

        val profile = ModeHardwareProfile(
            fanEnabled = prefs.getBoolean("call_mode_fan_enabled", false),
            fanLevel = prefs.getInt("call_mode_fan_level", 0),
            pumpEnabled = prefs.getBoolean("call_mode_pump_enabled", false),
            pumpProfile = prefs.getString("call_mode_pump_profile", "slow") ?: "slow",
            fanLedEnabled = prefs.getBoolean("call_mode_fan_led_enabled", true),
            fanLedEffect = prefs.getString("call_mode_fan_led_effect", "steady") ?: "steady",
            fanLedColor = prefs.getInt("call_mode_fan_led_color", prefs.getInt("fan_led_color", 1)),
            logoLedEnabled = prefs.getBoolean("call_mode_logo_led_enabled", true),
            logoLedEffect = prefs.getString("call_mode_logo_led_effect", "steady") ?: "steady",
            logoLedColor = prefs.getInt("call_mode_logo_led_color", prefs.getInt("logo_led_color", 1)),
            shoulderLedEnabled = prefs.getBoolean("call_mode_shoulder_led_enabled", true),
            shoulderLedEffect = prefs.getString("call_mode_shoulder_led_effect", "steady") ?: "steady",
            shoulderLedColor = prefs.getInt("call_mode_shoulder_led_color", prefs.getInt("shoulder_led_color", 8))
        )

        ModeHardwareApplier.apply(profile)

        android.util.Log.i(
            "RedmagicCallMode",
            "applied call profile fan=${profile.fanEnabled}/${profile.fanLevel} pump=${profile.pumpEnabled}/${profile.pumpProfile} fanLed=${profile.fanLedEnabled}/${profile.fanLedEffect}/${profile.fanLedColor} logo=${profile.logoLedEnabled}/${profile.logoLedEffect}/${profile.logoLedColor} shoulder=${profile.shoulderLedEnabled}/${profile.shoulderLedEffect}/${profile.shoulderLedColor}"
        )
    }

    private fun restoreNormalProfile() {
        val prefs = getSharedPreferences(PrefsKeys.HW_PREFS, Context.MODE_PRIVATE)

        val anyLedEnabled = NormalHardwareRestorer.restore(prefs)
        if (anyLedEnabled) {
            startService(Intent(this, FanLedService::class.java))
        }

        android.util.Log.i("RedmagicNormalRestore", "restored normal profile")
    }
}
