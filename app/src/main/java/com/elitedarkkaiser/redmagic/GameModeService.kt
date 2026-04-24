package com.elitedarkkaiser.redmagic

import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper

class GameModeService : Service() {

    private val handler = Handler(Looper.getMainLooper())
    private var gameModeActiveFor: String? = null

    private val pollRunnable = object : Runnable {
        override fun run() {
            try {
                val currentPkg = getForegroundPackageName()
                val prefs = getSharedPreferences(PrefsKeys.HW_PREFS, Context.MODE_PRIVATE)
                val tracked = prefs.getStringSet("game_mode_packages", emptySet()) ?: emptySet()

                if (!currentPkg.isNullOrBlank() && tracked.contains(currentPkg)) {
                    val callModeOwnsHardware = prefs.getBoolean(PrefsKeys.CALL_LED_OWNER, false)
                    val forceGameReapply = prefs.getBoolean("force_game_mode_reapply", false)

                    val enteringGame = gameModeActiveFor != currentPkg

                    if (enteringGame) {
                        gameModeActiveFor = currentPkg
                        prefs.edit()
                            .putBoolean(PrefsKeys.GAME_LED_OWNER, true)
                            .apply()
                        stopService(Intent(this@GameModeService, FanLedService::class.java))
                    }

                    if (!callModeOwnsHardware && (enteringGame || forceGameReapply)) {
                        prefs.edit().putBoolean("force_game_mode_reapply", false).apply()
                        applyGameModeProfile()
                    }
                } else if (!currentPkg.isNullOrBlank()) {
                    if (gameModeActiveFor != null) {
                        getSharedPreferences(PrefsKeys.HW_PREFS, Context.MODE_PRIVATE)
                            .edit()
                            .putBoolean(PrefsKeys.GAME_LED_OWNER, false)
                            .apply()
                        restoreNormalProfile()
                        gameModeActiveFor = null
                    }
                }
            } catch (_: Throwable) {
            } finally {
                handler.postDelayed(this, 1500L)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        handler.post(pollRunnable)
    }

    override fun onDestroy() {
        handler.removeCallbacks(pollRunnable)
        if (gameModeActiveFor != null) {
            getSharedPreferences(PrefsKeys.HW_PREFS, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(PrefsKeys.GAME_LED_OWNER, false)
                .apply()
            restoreNormalProfile()
            gameModeActiveFor = null
        }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun getForegroundPackageName(): String? {
        val usm = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val end = System.currentTimeMillis()
        val start = end - 15_000L

        val events = usm.queryEvents(start, end)
        val event = UsageEvents.Event()
        var lastForeground: String? = null

        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (
                event.eventType == UsageEvents.Event.ACTIVITY_RESUMED ||
                event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND
            ) {
                val pkg = event.packageName
                if (!pkg.isNullOrBlank() && !shouldIgnorePackage(pkg)) {
                    lastForeground = pkg
                }
            }
        }

        return lastForeground
    }

    private fun shouldIgnorePackage(pkg: String): Boolean {
        if (pkg == packageName) return true
        if (pkg == "com.android.systemui") return true
        return false
    }

    

    private fun getProfileForPackage(pkg: String): Map<String, Any> {
        val prefs = getSharedPreferences(PrefsKeys.HW_PREFS, Context.MODE_PRIVATE)
        val json = prefs.getString("game_profile_$pkg", null) ?: return emptyMap()

        return try {
            val obj = org.json.JSONObject(json)
            mapOf(
                "fanEnabled" to obj.optBoolean("fanEnabled", true),
                "fanLevel" to obj.optInt("fanLevel", 3),
                "fanLedEnabled" to obj.optBoolean("fanLedEnabled", true),
                "fanLedEffect" to obj.optString("fanLedEffect", "steady"),
                "fanLedColor" to obj.optInt("fanLedColor", 5),
                "fanLedModeType" to obj.optString("fanLedModeType", "basic"),
                "fanLedPresetValue" to obj.optString("fanLedPresetValue", ""),
                "logoLedEnabled" to obj.optBoolean("logoLedEnabled", true),
                "logoLedEffect" to obj.optString("logoLedEffect", "steady"),
                "logoLedColor" to obj.optInt("logoLedColor", 1),
                "shoulderLedEnabled" to obj.optBoolean("shoulderLedEnabled", true),
                "shoulderLedEffect" to obj.optString("shoulderLedEffect", "breathe"),
                "shoulderLedColor" to obj.optInt("shoulderLedColor", 8)
            )
        } catch (_: Throwable) {
            emptyMap()
        }
    }
    private fun applyGameModeProfile() {
        val prefs = getSharedPreferences(PrefsKeys.HW_PREFS, Context.MODE_PRIVATE)

        val pkg = gameModeActiveFor ?: return
        val profile = getProfileForPackage(pkg)
        val modeProfile = GameModeProfileBuilder.build(prefs, profile)

        fun applyOnce(reason: String) {
            if (gameModeActiveFor != pkg) return

            ModeHardwareApplier.apply(modeProfile)

            android.util.Log.i(
                "RedmagicGameMode",
                "apply[$reason] pkg=$pkg fan=${modeProfile.fanLedEnabled}/${modeProfile.fanLedEffect}/${modeProfile.fanLedColor} logo=${modeProfile.logoLedEnabled}/${modeProfile.logoLedEffect}/${modeProfile.logoLedColor} shoulder=${modeProfile.shoulderLedEnabled}/${modeProfile.shoulderLedEffect}/${modeProfile.shoulderLedColor}"
            )
        }

        applyOnce("now")
        handler.postDelayed({ applyOnce("750ms") }, 750L)
    }
    private fun restoreNormalProfile() {
        NormalLedRestoreRunner.restore(this)

        android.util.Log.i("RedmagicNormalRestore", "restored normal profile")
    }
}
