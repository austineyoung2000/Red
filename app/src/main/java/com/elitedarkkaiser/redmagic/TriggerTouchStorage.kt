package com.elitedarkkaiser.redmagic

import android.content.Context

object TriggerTouchStorage {
    private const val PREFS = "trigger_touch"
    private const val ACTIVE_PACKAGE_KEY = "active_trigger_touch_package"

    private fun key(pkg: String, side: String, axis: String): String {
        return "${pkg}_${side}_${axis}"
    }

    private fun confirmedKey(pkg: String): String {
        return "${pkg}_confirmed"
    }

    fun save(context: Context, pkg: String, side: String, x: Int, y: Int) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putInt(key(pkg, side, "x"), x)
            .putInt(key(pkg, side, "y"), y)
            .apply()
    }

    fun markConfirmed(context: Context, pkg: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(confirmedKey(pkg), true)
            .apply()
    }

    fun isConfirmed(context: Context, pkg: String): Boolean {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(confirmedKey(pkg), false)
    }

    fun load(context: Context, pkg: String, side: String): Pair<Int, Int>? {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val x = prefs.getInt(key(pkg, side, "x"), -1)
        val y = prefs.getInt(key(pkg, side, "y"), -1)
        return if (x >= 0 && y >= 0) Pair(x, y) else null
    }

    fun setActivePackage(context: Context, pkg: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(ACTIVE_PACKAGE_KEY, pkg)
            .apply()
    }

    fun activePackage(context: Context): String? {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(ACTIVE_PACKAGE_KEY, null)
    }

    fun hasBothPoints(context: Context, pkg: String): Boolean {
        return load(context, pkg, "left") != null && load(context, pkg, "right") != null
    }
}
