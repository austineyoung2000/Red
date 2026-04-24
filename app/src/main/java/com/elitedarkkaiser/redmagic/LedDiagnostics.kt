package com.elitedarkkaiser.redmagic

import android.content.Context

object LedDiagnostics {
    fun build(context: Context): String {
        val prefs = context.getSharedPreferences(PrefsKeys.HW_PREFS, Context.MODE_PRIVATE)

        val audioMode = CallStateMonitor.audioMode(context)
        val callState = CallStateMonitor.phoneStateLabel(context)

        return """
LED Ownership
Normal allowed: ${LedOwnership.normalAllowed(prefs)}
Game owns LEDs: ${LedOwnership.gameOwns(prefs)}
Call owns LEDs: ${LedOwnership.callOwns(prefs)}

Call State
Audio mode: $audioMode
Phone state: $callState

Saved Normal LEDs
Fan: ${prefs.getBoolean(PrefsKeys.FAN_LED_ENABLED, false)} / ${prefs.getString(PrefsKeys.FAN_LED_EFFECT, "steady")} / ${prefs.getInt(PrefsKeys.FAN_LED_COLOR, 1)}
Logo: ${prefs.getBoolean(PrefsKeys.LOGO_LED_ENABLED, true)} / ${prefs.getString(PrefsKeys.LOGO_LED_EFFECT, "steady")} / ${prefs.getInt(PrefsKeys.LOGO_LED_COLOR, 1)}
Shoulder: ${prefs.getBoolean(PrefsKeys.SHOULDER_LED_ENABLED, true)} / ${prefs.getString(PrefsKeys.SHOULDER_LED_EFFECT, "breathe")} / ${prefs.getInt(PrefsKeys.SHOULDER_LED_COLOR, 8)}
        """.trimIndent()
    }
}
