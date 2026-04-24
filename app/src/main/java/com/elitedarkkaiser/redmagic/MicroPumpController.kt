package com.elitedarkkaiser.redmagic

import android.content.Context

object MicroPumpController {
    const val KEY_ENABLED = "micro_pump_enabled"
    const val KEY_SMART_ENABLED = "micro_pump_smart_enabled"

    private const val PROC_PATH = "/proc/driver/micropump/enable"
    private const val SETTINGS_KEY = "liquid_cooling_off_on"

    fun isEnabledSaved(context: Context): Boolean =
        prefs(context).getBoolean(KEY_ENABLED, false)

    fun isSmartSaved(context: Context): Boolean =
        prefs(context).getBoolean(KEY_SMART_ENABLED, false)

    fun saveEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit()
            .putBoolean(KEY_ENABLED, enabled)
            .putBoolean("pump_enabled", enabled)
            .apply()
    }

    fun saveSmart(context: Context, enabled: Boolean) {
        prefs(context).edit()
            .putBoolean(KEY_SMART_ENABLED, enabled)
            .putBoolean("auto_pump_enabled", false)
            .apply()
    }

    fun setEnabled(context: Context, enabled: Boolean): Boolean {
        saveEnabled(context, enabled)
        val value = if (enabled) "1" else "0"
        return RootShell.exec("""
            if [ -e $PROC_PATH ]; then echo $value > $PROC_PATH; fi
            settings put system $SETTINGS_KEY $value
        """.trimIndent())
    }

    fun readStatus(): String {
        val proc = RootShell.execForOutput("cat $PROC_PATH 2>/dev/null")?.trim() ?: "?"
        val setting = RootShell.execForOutput("settings get system $SETTINGS_KEY 2>/dev/null")?.trim() ?: "?"
        return "Proc: $proc • Setting: $setting"
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences(PrefsKeys.HW_PREFS, Context.MODE_PRIVATE)
}
