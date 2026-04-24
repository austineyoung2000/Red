package com.elitedarkkaiser.redmagic

import android.content.Context
import androidx.work.WorkManager

object FanLedRestoreWork {
    fun cancelAll(context: Context) {
        val wm = WorkManager.getInstance(context)

        wm.cancelUniqueWork("fan_led_restore_short")
        wm.cancelUniqueWork("fan_led_restore_long")
        wm.cancelUniqueWork("fan_led_restore")
    }
}
