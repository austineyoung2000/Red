package com.elitedarkkaiser.redmagic

import android.content.Context
import android.content.Intent

object NormalLedRestoreRunner {
    fun restore(context: Context): Boolean {
        val prefs = context.getSharedPreferences(
            PrefsKeys.HW_PREFS,
            Context.MODE_PRIVATE
        )

        val anyLedEnabled = NormalHardwareRestorer.restore(prefs)

        if (anyLedEnabled) {
            context.startService(Intent(context, FanLedService::class.java))
        }

        return anyLedEnabled
    }
}
