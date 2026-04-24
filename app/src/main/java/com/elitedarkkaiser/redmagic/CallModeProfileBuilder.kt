package com.elitedarkkaiser.redmagic

import android.content.SharedPreferences

object CallModeProfileBuilder {
    fun build(prefs: SharedPreferences): ModeHardwareProfile {
        return ModeHardwareProfile(
            fanEnabled = prefs.getBoolean("call_mode_fan_enabled", false),
            fanLevel = prefs.getInt("call_mode_fan_level", 0),
            pumpEnabled = prefs.getBoolean("call_mode_pump_enabled", false),
            pumpProfile = prefs.getString("call_mode_pump_profile", "slow") ?: "slow",
            fanLedEnabled = prefs.getBoolean("call_mode_fan_led_enabled", true),
            fanLedEffect = prefs.getString("call_mode_fan_led_effect", "steady") ?: "steady",
            fanLedColor = prefs.getInt("call_mode_fan_led_color", prefs.getInt(PrefsKeys.FAN_LED_COLOR, 1)),
            logoLedEnabled = prefs.getBoolean("call_mode_logo_led_enabled", true),
            logoLedEffect = prefs.getString("call_mode_logo_led_effect", "steady") ?: "steady",
            logoLedColor = prefs.getInt("call_mode_logo_led_color", prefs.getInt(PrefsKeys.LOGO_LED_COLOR, 1)),
            shoulderLedEnabled = prefs.getBoolean("call_mode_shoulder_led_enabled", true),
            shoulderLedEffect = prefs.getString("call_mode_shoulder_led_effect", "steady") ?: "steady",
            shoulderLedColor = prefs.getInt("call_mode_shoulder_led_color", prefs.getInt(PrefsKeys.SHOULDER_LED_COLOR, 8))
        )
    }
}
