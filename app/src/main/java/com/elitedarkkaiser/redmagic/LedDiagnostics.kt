package com.elitedarkkaiser.redmagic

import android.content.Context
import android.media.AudioManager
import android.telephony.TelephonyManager

object LedDiagnostics {
    fun build(context: Context): String {
        val prefs = context.getSharedPreferences(PrefsKeys.HW_PREFS, Context.MODE_PRIVATE)

        val audio = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val callState = try {
            val telephony = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            when (telephony.callState) {
                TelephonyManager.CALL_STATE_RINGING -> "RINGING"
                TelephonyManager.CALL_STATE_OFFHOOK -> "OFFHOOK"
                TelephonyManager.CALL_STATE_IDLE -> "IDLE"
                else -> "UNKNOWN"
            }
        } catch (_: Throwable) {
            "NO PERMISSION"
        }

        return """
LED Ownership
Normal allowed: ${LedOwnership.normalAllowed(prefs)}
Game owns LEDs: ${LedOwnership.gameOwns(prefs)}
Call owns LEDs: ${LedOwnership.callOwns(prefs)}

Call State
Audio mode: ${audio.mode}
Phone state: $callState

Saved Normal LEDs
Fan: ${prefs.getBoolean(PrefsKeys.FAN_LED_ENABLED, false)} / ${prefs.getString(PrefsKeys.FAN_LED_EFFECT, "steady")} / ${prefs.getInt(PrefsKeys.FAN_LED_COLOR, 1)}
Logo: ${prefs.getBoolean(PrefsKeys.LOGO_LED_ENABLED, true)} / ${prefs.getString(PrefsKeys.LOGO_LED_EFFECT, "steady")} / ${prefs.getInt(PrefsKeys.LOGO_LED_COLOR, 1)}
Shoulder: ${prefs.getBoolean(PrefsKeys.SHOULDER_LED_ENABLED, true)} / ${prefs.getString(PrefsKeys.SHOULDER_LED_EFFECT, "breathe")} / ${prefs.getInt(PrefsKeys.SHOULDER_LED_COLOR, 8)}
        """.trimIndent()
    }
}
