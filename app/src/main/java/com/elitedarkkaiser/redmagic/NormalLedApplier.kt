package com.elitedarkkaiser.redmagic

import android.content.SharedPreferences

object NormalLedApplier {
    fun apply(prefs: SharedPreferences): Boolean {
        if (!LedOwnership.normalAllowed(prefs)) return false

        val fanEnabled = prefs.getBoolean(PrefsKeys.FAN_LED_ENABLED, false)
        val fanEffect = prefs.getString(PrefsKeys.FAN_LED_EFFECT, "steady") ?: "steady"
        val fanColor = prefs.getInt(PrefsKeys.FAN_LED_COLOR, 1)

        val logoEnabled = prefs.getBoolean(PrefsKeys.LOGO_LED_ENABLED, true)
        val logoEffect = prefs.getString(PrefsKeys.LOGO_LED_EFFECT, "steady") ?: "steady"
        val logoColor = prefs.getInt(PrefsKeys.LOGO_LED_COLOR, 1)

        val shoulderEnabled = prefs.getBoolean(PrefsKeys.SHOULDER_LED_ENABLED, true)
        val shoulderEffect = prefs.getString(PrefsKeys.SHOULDER_LED_EFFECT, "breathe") ?: "breathe"
        val shoulderColor = prefs.getInt(PrefsKeys.SHOULDER_LED_COLOR, 8)

        if (fanEnabled) {
            if (fanEffect.startsWith("preset:")) {
                HardwareController.setFanLedStockPreset(fanEffect.removePrefix("preset:"))
            } else {
                HardwareController.setFanLedEffect(fanEffect, fanColor)
            }
        } else {
            HardwareController.setFanLedEnabled(false)
        }

        if (logoEnabled) HardwareController.setLogoLedEffect(logoEffect, logoColor)
        else HardwareController.setLogoLedEnabled(false)

        if (shoulderEnabled) HardwareController.setShoulderLedEffect(shoulderEffect, shoulderColor)
        else HardwareController.setShoulderLedEnabled(false)

        return fanEnabled || logoEnabled || shoulderEnabled
    }
}
