package com.elitedarkkaiser.redmagic

import android.content.Context
import org.json.JSONObject

private const val TOUCH_TRIGGER_PREFS = "trigger_touch_profiles"
private const val ACTIVE_TOUCH_PACKAGE_KEY = "active_touch_package"

fun saveActiveTriggerTouchPackageStorage(context: Context, packageName: String) {
    context.getSharedPreferences(TOUCH_TRIGGER_PREFS, Context.MODE_PRIVATE)
        .edit()
        .putString(ACTIVE_TOUCH_PACKAGE_KEY, packageName)
        .apply()
}

fun activeTriggerTouchPackageStorage(context: Context): String? {
    return context.getSharedPreferences(TOUCH_TRIGGER_PREFS, Context.MODE_PRIVATE)
        .getString(ACTIVE_TOUCH_PACKAGE_KEY, null)
}

data class TriggerTouchPoint(
    val x: Int,
    val y: Int
)

data class TriggerTouchProfile(
    val packageName: String,
    val leftPoint: TriggerTouchPoint?,
    val rightPoint: TriggerTouchPoint?
)

fun loadTriggerTouchProfileStorage(context: Context, packageName: String): TriggerTouchProfile {
    val prefs = context.getSharedPreferences(TOUCH_TRIGGER_PREFS, Context.MODE_PRIVATE)
    val raw = prefs.getString(packageName, null) ?: return TriggerTouchProfile(
        packageName = packageName,
        leftPoint = null,
        rightPoint = null
    )

    val obj = JSONObject(raw)
    return TriggerTouchProfile(
        packageName = packageName,
        leftPoint = obj.optJSONObject("left")?.toTouchPoint(),
        rightPoint = obj.optJSONObject("right")?.toTouchPoint()
    )
}

fun saveTriggerTouchProfileStorage(context: Context, profile: TriggerTouchProfile) {
    val obj = JSONObject().apply {
        profile.leftPoint?.let { put("left", it.toJson()) }
        profile.rightPoint?.let { put("right", it.toJson()) }
    }

    context.getSharedPreferences(TOUCH_TRIGGER_PREFS, Context.MODE_PRIVATE)
        .edit()
        .putString(profile.packageName, obj.toString())
        .apply()
}

fun triggerTouchProfileSummaryStorage(context: Context, packageName: String): String {
    val profile = loadTriggerTouchProfileStorage(context, packageName)
    val left = if (profile.leftPoint != null) "Left set" else "Left unset"
    val right = if (profile.rightPoint != null) "Right set" else "Right unset"
    return "$left • $right"
}

private fun TriggerTouchPoint.toJson(): JSONObject {
    return JSONObject().apply {
        put("x", x)
        put("y", y)
    }
}

private fun JSONObject.toTouchPoint(): TriggerTouchPoint {
    return TriggerTouchPoint(
        x = optInt("x", -1),
        y = optInt("y", -1)
    )
}
