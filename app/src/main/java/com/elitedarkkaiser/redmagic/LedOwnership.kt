package com.elitedarkkaiser.redmagic

import android.content.SharedPreferences

object LedOwnership {
    fun gameOwns(prefs: SharedPreferences): Boolean =
        prefs.getBoolean(PrefsKeys.GAME_LED_OWNER, false)

    fun callOwns(prefs: SharedPreferences): Boolean =
        prefs.getBoolean(PrefsKeys.CALL_LED_OWNER, false)

    fun chargingOwns(prefs: SharedPreferences): Boolean =
        prefs.getBoolean(PrefsKeys.CHARGING_LED_OWNER, false)

    fun normalAllowed(prefs: SharedPreferences): Boolean =
        !gameOwns(prefs) && !callOwns(prefs) && !chargingOwns(prefs)
}
