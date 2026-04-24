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

    fun scheduleBootRestore(context: Context) {
        val shortRequest =
            androidx.work.OneTimeWorkRequestBuilder<FanLedRestoreWorker>()
                .setInitialDelay(10, java.util.concurrent.TimeUnit.SECONDS)
                .addTag("fan_led_restore_short")
                .build()

        val longRequest =
            androidx.work.OneTimeWorkRequestBuilder<FanLedRestoreWorker>()
                .setInitialDelay(40, java.util.concurrent.TimeUnit.SECONDS)
                .addTag("fan_led_restore_long")
                .build()

        val wm = WorkManager.getInstance(context)

        wm.enqueueUniqueWork(
            "fan_led_restore_short",
            androidx.work.ExistingWorkPolicy.REPLACE,
            shortRequest
        )

        wm.enqueueUniqueWork(
            "fan_led_restore_long",
            androidx.work.ExistingWorkPolicy.REPLACE,
            longRequest
        )
    }
}
