package com.elitedarkkaiser.redmagic

import android.content.SharedPreferences

object GameModeProfileBuilder {
    fun build(prefs: SharedPreferences, profile: Map<String, Any>): ModeHardwareProfile {
        val fanLedModeType = profile["fanLedModeType"] as? String ?: "basic"
        val fanLedPresetValue = profile["fanLedPresetValue"] as? String ?: ""

        return ModeHardwareProfile(
            fanEnabled = profile["fanEnabled"] as? Boolean ?: prefs.getBoolean("game_mode_fan_enabled", true),
            fanLevel = profile["fanLevel"] as? Int ?: prefs.getInt("game_mode_fan_level", 3),
            pumpEnabled = prefs.getBoolean("game_mode_pump_enabled", false),
            pumpProfile = prefs.getString("game_mode_pump_profile", "quick") ?: "quick",

            fanLedEnabled = profile["fanLedEnabled"] as? Boolean ?: prefs.getBoolean("game_mode_fan_led_enabled", true),
            fanLedEffect = profile["fanLedEffect"] as? String ?: prefs.getString("game_mode_fan_led_effect", "steady") ?: "steady",
            fanLedColor = profile["fanLedColor"] as? Int
                ?: prefs.getInt("game_mode_fan_led_color", prefs.getInt(PrefsKeys.FAN_LED_COLOR, 1)),
            fanLedPresetValue =
                if (fanLedModeType == "preset" && fanLedPresetValue.isNotBlank())
                    fanLedPresetValue
                else "",

            logoLedEnabled = profile["logoLedEnabled"] as? Boolean ?: prefs.getBoolean("game_mode_logo_led_enabled", true),
            logoLedEffect = profile["logoLedEffect"] as? String ?: prefs.getString("game_mode_logo_led_effect", "steady") ?: "steady",
            logoLedColor = profile["logoLedColor"] as? Int ?: prefs.getInt("game_mode_logo_led_color", prefs.getInt(PrefsKeys.LOGO_LED_COLOR, 1)),

            shoulderLedEnabled = profile["shoulderLedEnabled"] as? Boolean ?: prefs.getBoolean("game_mode_shoulder_led_enabled", true),
            shoulderLedEffect = profile["shoulderLedEffect"] as? String ?: prefs.getString("game_mode_shoulder_led_effect", "breathe") ?: "breathe",
            shoulderLedColor = profile["shoulderLedColor"] as? Int ?: prefs.getInt("game_mode_shoulder_led_color", prefs.getInt(PrefsKeys.SHOULDER_LED_COLOR, 8))
        )
    }
}
