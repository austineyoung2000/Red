package com.elitedarkkaiser.redmagic

import android.content.Context

object MicroPumpController {
    const val KEY_ENABLED = "micro_pump_enabled"
    const val KEY_SMART_ENABLED = "micro_pump_smart_enabled"
    const val KEY_FORCE_UNTIL = "micro_pump_force_until"
    const val KEY_ON_TEMP = "micro_pump_on_temp"
    const val KEY_OFF_TEMP = "micro_pump_off_temp"
    const val KEY_RUNTIME_MODE = "micro_pump_runtime_mode"
    const val KEY_RUNTIME_TEMP = "micro_pump_runtime_temp"
    const val KEY_RUNTIME_ACTIVE = "micro_pump_runtime_active"

    private const val PROC_PATH = "/proc/driver/micropump/enable"
    private const val SETTINGS_KEY = "liquid_cooling_off_on"

    fun isEnabledSaved(context: Context): Boolean =
        prefs(context).getBoolean(KEY_ENABLED, false)

    fun isSmartSaved(context: Context): Boolean =
        prefs(context).getBoolean(KEY_SMART_ENABLED, false)

    fun saveEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit()
            .putBoolean(KEY_ENABLED, enabled)
            .apply()
    }

    fun saveSmart(context: Context, enabled: Boolean) {
        prefs(context).edit()
            .putBoolean(KEY_SMART_ENABLED, enabled)
            .apply()
    }

    fun forceUntil(context: Context): Long =
        prefs(context).getLong(KEY_FORCE_UNTIL, 0L)

    fun isForceActive(context: Context): Boolean =
        System.currentTimeMillis() < forceUntil(context)

    fun saveForceUntil(context: Context, untilMs: Long) {
        prefs(context).edit()
            .putLong(KEY_FORCE_UNTIL, untilMs)
            .apply()
    }


    fun onTemp(context: Context): Float =
        prefs(context).getFloat(KEY_ON_TEMP, 95f)

    fun offTemp(context: Context): Float =
        prefs(context).getFloat(KEY_OFF_TEMP, 88f)

    fun saveOnTemp(context: Context, value: Float) {
        prefs(context).edit()
            .putFloat(KEY_ON_TEMP, value)
            .apply()
    }

    fun saveOffTemp(context: Context, value: Float) {
        prefs(context).edit()
            .putFloat(KEY_OFF_TEMP, value)
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

    fun saveRuntimeState(context: Context, mode: String, tempF: Float?, active: Boolean) {
        prefs(context).edit()
            .putString(KEY_RUNTIME_MODE, mode)
            .putFloat(KEY_RUNTIME_TEMP, tempF ?: -1f)
            .putBoolean(KEY_RUNTIME_ACTIVE, active)
            .apply()
    }

    fun runtimeSummary(context: Context): String {
        val p = prefs(context)
        val mode = p.getString(KEY_RUNTIME_MODE, "Idle") ?: "Idle"
        val temp = p.getFloat(KEY_RUNTIME_TEMP, -1f)
        val active = p.getBoolean(KEY_RUNTIME_ACTIVE, false)

        val tempText = if (temp >= 0f) "${temp.toInt()}°F" else "--"
        return "Auto state: $mode • Pump: ${if (active) "ON" else "OFF"} • Temp: $tempText"
    }

    fun diagnostics(): String {
        val exists = RootShell.execForOutput("if [ -e $PROC_PATH ]; then echo yes; else echo no; fi 2>/dev/null")?.trim() ?: "?"
        val writable = RootShell.execForOutput("if [ -w $PROC_PATH ]; then echo yes; else echo no; fi 2>/dev/null")?.trim() ?: "?"
        val proc = RootShell.execForOutput("cat $PROC_PATH 2>/dev/null")?.trim() ?: "?"
        val setting = RootShell.execForOutput("settings get system $SETTINGS_KEY 2>/dev/null")?.trim() ?: "?"
        val root = if (RootShell.hasRoot()) "yes" else "no"

        return "Root: $root • Path exists: $exists • Writable: $writable • Proc: $proc • Setting: $setting"
    }

    fun readStatus(): String {
        val proc = RootShell.execForOutput("cat $PROC_PATH 2>/dev/null")?.trim() ?: "?"
        val setting = RootShell.execForOutput("settings get system $SETTINGS_KEY 2>/dev/null")?.trim() ?: "?"
        return "Proc: $proc • Setting: $setting"
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences(PrefsKeys.HW_PREFS, Context.MODE_PRIVATE)
}
