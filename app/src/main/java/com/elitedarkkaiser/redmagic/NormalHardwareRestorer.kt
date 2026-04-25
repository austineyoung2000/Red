package com.elitedarkkaiser.redmagic

import android.content.SharedPreferences

object NormalHardwareRestorer {
    fun restore(prefs: SharedPreferences): Boolean {
        val fanEnabled = prefs.getBoolean("fan_enabled", false)
        val fanLevel = prefs.getInt("fan_level", 0)

        if (fanEnabled) {
            HardwareController.setFanLevel(fanLevel)
        } else {
            HardwareController.enableFan(false)
        }

        return NormalLedApplier.apply(prefs)
    }
}
