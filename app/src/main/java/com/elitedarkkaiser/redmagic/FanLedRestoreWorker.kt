package com.elitedarkkaiser.redmagic

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class FanLedRestoreWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : Worker(appContext, workerParams) {

    override fun doWork(): Result {
        val prefs = applicationContext.getSharedPreferences(
            PrefsKeys.HW_PREFS,
            Context.MODE_PRIVATE
        )

        if (!LedOwnership.normalAllowed(prefs)) {
            return Result.success()
        }

        return try {
            NormalLedApplier.apply(prefs)
            Result.success()
        } catch (_: Throwable) {
            Result.retry()
        }
    }
}
