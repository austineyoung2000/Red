package com.elitedarkkaiser.redmagic

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent

class TriggerAccessibilityService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit
    override fun onInterrupt() = Unit

    override fun onServiceConnected() {
        super.onServiceConnected()
        HardwareController.enableTriggers()
    }

    private fun prefs() = getSharedPreferences(PrefsKeys.TRIGGER_PREFS, Context.MODE_PRIVATE)

    private fun getAction(key: String): String {
        return prefs().getString(key, "NONE") ?: "NONE"
    }

    private fun runRoot(cmd: String) {
        try {
            Runtime.getRuntime().exec(arrayOf("su", "-c", cmd))
        } catch (_: Throwable) {
        }
    }

    private fun performAction(action: String) {
        when (action) {
            "VOL_UP" -> runRoot("input keyevent 24")
            "VOL_DOWN" -> runRoot("input keyevent 25")
            "PLAY_PAUSE" -> runRoot("input keyevent 85")
            "NONE" -> Unit
            else -> Unit
        }
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        if (event.action != KeyEvent.ACTION_DOWN) return false

        return when (event.keyCode) {
            KeyEvent.KEYCODE_F7 -> {
                performAction(getAction("left_trigger"))
                true
            }

            KeyEvent.KEYCODE_F8 -> {
                performAction(getAction("right_trigger"))
                true
            }

            else -> false
        }
    }
}
