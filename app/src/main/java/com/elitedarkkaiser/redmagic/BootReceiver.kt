package com.elitedarkkaiser.redmagic

import android.app.AppOpsManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Process

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return

        if (action != Intent.ACTION_BOOT_COMPLETED && action != Intent.ACTION_USER_UNLOCKED) return

        val bootPrefs = context.getSharedPreferences(PrefsKeys.HW_PREFS, Context.MODE_PRIVATE)
        val normalLedsAllowed = LedOwnership.normalAllowed(bootPrefs)

        if (normalLedsAllowed) {
            restorePersistentHardware(context)
        }

        val triggerPrefs = context.getSharedPreferences(PrefsKeys.TRIGGER_PREFS, Context.MODE_PRIVATE)
        if (triggerPrefs.getBoolean("triggers_auto_start", false)) {
            HardwareController.enableTriggers()
            context.startService(Intent(context, TriggerRootService::class.java))
        }

        val prefs = context.getSharedPreferences(PrefsKeys.HW_PREFS, Context.MODE_PRIVATE)
        val fanLedEnabled = prefs.getBoolean("fan_led_enabled", false)

        if (normalLedsAllowed && fanLedEnabled) {
            FanLedRestoreWork.scheduleBootRestore(context)
        }

        if (prefs.getBoolean("call_mode_enabled", false)) {
            context.startService(Intent(context, CallModeService::class.java))
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

    private fun restorePersistentHardware(context: Context) {
        val prefs = context.getSharedPreferences(PrefsKeys.HW_PREFS, Context.MODE_PRIVATE)
        val triggerPrefs = context.getSharedPreferences(PrefsKeys.TRIGGER_PREFS, Context.MODE_PRIVATE)

        val autoPumpEnabled = false
        val autoFanEnabled = prefs.getBoolean("auto_fan_curve_enabled", false)

        val anyLedEnabled = NormalLedApplier.apply(prefs)
        if (anyLedEnabled) {
            context.startService(Intent(context, FanLedService::class.java))
        }

        if (MicroPumpController.isSmartSaved(context)) {
            context.startService(Intent(context, MicroPumpService::class.java))
        } else if (MicroPumpController.isEnabledSaved(context)) {
            MicroPumpController.setEnabled(context, true)
        }

        if (autoFanEnabled) {
            context.startService(Intent(context, AutoFanService::class.java))
        }

        if (triggerPrefs.getBoolean("triggers_auto_start", false)) {
            HardwareController.enableTriggers()
            context.startService(Intent(context, TriggerRootService::class.java))
        }

        context.startService(Intent(context, GameModeService::class.java))

        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            val delayedPrefs = context.getSharedPreferences(PrefsKeys.HW_PREFS, Context.MODE_PRIVATE)

            if (!LedOwnership.normalAllowed(delayedPrefs)) {
                android.util.Log.i("RedmagicBoot", "boot delayed normal restore skipped because another mode owns LEDs")
                return@postDelayed
            }

            val delayedAnyLedEnabled = NormalLedApplier.apply(delayedPrefs)
            if (delayedAnyLedEnabled) {
                context.startService(Intent(context, FanLedService::class.java))
            }

            android.util.Log.i(
                "RedmagicBoot",
                "boot restore reapplied normal LEDs via shared applier microPump=${MicroPumpController.isEnabledSaved(context)} smartMicroPump=${MicroPumpController.isSmartSaved(context)} autoFan=$autoFanEnabled"
            )
        }, 10000L)
    }

}
