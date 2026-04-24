package com.elitedarkkaiser.redmagic

import android.content.SharedPreferences

object NormalHardwareRestorer {
    fun restore(prefs: SharedPreferences): Boolean {
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

        return NormalLedApplier.apply(prefs)
    }
}
