package com.elitedarkkaiser.redmagic

import android.content.Context

object ModeHardwareApplier {
    fun apply(context: Context, profile: ModeHardwareProfile) {
        if (profile.fanEnabled) {
            HardwareController.setFanLevel(profile.fanLevel)
        } else {
            HardwareController.enableFan(false)
        }

        MicroPumpController.setEnabled(context, profile.pumpEnabled)

        if (profile.fanLedEnabled) {
            HardwareController.setFanLedEnabled(true)
            if (profile.fanLedPresetValue.isNotBlank()) {
                HardwareController.setFanLedStockPreset(profile.fanLedPresetValue)
            } else if (profile.fanLedEffect.startsWith("preset:")) {
                HardwareController.setFanLedStockPreset(profile.fanLedEffect.removePrefix("preset:"))
            } else {
                HardwareController.setFanLedEffect(profile.fanLedEffect, profile.fanLedColor)
            }
        } else {
            HardwareController.setFanLedEnabled(false)
        }

        if (profile.logoLedEnabled) {
            HardwareController.setLogoLedEnabled(true)
            HardwareController.setLogoLedEffect(profile.logoLedEffect, profile.logoLedColor)
        } else {
            HardwareController.setLogoLedEnabled(false)
        }

        if (profile.shoulderLedEnabled) {
            HardwareController.setShoulderLedEnabled(true)
            HardwareController.setShoulderLedEffect(profile.shoulderLedEffect, profile.shoulderLedColor)
        } else {
            HardwareController.setShoulderLedEnabled(false)
        }
    }
}
