package com.elitedarkkaiser.redmagic

import android.app.Service
import android.content.Intent
import android.os.IBinder
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

class TriggerRootService : Service() {

    
    private var rightTriggerUnlockedUntil: Long = 0L
    private var running = true
    private val held = ConcurrentHashMap<String, AtomicBoolean>()
    private val repeatThreads = ConcurrentHashMap<String, Thread>()
    private val lastPulseAt = ConcurrentHashMap<String, Long>()

    private var rightUnlockArmedAt = 0L
    private var rightUnlockedUntil = 0L

    private val RIGHT_UNLOCK_TAP_WINDOW_MS = 450L
    private val RIGHT_UNLOCK_ACTIVE_MS = 2500L
    private val HOLD_REPEAT_START_MS = 350L
    private val HOLD_REPEAT_INTERVAL_MS = 110L
    private val RAPID_FIRE_INTERVAL_MS = 75L
    private val VIRTUAL_HOLD_TIMEOUT_MS = 1200L

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        
        HardwareController.enableTriggers()
        android.util.Log.d("TRIGGER", "TriggerRootService onCreate")
        startReader("/dev/input/event4", "left_trigger")
        startReader("/dev/input/event5", "right_trigger")
    }

    private fun prefs() = getSharedPreferences("triggers", MODE_PRIVATE)

    private fun isScreenInteractive(): Boolean {
        val powerManager = getSystemService(POWER_SERVICE) as android.os.PowerManager
        return powerManager.isInteractive
    }


    private fun getAction(key: String): String {
        val value = prefs().getString(key, "NONE") ?: "NONE"
        android.util.Log.d("TRIGGER", "getAction(" + key + ")=" + value)
        return value
    }

    private fun hapticsEnabled(): Boolean {
        return prefs().getBoolean("haptics_enabled", true)
    }

    private fun runRoot(cmd: String) {
        try {
            android.util.Log.d("TRIGGER", "runRoot=" + cmd)
            Runtime.getRuntime().exec(arrayOf("su", "-c", cmd))
        } catch (t: Throwable) {
            android.util.Log.e("TRIGGER", "runRoot failed: " + t)
        }
    }

    private fun hapticTap() {
        if (!hapticsEnabled()) return
        try {
            HardwareController.vibrate(durationMs = 20, gain = 180)
        } catch (t: Throwable) {
            android.util.Log.e("TRIGGER", "hapticTap failed: " + t)
        }
    }

    private fun hapticUnlock() {
        if (!hapticsEnabled()) return
        try {
            HardwareController.vibrate(durationMs = 40, gain = 255)
        } catch (t: Throwable) {
            android.util.Log.e("TRIGGER", "hapticUnlock failed: " + t)
        }
    }

    private fun hapticHoldStart() {
        if (!hapticsEnabled()) return
        try {
            HardwareController.vibrate(durationMs = 35, gain = 255)
        } catch (t: Throwable) {
            android.util.Log.e("TRIGGER", "hapticHoldStart failed: " + t)
        }
    }

    private fun performAction(action: String) {
        android.util.Log.d("TRIGGER", "performAction=" + action)

        when (action) {
            "VOL_UP" -> runRoot("input keyevent 24")
            "VOL_DOWN" -> runRoot("input keyevent 25")
            "MEDIA_PLAY_PAUSE" -> runRoot("input keyevent 85")
            "MEDIA_NEXT" -> runRoot("input keyevent 87")
            "MEDIA_PREVIOUS" -> runRoot("input keyevent 88")
            "TOUCH_LEFT_POINT" -> tapTouchPoint("left")
            "TOUCH_RIGHT_POINT" -> tapTouchPoint("right")
            "HOLD_LEFT_POINT" -> startHoldTouchPoint(if (action.contains("LEFT")) "left_trigger" else "right_trigger", "left")
            "HOLD_RIGHT_POINT" -> startHoldTouchPoint(if (action.contains("LEFT")) "left_trigger" else "right_trigger", "right")
            "REPEAT_LEFT_POINT" -> startRapidTouchPoint(if (action.contains("LEFT")) "left_trigger" else "right_trigger", "left")
            "REPEAT_RIGHT_POINT" -> startRapidTouchPoint(if (action.contains("LEFT")) "left_trigger" else "right_trigger", "right")
            "NONE" -> Unit
            else -> Unit
        }
    }


    private fun currentForegroundPackage(): String? {
        return try {
            val usageStatsManager = getSystemService(USAGE_STATS_SERVICE) as android.app.usage.UsageStatsManager
            val current = now()
            usageStatsManager.queryUsageStats(
                android.app.usage.UsageStatsManager.INTERVAL_DAILY,
                current - 15000L,
                current
            )
                ?.filter { it.packageName != packageName }
                ?.maxByOrNull { it.lastTimeUsed }
                ?.packageName
        } catch (t: Throwable) {
            android.util.Log.e("TRIGGER", "currentForegroundPackage failed: $t")
            null
        }
    }

    private fun loadPoint(side: String): TriggerTouchPoint? {
        val activePkg = activeTriggerTouchPackageStorage(this)
        val foregroundPkg = currentForegroundPackage()
        val pkg = activePkg ?: foregroundPkg ?: return null
        val profile = loadTriggerTouchProfileStorage(this, pkg)
        return (if (side == "left") profile.leftPoint else profile.rightPoint)
            ?.takeIf { it.x >= 0 && it.y >= 0 }
    }

    private fun slotFor(side: String): Int = if (side == "left") 8 else 9

    private fun tapTouchPoint(side: String) {
        val point = loadPoint(side) ?: return
        RootTouchInjector.tap(slotFor(side), point.x, point.y, 45L)
    }

    private fun startHoldTouchPoint(prefKey: String, side: String) {
        val point = loadPoint(side) ?: return
        lastPulseAt[prefKey] = now()
        if (repeatThreads[prefKey]?.isAlive == true) return

        val flag = AtomicBoolean(true)
        held[prefKey] = flag

        val thread = Thread {
            try {
                hapticHoldStart()
                RootTouchInjector.down(slotFor(side), point.x, point.y)
                while (running && flag.get()) {
                    if (now() - (lastPulseAt[prefKey] ?: 0L) > VIRTUAL_HOLD_TIMEOUT_MS) break
                    Thread.sleep(60L)
                }
            } catch (_: InterruptedException) {
            } finally {
                RootTouchInjector.up(slotFor(side))
                held.remove(prefKey)
                repeatThreads.remove(prefKey)
                lastPulseAt.remove(prefKey)
            }
        }

        repeatThreads[prefKey] = thread
        thread.start()
    }

    private fun startRapidTouchPoint(prefKey: String, side: String) {
        val point = loadPoint(side) ?: return
        lastPulseAt[prefKey] = now()
        if (repeatThreads[prefKey]?.isAlive == true) return

        val flag = AtomicBoolean(true)
        held[prefKey] = flag

        val thread = Thread {
            try {
                hapticHoldStart()
                while (running && flag.get()) {
                    if (now() - (lastPulseAt[prefKey] ?: 0L) > VIRTUAL_HOLD_TIMEOUT_MS) break
                    RootTouchInjector.tap(slotFor(side), point.x, point.y, 35L)
                    Thread.sleep(RAPID_FIRE_INTERVAL_MS)
                }
            } catch (_: InterruptedException) {
            } finally {
                held.remove(prefKey)
                repeatThreads.remove(prefKey)
                lastPulseAt.remove(prefKey)
            }
        }

        repeatThreads[prefKey] = thread
        thread.start()
    }

    private fun isTouchHoldOrRapid(action: String): Boolean {
        return action == "HOLD_LEFT_POINT" ||
            action == "HOLD_RIGHT_POINT" ||
            action == "REPEAT_LEFT_POINT" ||
            action == "REPEAT_RIGHT_POINT"
    }

    private fun isRepeatable(action: String): Boolean {
        return when (action) {
            "VOL_UP", "VOL_DOWN" -> true
            else -> false
        }
    }

    private fun startRepeater(prefKey: String) {
        stopRepeater(prefKey)

        val action = getAction(prefKey)
        if (!isRepeatable(action)) return

        val flag = AtomicBoolean(true)
        held[prefKey] = flag

        val thread = Thread {
            try {
                Thread.sleep(HOLD_REPEAT_START_MS)

                if (running && flag.get()) {
                    hapticHoldStart()
                }

                while (running && flag.get()) {
                    if (prefKey == "right_trigger" && !isRightUnlocked()) {
                        break
                    }
                    performAction(getAction(prefKey))
                    if (prefKey == "right_trigger") {
                        extendRightUnlock()
                    }
                    Thread.sleep(HOLD_REPEAT_INTERVAL_MS)
                }
            } catch (_: InterruptedException) {
            } catch (t: Throwable) {
                android.util.Log.e("TRIGGER", "startRepeater failed for " + prefKey + ": " + t)
            }
        }

        repeatThreads[prefKey] = thread
        thread.start()
    }

    private fun stopRepeater(prefKey: String) {
        held[prefKey]?.set(false)
        held.remove(prefKey)

        repeatThreads[prefKey]?.interrupt()
        repeatThreads.remove(prefKey)
    }

    private fun now() = System.currentTimeMillis()

    private fun isRightUnlocked(): Boolean {
        val unlocked = now() <= rightUnlockedUntil
        if (!unlocked && rightUnlockedUntil != 0L) {
            android.util.Log.d("TRIGGER", "right trigger locked by timeout")
        }
        return unlocked
    }

    private fun extendRightUnlock() {
        rightUnlockedUntil = now() + RIGHT_UNLOCK_ACTIVE_MS
        android.util.Log.d("TRIGGER", "right unlock extended until=" + rightUnlockedUntil)
    }

    private fun handleRightIntentUnlock(): Boolean {
        if (!prefs().getBoolean("intent_unlock_right_trigger", true)) {
            return true
        }

        val current = now()

        if (current <= rightUnlockedUntil) {
            extendRightUnlock()
            return true
        }

        if (rightUnlockArmedAt != 0L && (current - rightUnlockArmedAt) <= RIGHT_UNLOCK_TAP_WINDOW_MS) {
            rightUnlockArmedAt = 0L
            rightUnlockedUntil = current + RIGHT_UNLOCK_ACTIVE_MS
            android.util.Log.d("TRIGGER", "right trigger UNLOCKED")
            hapticUnlock()
            return true
        }

        rightUnlockArmedAt = current
        android.util.Log.d("TRIGGER", "right trigger first tap ignored, armedAt=" + rightUnlockArmedAt)
        return false
    }

    private fun handleLeftDown(device: String, line: String) {
        android.util.Log.d("TRIGGER", "LEFT DOWN device=" + device + " line=" + line)

        if (!isScreenInteractive()) {
            android.util.Log.d("TRIGGER", "LEFT ignored because screen is off")
            return
        }

        hapticTap()
        rightTriggerUnlockedUntil = System.currentTimeMillis() + 1000L
        android.util.Log.d("TRIGGER", "LEFT unlocked right trigger until=" + rightTriggerUnlockedUntil)
        val action = getAction("left_trigger")
        performAction(action)
        if (!isTouchHoldOrRapid(action)) startRepeater("left_trigger")
    }

    private fun handleRightDown(device: String, line: String) {
        android.util.Log.d("TRIGGER", "RIGHT DOWN device=" + device + " line=" + line)

        if (!isScreenInteractive()) {
            android.util.Log.d("TRIGGER", "RIGHT ignored because screen is off")
            return
        }

        if (System.currentTimeMillis() <= rightTriggerUnlockedUntil) {
            rightTriggerUnlockedUntil = 0L
            hapticTap()
            val action = getAction("right_trigger")
            performAction(action)
            if (!isTouchHoldOrRapid(action)) startRepeater("right_trigger")
            return
        }

        if (!handleRightIntentUnlock()) {
            stopRepeater("right_trigger")
            return
        }

        hapticTap()
        val action = getAction("right_trigger")
        performAction(action)
        if (!isTouchHoldOrRapid(action)) startRepeater("right_trigger")
    }

    private fun handleUp(prefKey: String, device: String, line: String) {
        android.util.Log.d("TRIGGER", "UP device=" + device + " key=" + prefKey + " line=" + line)

        val action = getAction(prefKey)
        if (isTouchHoldOrRapid(action)) {
            lastPulseAt[prefKey] = now()
            return
        }

        stopRepeater(prefKey)
    }

    private fun startReader(device: String, prefKey: String) {
        Thread {
            try {
                android.util.Log.d("TRIGGER", "startReader device=" + device + " key=" + prefKey)
                val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "getevent -l " + device))
                val reader = BufferedReader(InputStreamReader(process.inputStream))

                while (running) {
                    val line = reader.readLine() ?: break

                    if (line.contains(" DOWN")) {
                        if (prefKey == "left_trigger") {
                            handleLeftDown(device, line)
                        } else {
                            handleRightDown(device, line)
                        }
                    } else if (line.contains(" UP")) {
                        handleUp(prefKey, device, line)
                    }
                }
            } catch (t: Throwable) {
                android.util.Log.e("TRIGGER", "startReader failed for " + device + ": " + t)
            }
        }.start()
    }

    override fun onDestroy() {
        running = false
        stopRepeater("left_trigger")
        stopRepeater("right_trigger")
        android.util.Log.d("TRIGGER", "TriggerRootService onDestroy")
        super.onDestroy()
    }
}
