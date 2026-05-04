package com.elitedarkkaiser.redmagic

import android.content.Context

object TriggerTouchStorage {

    private fun key(pkg: String, side: String, axis: String) =
        "${pkg}_${side}_${axis}"

    fun save(context: Context, pkg: String, side: String, x: Int, y: Int) {
        context.getSharedPreferences("trigger_touch", Context.MODE_PRIVATE)
            .edit()
            .putInt(key(pkg, side, "x"), x)
            .putInt(key(pkg, side, "y"), y)
            .apply()
    }

    fun load(context: Context, pkg: String, side: String): Pair<Int, Int>? {
        val prefs = context.getSharedPreferences("trigger_touch", Context.MODE_PRIVATE)
        val x = prefs.getInt(key(pkg, side, "x"), -1)
        val y = prefs.getInt(key(pkg, side, "y"), -1)
        return if (x >= 0 and y >= 0) Pair(x, y) else null
    }
}
