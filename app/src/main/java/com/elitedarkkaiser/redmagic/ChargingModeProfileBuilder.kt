package com.elitedarkkaiser.redmagic

import android.content.SharedPreferences

object ChargingModeProfileBuilder {
    fun build(prefs: SharedPreferences): ModeHardwareProfile {
        return ModeHardwareProfile(
            fanEnabled = prefs.getBoolean("charging_mode_fan_enabled", false),
            fanLevel = prefs.getInt("charging_mode_fan_level", 2),
            pumpEnabled = false,
            pumpProfile = "quick",

            fanLedEnabled = prefs.getBoolean("charging_mode_fan_led_enabled", true),
            fanLedEffect = prefs.getString("charging_mode_fan_led_effect", "steady") ?: "steady",
            fanLedColor = prefs.getInt("charging_mode_fan_led_color", prefs.getInt(PrefsKeys.FAN_LED_COLOR, 1)),

            logoLedEnabled = prefs.getBoolean("charging_mode_logo_led_enabled", true),
            logoLedEffect = prefs.getString("charging_mode_logo_led_effect", "steady") ?: "steady",
            logoLedColor = prefs.getInt("charging_mode_logo_led_color", prefs.getInt(PrefsKeys.LOGO_LED_COLOR, 1)),

            shoulderLedEnabled = prefs.getBoolean("charging_mode_shoulder_led_enabled", true),
            shoulderLedEffect = prefs.getString("charging_mode_shoulder_led_effect", "steady") ?: "steady",
            shoulderLedColor = prefs.getInt("charging_mode_shoulder_led_color", prefs.getInt(PrefsKeys.SHOULDER_LED_COLOR, 8))
        )
    }
}
