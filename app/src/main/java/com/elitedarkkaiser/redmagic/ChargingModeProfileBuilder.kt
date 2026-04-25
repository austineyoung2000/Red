package com.elitedarkkaiser.redmagic

import android.content.SharedPreferences

object ChargingModeProfileBuilder {
    fun build(prefs: SharedPreferences, full: Boolean = false): ModeHardwareProfile {
        val prefix = if (full) "charging_full_mode" else "charging_mode"

        return ModeHardwareProfile(
            fanEnabled = prefs.getBoolean("${prefix}_fan_enabled", false),
            fanLevel = prefs.getInt("${prefix}_fan_level", if (full) 0 else 2),
            pumpEnabled = false,
            pumpProfile = "quick",

            fanLedEnabled = prefs.getBoolean("${prefix}_fan_led_enabled", true),
            fanLedEffect = prefs.getString("${prefix}_fan_led_effect", if (full) "breathe" else "steady") ?: "steady",
            fanLedColor = prefs.getInt("${prefix}_fan_led_color", if (full) 2 else prefs.getInt(PrefsKeys.FAN_LED_COLOR, 1)),

            logoLedEnabled = prefs.getBoolean("${prefix}_logo_led_enabled", true),
            logoLedEffect = prefs.getString("${prefix}_logo_led_effect", if (full) "breathe" else "steady") ?: "steady",
            logoLedColor = prefs.getInt("${prefix}_logo_led_color", if (full) 2 else prefs.getInt(PrefsKeys.LOGO_LED_COLOR, 1)),

            shoulderLedEnabled = prefs.getBoolean("${prefix}_shoulder_led_enabled", true),
            shoulderLedEffect = prefs.getString("${prefix}_shoulder_led_effect", if (full) "breathe" else "steady") ?: "steady",
            shoulderLedColor = prefs.getInt("${prefix}_shoulder_led_color", if (full) 2 else prefs.getInt(PrefsKeys.SHOULDER_LED_COLOR, 8))
        )
    }
}
