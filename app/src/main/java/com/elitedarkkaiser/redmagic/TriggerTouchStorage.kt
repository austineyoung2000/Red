package com.elitedarkkaiser.redmagic

import android.content.Context

object TriggerTouchStorage {
    fun save(context: Context, side: String, x: Int, y: Int) {
        context.getSharedPreferences("trigger_touch", Context.MODE_PRIVATE)
            .edit()
            .putInt("${side}_x", x)
            .putInt("${side}_y", y)
            .apply()
    }

    fun load(context: Context, side: String): Pair<Int, Int>? {
        val prefs = context.getSharedPreferences("trigger_touch", Context.MODE_PRIVATE)
        val x = prefs.getInt("${side}_x", -1)
        val y = prefs.getInt("${side}_y", -1)
        return if (x >= 0 && y >= 0) Pair(x, y) else null
    }
}
