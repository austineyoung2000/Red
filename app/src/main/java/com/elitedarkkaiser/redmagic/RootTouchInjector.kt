package com.elitedarkkaiser.redmagic

import android.content.res.Resources
import android.util.Log
import java.util.concurrent.atomic.AtomicInteger

object RootTouchInjector {
    private const val TOUCH_DEVICE = "/dev/input/event9"

    private const val EV_SYN = 0
    private const val EV_KEY = 1
    private const val EV_ABS = 3

    private const val SYN_REPORT = 0
    private const val BTN_TOUCH = 330

    private const val ABS_MT_SLOT = 47
    private const val ABS_MT_TOUCH_MAJOR = 48
    private const val ABS_MT_POSITION_X = 53
    private const val ABS_MT_POSITION_Y = 54
    private const val ABS_MT_TRACKING_ID = 57

    private const val MAX_X = 12159
    private const val MAX_Y = 26879

    private val nextTrackingId = AtomicInteger(2000)

    private fun displayWidth(): Int {
        val dm = Resources.getSystem().displayMetrics
        return maxOf(dm.widthPixels, dm.heightPixels).coerceAtLeast(1)
    }

    private fun displayHeight(): Int {
        val dm = Resources.getSystem().displayMetrics
        return minOf(dm.widthPixels, dm.heightPixels).coerceAtLeast(1)
    }

    private fun scaleX(x: Int, y: Int): Int {
        val screenH = displayHeight()
        return ((y.toFloat() / screenH.toFloat()) * MAX_X).toInt().coerceIn(0, MAX_X)
    }

    private fun scaleY(x: Int, y: Int): Int {
        val screenW = displayWidth()
        return (((screenW - x).toFloat() / screenW.toFloat()) * MAX_Y).toInt().coerceIn(0, MAX_Y)
    }

    fun down(slot: Int, x: Int, y: Int): Boolean {
        val safeSlot = slot.coerceIn(0, 9)
        val id = nextTrackingId.incrementAndGet()

        return runEvents(
            event(EV_ABS, ABS_MT_SLOT, safeSlot),
            event(EV_ABS, ABS_MT_TRACKING_ID, id),
            event(EV_KEY, BTN_TOUCH, 1),
            event(EV_ABS, ABS_MT_POSITION_X, scaleX(x, y)),
            event(EV_ABS, ABS_MT_POSITION_Y, scaleY(x, y)),
            event(EV_ABS, ABS_MT_TOUCH_MAJOR, 10),
            event(EV_SYN, SYN_REPORT, 0)
        )
    }

    fun up(slot: Int): Boolean {
        val safeSlot = slot.coerceIn(0, 9)

        return runEvents(
            event(EV_ABS, ABS_MT_SLOT, safeSlot),
            event(EV_ABS, ABS_MT_TRACKING_ID, -1),
            event(EV_SYN, SYN_REPORT, 0)
        )
    }

    fun tap(slot: Int, x: Int, y: Int, durationMs: Long = 45L): Boolean {
        val downOk = down(slot, x, y)
        Thread.sleep(durationMs.coerceAtLeast(20L))
        val upOk = up(slot)
        return downOk && upOk
    }

    private fun event(type: Int, code: Int, value: Int): String {
        return "sendevent $TOUCH_DEVICE $type $code $value"
    }

    private fun runEvents(vararg events: String): Boolean {
        return try {
            val command = events.joinToString("; ")
            Log.d("RootTouchInjector", command)
            Runtime.getRuntime().exec(arrayOf("su", "-c", command)).waitFor() == 0
        } catch (t: Throwable) {
            Log.e("RootTouchInjector", "touch injection failed", t)
            false
        }
    }
}
