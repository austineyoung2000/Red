package com.elitedarkkaiser.redmagic

import android.content.Context
import android.widget.Toast

internal object ProfileActions {

    fun saveProfile(
        context: Context,
        profileName: String,
        profile: HardwareProfile,
        onSaved: () -> Unit
    ) {
        ProfileManager.upsertProfile(context, profile)
        Toast.makeText(context, "Saved $profileName", Toast.LENGTH_SHORT).show()
        onSaved()
    }

    fun afterProfileApplied(
        profile: HardwareProfile,
        setAutoFanEnabledSaved: (Boolean) -> Unit,
        savePumpState: () -> Unit,
        saveAutoPumpState: () -> Unit,
        saveFanLedState: () -> Unit,
        saveLogoLedState: () -> Unit,
        saveShoulderLedState: () -> Unit,
        startAutoFanService: () -> Unit,
        stopAutoFanService: () -> Unit,
        startSmartMicroPump: () -> Unit,
        stopSmartMicroPump: () -> Unit,
        refreshStatus: () -> Unit,
        refreshSmartPumpStatusViews: () -> Unit
    ) {
        setAutoFanEnabledSaved(profile.autoFanEnabled)
        savePumpState()
        saveAutoPumpState()
        saveFanLedState()
        saveLogoLedState()
        saveShoulderLedState()

        if (profile.autoFanEnabled) {
            startAutoFanService()
        } else {
            stopAutoFanService()
        }

        if (profile.autoPumpEnabled) {
            startSmartMicroPump()
        } else {
            stopSmartMicroPump()
        }

        refreshStatus()
        refreshSmartPumpStatusViews()
    }
}
