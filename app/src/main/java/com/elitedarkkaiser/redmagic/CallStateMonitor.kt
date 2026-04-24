package com.elitedarkkaiser.redmagic

import android.content.Context
import android.media.AudioManager
import android.telephony.TelephonyManager

object CallStateMonitor {
    fun isInAnyCall(context: Context): Boolean {
        val audio = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        if (
            audio.mode == AudioManager.MODE_IN_CALL ||
            audio.mode == AudioManager.MODE_IN_COMMUNICATION
        ) {
            return true
        }

        return try {
            val telephony = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            telephony.callState == TelephonyManager.CALL_STATE_RINGING ||
                telephony.callState == TelephonyManager.CALL_STATE_OFFHOOK
        } catch (_: Throwable) {
            false
        }
    }

    fun phoneStateLabel(context: Context): String {
        return try {
            val telephony = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            when (telephony.callState) {
                TelephonyManager.CALL_STATE_RINGING -> "RINGING"
                TelephonyManager.CALL_STATE_OFFHOOK -> "OFFHOOK"
                TelephonyManager.CALL_STATE_IDLE -> "IDLE"
                else -> "UNKNOWN"
            }
        } catch (_: Throwable) {
            "NO PERMISSION"
        }
    }

    fun audioMode(context: Context): Int {
        val audio = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        return audio.mode
    }
}
