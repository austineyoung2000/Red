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
                val prefs = getSharedPreferences("redmagic_hw_controls_prefs", Context.MODE_PRIVATE)
                val tracked = prefs.getStringSet("game_mode_packages", emptySet()) ?: emptySet()

                if (!currentPkg.isNullOrBlank() && tracked.contains(currentPkg)) {
                    if (gameModeActiveFor != currentPkg) {
                        gameModeActiveFor = currentPkg
                        getSharedPreferences("redmagic_hw_controls_prefs", Context.MODE_PRIVATE)
                            .edit()
                            .putBoolean("game_mode_led_override_active", true)
                            .apply()
                        applyGameModeProfile()
                    }
                } else if (!currentPkg.isNullOrBlank()) {
                    if (gameModeActiveFor != null) {
                        restoreNormalProfile()
                        getSharedPreferences("redmagic_hw_controls_prefs", Context.MODE_PRIVATE)
                            .edit()
                            .putBoolean("game_mode_led_override_active", false)
                            .apply()
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
            restoreNormalProfile()
            getSharedPreferences("redmagic_hw_controls_prefs", Context.MODE_PRIVATE)
                .edit()
                .putBoolean("game_mode_led_override_active", false)
                .apply()
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
        val prefs = getSharedPreferences("redmagic_hw_controls_prefs", Context.MODE_PRIVATE)
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
        val prefs = getSharedPreferences("redmagic_hw_controls_prefs", Context.MODE_PRIVATE)

        val pkg = gameModeActiveFor ?: return
        val profile = getProfileForPackage(pkg)

        val fanEnabled = profile["fanEnabled"] as? Boolean ?: prefs.getBoolean("game_mode_fan_enabled", true)
        val fanLevel = profile["fanLevel"] as? Int ?: prefs.getInt("game_mode_fan_level", 3)
        val pumpEnabled = prefs.getBoolean("game_mode_pump_enabled", false)
        val pumpProfile = prefs.getString("game_mode_pump_profile", "quick") ?: "quick"

        val fanLedEnabled = profile["fanLedEnabled"] as? Boolean ?: prefs.getBoolean("game_mode_fan_led_enabled", true)
        val fanLedEffect = profile["fanLedEffect"] as? String ?: prefs.getString("game_mode_fan_led_effect", "steady") ?: "steady"
        val fanLedColor = profile["fanLedColor"] as? Int ?: prefs.getInt("game_mode_fan_led_color", 5)
        val fanLedModeType = profile["fanLedModeType"] as? String ?: "basic"
        val fanLedPresetValue = profile["fanLedPresetValue"] as? String ?: ""

        val logoLedEnabled = profile["logoLedEnabled"] as? Boolean ?: prefs.getBoolean("game_mode_logo_led_enabled", true)
        val logoLedEffect = profile["logoLedEffect"] as? String ?: prefs.getString("game_mode_logo_led_effect", "steady") ?: "steady"
        val logoLedColor = profile["logoLedColor"] as? Int ?: prefs.getInt("game_mode_logo_led_color", 1)

        val shoulderLedEnabled = profile["shoulderLedEnabled"] as? Boolean ?: prefs.getBoolean("game_mode_shoulder_led_enabled", true)
        val shoulderLedEffect = profile["shoulderLedEffect"] as? String ?: prefs.getString("game_mode_shoulder_led_effect", "breathe") ?: "breathe"
        val shoulderLedColor = profile["shoulderLedColor"] as? Int ?: prefs.getInt("game_mode_shoulder_led_color", 8)

        fun applyOnce(reason: String) {
            if (gameModeActiveFor != pkg) return

            if (fanEnabled) {
                HardwareController.setFanLevel(fanLevel)
            } else {
                HardwareController.enableFan(false)
            }

            if (pumpEnabled) {
                HardwareController.setPumpProfile(pumpProfile)
            } else {
                HardwareController.enablePump(false)
            }

            if (fanLedEnabled) {
                HardwareController.setFanLedEnabled(true)
                if (fanLedModeType == "preset" && fanLedPresetValue.isNotBlank()) {
                    HardwareController.setFanLedStockPreset(fanLedPresetValue)
                } else if (fanLedEffect.startsWith("preset:")) {
                    HardwareController.setFanLedStockPreset(fanLedEffect.removePrefix("preset:"))
                } else {
                    HardwareController.setFanLedEffect(fanLedEffect, fanLedColor)
                }
            } else {
                HardwareController.setFanLedEnabled(false)
            }

            if (logoLedEnabled) {
                HardwareController.setLogoLedEnabled(true)
                HardwareController.setLogoLedEffect(logoLedEffect, logoLedColor)
            } else {
                HardwareController.setLogoLedEnabled(false)
            }

            if (shoulderLedEnabled) {
                HardwareController.setShoulderLedEnabled(true)
                HardwareController.setShoulderLedEffect(shoulderLedEffect, shoulderLedColor)
            } else {
                HardwareController.setShoulderLedEnabled(false)
            }

            android.util.Log.i(
                "RedmagicGameMode",
                "apply[$reason] pkg=$pkg fan=$fanLedEnabled/$fanLedEffect/$fanLedColor logo=$logoLedEnabled/$logoLedEffect/$logoLedColor shoulder=$shoulderLedEnabled/$shoulderLedEffect/$shoulderLedColor"
            )
        }

        applyOnce("now")
        handler.postDelayed({ applyOnce("750ms") }, 750L)
    }
    private fun restoreNormalProfile() {
        val prefs = getSharedPreferences("redmagic_hw_controls_prefs", Context.MODE_PRIVATE)

        val fanEnabled = prefs.getBoolean("fan_enabled", false)
        val fanLevel = prefs.getInt("fan_level", 0)
        val pumpEnabled = prefs.getBoolean("pump_enabled", false)
        val pumpProfile = prefs.getString("pump_profile", "quick") ?: "quick"

        val fanLedEnabled = prefs.getBoolean("fan_led_enabled", false)
        val fanLedEffect = prefs.getString("fan_led_effect", "steady") ?: "steady"
        val fanLedColor = prefs.getInt("fan_led_color", 5)

        val logoLedEnabled = prefs.getBoolean("logo_led_enabled", true)
        val logoLedEffect = prefs.getString("logo_led_effect", "steady") ?: "steady"
        val logoLedColor = prefs.getInt("logo_led_color", 1)

        val shoulderLedEnabled = prefs.getBoolean("shoulder_led_enabled", true)
        val shoulderLedEffect = prefs.getString("shoulder_led_effect", "breathe") ?: "breathe"
        val shoulderLedColor = prefs.getInt("shoulder_led_color", 8)

        fun restoreOnce(reason: String) {
            if (fanEnabled) {
                HardwareController.setFanLevel(fanLevel)
            } else {
                HardwareController.enableFan(false)
            }

            if (pumpEnabled) {
                HardwareController.setPumpProfile(pumpProfile)
            } else {
                HardwareController.enablePump(false)
            }

            if (fanLedEnabled) {
                HardwareController.setFanLedEnabled(true)
                if (fanLedEffect.startsWith("preset:")) {
                    HardwareController.setFanLedStockPreset(fanLedEffect.removePrefix("preset:"))
                } else {
                    HardwareController.setFanLedEffect(fanLedEffect, fanLedColor)
                }
            } else {
                HardwareController.setFanLedEnabled(false)
            }

            if (logoLedEnabled) {
                HardwareController.setLogoLedEnabled(true)
                HardwareController.setLogoLedEffect(logoLedEffect, logoLedColor)
            } else {
                HardwareController.setLogoLedEnabled(false)
            }

            if (shoulderLedEnabled) {
                HardwareController.setShoulderLedEnabled(true)
                HardwareController.setShoulderLedEffect(shoulderLedEffect, shoulderLedColor)
            } else {
                HardwareController.setShoulderLedEnabled(false)
            }

            android.util.Log.i(
                "RedmagicGameMode",
                "restore[$reason] fan=$fanLedEnabled/$fanLedEffect/$fanLedColor logo=$logoLedEnabled/$logoLedEffect/$logoLedColor shoulder=$shoulderLedEnabled/$shoulderLedEffect/$shoulderLedColor"
            )
        }

        restoreOnce("now")
        handler.postDelayed({ restoreOnce("750ms") }, 750L)
    }
}
