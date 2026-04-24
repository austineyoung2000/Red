package com.elitedarkkaiser.redmagic

import android.app.AppOpsManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Process
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return

        if (action != Intent.ACTION_BOOT_COMPLETED && action != Intent.ACTION_USER_UNLOCKED) return

        val triggerPrefs = context.getSharedPreferences("triggers", Context.MODE_PRIVATE)
        if (triggerPrefs.getBoolean("triggers_auto_start", false)) {
            HardwareController.enableTriggers()
            context.startService(Intent(context, TriggerRootService::class.java))
        }

        val prefs = context.getSharedPreferences("redmagic_hw_controls_prefs", Context.MODE_PRIVATE)
        val fanLedEnabled = prefs.getBoolean("fan_led_enabled", false)

        if (fanLedEnabled) {
            val shortRequest = OneTimeWorkRequestBuilder<FanLedRestoreWorker>()
                .setInitialDelay(10, TimeUnit.SECONDS)
                .addTag("fan_led_restore_short")
                .build()

            val longRequest = OneTimeWorkRequestBuilder<FanLedRestoreWorker>()
                .setInitialDelay(40, TimeUnit.SECONDS)
                .addTag("fan_led_restore_long")
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                "fan_led_restore_short",
                ExistingWorkPolicy.REPLACE,
                shortRequest
            )

            WorkManager.getInstance(context).enqueueUniqueWork(
                "fan_led_restore_long",
                ExistingWorkPolicy.REPLACE,
                longRequest
            )
        }

        val tracked = prefs.getStringSet("game_mode_packages", emptySet()) ?: emptySet()
        if (tracked.isNotEmpty() && hasUsageStatsPermission(context)) {
            context.startService(Intent(context, GameModeService::class.java))
        }
    }

    private fun hasUsageStatsPermission(context: Context): Boolean {
        val appOps = context.getSystemService(AppOpsManager::class.java)
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow("android:get_usage_stats", Process.myUid(), context.packageName)
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow("android:get_usage_stats", Process.myUid(), context.packageName)
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }
}
