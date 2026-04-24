package com.elitedarkkaiser.redmagic

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.CheckBox
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.TextView
import android.view.animation.LinearInterpolator
import android.animation.ValueAnimator
import android.animation.ArgbEvaluator
import android.widget.Toast

class MainActivity : Activity() {

    private var useFahrenheit = true
    private val tempUnitPrefKey = "temp_unit_fahrenheit"

    private fun isUseFahrenheitSaved(): Boolean {
        return prefs().getBoolean(tempUnitPrefKey, true)
    }

    private fun saveUseFahrenheit(useF: Boolean) {
        prefs().edit().putBoolean(tempUnitPrefKey, useF).apply()
    }

    private fun formatDisplayTempFromF(tempF: Float?): String {
        if (tempF == null) return "--"
        return if (useFahrenheit) {
            "${tempF.toInt()}°F"
        } else {
            "${((tempF - 32f) * 5f / 9f).toInt()}°C"
        }
    }


    private lateinit var rootChip: TextView
    private lateinit var fanChip: TextView
    private lateinit var rpmChip: TextView
    private lateinit var tempChip: TextView

    private lateinit var tempText: TextView
    private lateinit var curveStatusText: TextView
    private lateinit var fanSeek: SeekBar
    private lateinit var autoCurveCheck: CheckBox

    private lateinit var quietCardRef: LinearLayout
    private lateinit var balancedCardRef: LinearLayout
    private lateinit var turboCardRef: LinearLayout

    private lateinit var deviceModelValue: TextView
    private lateinit var deviceRomValue: TextView
    private lateinit var deviceCpuValue: TextView
    private lateinit var deviceRamValue: TextView

    private lateinit var homeTab: LinearLayout
    private lateinit var coolingTab: LinearLayout
    private lateinit var controlsTab: LinearLayout
    private lateinit var lightingTab: LinearLayout
    private var gameModeAppsTextRef: TextView? = null

    private var smartPumpStatusView: TextView? = null
    private var smartPumpSpeedView: TextView? = null

    private lateinit var homeNav: LinearLayout
    private lateinit var coolingNav: LinearLayout
    private lateinit var controlsNav: LinearLayout
    private lateinit var lightingNav: LinearLayout

    private var selectedCurve = "balanced"
    private var autoFanCurveEnabled = false
    private var realTimePreviewEnabled = true

    private var fanLedEnabled = true
    private var fanLedEffect = "steady"
    private var fanLedColor = 1

    private var logoLedEnabled = true
    private var logoLedEffect = "steady"
    private var logoLedColor = 1

    private var shoulderLedEnabled = true
    private var shoulderLedEffect = "breathe"
    private var shoulderLedColor = 8

    private var pumpEnabled = false
    private var pumpProfile = "quick"
    private var autoPumpEnabled = false

    private val prefsName = "redmagic_hw_controls_prefs"
    private val skipSupportedDialogKey = "skip_supported_dialog"
    private val autoFanEnabledKey = "auto_fan_enabled"
    private val realTimePreviewEnabledKey = "realtime_preview_enabled"
    private val selectedCurveKey = "selected_curve"
    private val fanLedEnabledKey = "fan_led_enabled"
    private val fanLedEffectKey = "fan_led_effect"
    private val fanLedColorKey = "fan_led_color"

    private val logoLedEnabledKey = "logo_led_enabled"
    private val logoLedEffectKey = "logo_led_effect"
    private val logoLedColorKey = "logo_led_color"

    private val shoulderLedEnabledKey = "shoulder_led_enabled"
    private val shoulderLedEffectKey = "shoulder_led_effect"
    private val shoulderLedColorKey = "shoulder_led_color"

    private val gameModePackagesKey = "game_mode_packages"
    private val gameModeFanEnabledKey = "game_mode_fan_enabled"
    private val gameModeFanLevelKey = "game_mode_fan_level"
    private val gameModePumpEnabledKey = "game_mode_pump_enabled"
    private val gameModePumpProfileKey = "game_mode_pump_profile"
    private val gameModeFanLedEnabledKey = "game_mode_fan_led_enabled"
    private val gameModeFanLedEffectKey = "game_mode_fan_led_effect"
    private val gameModeFanLedColorKey = "game_mode_fan_led_color"

    private fun getGameModePackagesSaved(): Set<String> {
        return prefs().getStringSet(gameModePackagesKey, emptySet()) ?: emptySet()
    }

    private fun saveGameModePackages(packages: Set<String>) {
        prefs().edit().putStringSet(gameModePackagesKey, packages).apply()
    }

    private fun gameModeAppsSummary(): String {
        val count = getGameModePackagesSaved().size
        return when {
            count <= 0 -> "No games selected"
            count == 1 -> "1 game selected"
            else -> "$count games selected"
        }
    }

    private fun getSavedGameModeProfile(): GameModeProfile {
        return GameModeProfile(
            fanEnabled = prefs().getBoolean(gameModeFanEnabledKey, true),
            fanLevel = prefs().getInt(gameModeFanLevelKey, 3),
            pumpEnabled = prefs().getBoolean(gameModePumpEnabledKey, false),
            pumpProfile = prefs().getString(gameModePumpProfileKey, "quick") ?: "quick",
            fanLedEnabled = prefs().getBoolean(gameModeFanLedEnabledKey, true),
            fanLedEffect = prefs().getString(gameModeFanLedEffectKey, "steady") ?: "steady",
            fanLedColor = prefs().getInt(gameModeFanLedColorKey, 5),
            logoLedEnabled = prefs().getBoolean("game_mode_logo_led_enabled", true),
            logoLedEffect = prefs().getString("game_mode_logo_led_effect", "steady") ?: "steady",
            logoLedColor = prefs().getInt("game_mode_logo_led_color", 1),
            shoulderLedEnabled = prefs().getBoolean("game_mode_shoulder_led_enabled", true),
            shoulderLedEffect = prefs().getString("game_mode_shoulder_led_effect", "breathe") ?: "breathe",
            shoulderLedColor = prefs().getInt("game_mode_shoulder_led_color", 8)
        )
    }

    private fun saveGameModeProfile(profile: GameModeProfile) {
        prefs().edit()
            .putBoolean(gameModeFanEnabledKey, profile.fanEnabled)
            .putInt(gameModeFanLevelKey, profile.fanLevel)
            .putBoolean(gameModePumpEnabledKey, profile.pumpEnabled)
            .putString(gameModePumpProfileKey, profile.pumpProfile)
            .putBoolean(gameModeFanLedEnabledKey, profile.fanLedEnabled)
            .putString(gameModeFanLedEffectKey, profile.fanLedEffect)
            .putInt(gameModeFanLedColorKey, profile.fanLedColor)
            .putBoolean("game_mode_logo_led_enabled", profile.logoLedEnabled)
            .putString("game_mode_logo_led_effect", profile.logoLedEffect)
            .putInt("game_mode_logo_led_color", profile.logoLedColor)
            .putBoolean("game_mode_shoulder_led_enabled", profile.shoulderLedEnabled)
            .putString("game_mode_shoulder_led_effect", profile.shoulderLedEffect)
            .putInt("game_mode_shoulder_led_color", profile.shoulderLedColor)
            .apply()
    }

    private fun applySavedGameModeProfileNow() {
        val p = getSavedGameModeProfile()

        if (p.fanEnabled) {
            HardwareController.setFanLevel(p.fanLevel)
        } else {
            HardwareController.enableFan(false)
        }

        if (p.pumpEnabled) {
            HardwareController.setPumpProfile(p.pumpProfile)
        } else {
            HardwareController.enablePump(false)
        }

        if (p.fanLedEnabled) {
            applyFanLedSelection(p.fanLedEffect, p.fanLedColor)
        } else {
            HardwareController.setFanLedEnabled(false)
        }

        if (p.logoLedEnabled) {
            HardwareController.setLogoLedEffect(p.logoLedEffect, p.logoLedColor)
        }
    }

    private fun restoreNormalProfileNow() {
        if (HardwareController.isFanEnabled()) {
            HardwareController.setFanLevel(fanSeek.progress)
        } else {
            HardwareController.enableFan(false)
        }

        if (pumpEnabled) {
            HardwareController.setPumpProfile(pumpProfile)
        } else {
            HardwareController.enablePump(false)
        }

        if (fanLedEnabled) {
            applyFanLedSelection(fanLedEffect, fanLedColor)
        } else {
            HardwareController.setFanLedEnabled(false)
        }
    }


    private fun gameModeProfileSummary(): String {
        val p = getSavedGameModeProfile()
        val fanText = if (p.fanEnabled) "Fan ${p.fanLevel}" else "Fan Off"
        val pumpText = if (p.pumpEnabled) "Pump ${p.pumpProfile.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }}" else "Pump Off"
        val ledText = if (p.fanLedEnabled) "Fan LED ${p.fanLedEffect}" else "Fan LED Off"
        return "$fanText • $pumpText • $ledText"
    }

    private data class GameAppEntry(
        val label: String,
        val packageName: String
    )

    private fun getLaunchableApps(): List<GameAppEntry> {
        return packageManager.getInstalledApplications(0)
            .mapNotNull { appInfo ->
                val pkg = appInfo.packageName ?: return@mapNotNull null
                if (pkg == packageName) return@mapNotNull null
                val label = packageManager.getApplicationLabel(appInfo)?.toString()?.trim().orEmpty()
                if (label.isBlank()) return@mapNotNull null
                GameAppEntry(label, pkg)
            }
            .distinctBy { it.packageName }
            .sortedBy { it.label.lowercase() }
    }

    private fun refreshGameModeCardUi() {
        gameModeAppsTextRef?.text = gameModeAppsSummary()
    }

    private fun showGameModeAppPicker() {
        showGamePickerDialogUI(this) {
            refreshGameModeCardUi()
        }
    }



    private val pumpEnabledKey = "pump_enabled"
    private val pumpProfileKey = "pump_profile"
    private val pumpExperimentalAcceptedKey = "pump_experimental_accepted"
    private val autoPumpEnabledKey = "auto_pump_enabled"
    private val magicKeyAppPackageKey = "magic_key_app_package"

    private val bgColor = Color.parseColor("#0A0D12")
    private val panelColor = Color.parseColor("#121720")
    private val panelPressed = Color.parseColor("#1A2230")
    private val borderColor = Color.parseColor("#232C3B")

    private val accent = Color.parseColor("#8FA3BF")
    private val chipOn = Color.parseColor("#202B38")
    private val chipActive = Color.parseColor("#2B3A4F")
    private val danger = Color.parseColor("#5B2C33")
    private val textPrimary = Color.parseColor("#E8EEF7")
    private val textSecondary = Color.parseColor("#9AA8BA")
    private val typeface: Typeface? = Typeface.SANS_SERIF
    private val highlightBorder = Color.parseColor("#7F8EA3")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        initDefaultTriggerMappings()
if (!isSupportedDevice()) {
            showUnsupportedDeviceDialog()
            return
        }

        if (!RootShell.hasRoot()) {
            showRootRequiredDialog()
            return
        }

        if (shouldSkipSupportedDialog()) {
            launchMainUi()
        } else {
            showSupportedDeviceDialog()
        }
    }

    private fun prefs() = getSharedPreferences(prefsName, Context.MODE_PRIVATE)

    private fun hasUsageStatsPermission(): Boolean {
        val appOps = getSystemService(android.app.AppOpsManager::class.java)
        val mode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow("android:get_usage_stats", android.os.Process.myUid(), packageName)
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow("android:get_usage_stats", android.os.Process.myUid(), packageName)
        }
        return mode == android.app.AppOpsManager.MODE_ALLOWED
    }

    private fun openUsageStatsAccessSettings() {
        startActivity(
            android.content.Intent(android.provider.Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        )
    }


    private fun startGameModeService() {
        if (!hasUsageStatsPermission()) {
            android.widget.Toast.makeText(
                this,
                "Grant Usage Access to enable Game Mode",
                android.widget.Toast.LENGTH_LONG
            ).show()
            return
        }
        startService(Intent(this, GameModeService::class.java))
    }


    private fun shouldSkipSupportedDialog(): Boolean {
        return prefs().getBoolean(skipSupportedDialogKey, false)
    }

    private fun setSkipSupportedDialog(skip: Boolean) {
        prefs().edit().putBoolean(skipSupportedDialogKey, skip).apply()
    }

    private fun setSelectedCurveSaved(value: String) {
        prefs().edit().putString(selectedCurveKey, value).apply()
    }

    private fun getSelectedCurveSaved(): String {
        return prefs().getString(selectedCurveKey, "balanced") ?: "balanced"
    }


    private fun isAutoFanEnabledSaved(): Boolean {
        return prefs().getBoolean(autoFanEnabledKey, false)
    }

    private fun setAutoFanEnabledSaved(enabled: Boolean) {
        prefs().edit().putBoolean(autoFanEnabledKey, enabled).apply()
    }

    private fun isRealTimePreviewEnabledSaved(): Boolean {
        return prefs().getBoolean(realTimePreviewEnabledKey, true)
    }

    private fun saveRealTimePreviewEnabled(enabled: Boolean) {
        prefs().edit().putBoolean(realTimePreviewEnabledKey, enabled).apply()
    }

    private fun savedMagicKeyAppPackage(): String? {
        return prefs().getString(magicKeyAppPackageKey, null)
    }

    private fun saveMagicKeyAppPackage(pkg: String?) {
        prefs().edit().putString(magicKeyAppPackageKey, pkg).apply()
    }

    private fun resolveMagicKeyAppLabel(pkg: String?): String {
        if (pkg.isNullOrBlank()) return "Choose App"
        return try {
            val appInfo = packageManager.getApplicationInfo(pkg, 0)
            packageManager.getApplicationLabel(appInfo).toString()
        } catch (_: Throwable) {
            pkg
        }
    }

    private fun readMagicKeyModeLabel(): String {
        val raw = RootShell.execForOutput("settings get system fourth_physical_key_function_value")?.trim().orEmpty()
        return when (raw) {
            "1" -> "Camera"
            "2" -> "GameSpace"
            "3" -> "Sound Mode"
            "4" -> "Flashlight"
            "5" -> "Voice Recorder"
            "16" -> "Launch App"
            "0" -> "Disabled"
            else -> "Unknown"
        }
    }

    private fun applyStockMagicKeyMode(
        label: String,
        applyMode: () -> Boolean,
        statusLabel: TextView,
        sliderButton: Button? = null
    ) {
        val ok = applyMode()
        if (ok) {
            saveMagicKeyAppPackage(null)
            sliderButton?.text = "MAGIC KEY APP: Choose App"
            statusLabel.text = "Current: $label"
            refreshStatus()
            Toast.makeText(this, "Magic Key set to $label", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Failed to set Magic Key to $label", Toast.LENGTH_SHORT).show()
        }
    }

    private fun applyLaunchAppMagicKeyMode(
        pkg: String,
        label: String,
        statusLabel: TextView,
        sliderButton: Button
    ) {
        val ok = HardwareController.setSliderLaunchApp(pkg)
        if (ok) {
            saveMagicKeyAppPackage(pkg)
            sliderButton.text = "MAGIC KEY APP: $label"
            statusLabel.text = "Current: Launch App"
            refreshStatus()
            Toast.makeText(this, "Magic Key set to launch $label", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Failed to set Magic Key app", Toast.LENGTH_SHORT).show()
        }
    }

    private fun disableMagicKeyMode(statusLabel: TextView, sliderButton: Button? = null) {
        val ok = HardwareController.disableSliderSystemHandling()
        if (ok) {
            saveMagicKeyAppPackage(null)
            sliderButton?.text = "MAGIC KEY APP: Choose App"
            statusLabel.text = "Current: Disabled"
            refreshStatus()
            Toast.makeText(this, "Magic Key disabled", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Failed to disable Magic Key", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showMagicKeyAppPicker(targetButton: Button) {
        data class MagicKeyAppItem(
            val pkg: String,
            val label: String,
            val launchable: Boolean,
            val icon: android.graphics.drawable.Drawable?
        )

        val allApps = packageManager.getInstalledApplications(0)
            .map { appInfo ->
                val pkg = appInfo.packageName
                val label = try {
                    packageManager.getApplicationLabel(appInfo).toString()
                } catch (_: Throwable) {
                    pkg
                }
                val icon = try {
                    packageManager.getApplicationIcon(appInfo)
                } catch (_: Throwable) {
                    null
                }
                MagicKeyAppItem(
                    pkg = pkg,
                    label = label,
                    launchable = packageManager.getLaunchIntentForPackage(pkg) != null,
                    icon = icon
                )
            }
            .sortedWith(
                compareBy<MagicKeyAppItem> { it.label.lowercase() }
                    .thenBy { it.pkg.lowercase() }
            )

        if (allApps.isEmpty()) {
            Toast.makeText(this, "No installed apps found", Toast.LENGTH_SHORT).show()
            return
        }

        val titleView = TextView(this).apply {
            text = "Choose Magic Key app"
            textSize = 20f
            setTextColor(textPrimary)
            setTypeface(typeface, Typeface.BOLD)
            setPadding(0, 0, 0, dp(12))
        }

        val subtitleView = TextView(this).apply {
            text = "Search by app name or package name. Lists user and system apps."
            textSize = 12f
            setTextColor(textSecondary)
            setPadding(0, 0, 0, dp(12))
        }

        val searchInput = EditText(this).apply {
            hint = "Search apps or package names"
            setTextColor(textPrimary)
            setHintTextColor(textSecondary)
            textSize = 14f
            setPadding(dp(16), dp(12), dp(16), dp(12))
            background = roundedBg(Color.parseColor("#121A27"), Color.parseColor("#263246"), 18)
        }

        val listView = android.widget.ListView(this).apply {
            divider = android.graphics.drawable.ColorDrawable(Color.parseColor("#263246"))
            dividerHeight = dp(1)
            setBackgroundColor(Color.TRANSPARENT)
            isVerticalScrollBarEnabled = true
        }

        val filteredApps = ArrayList(allApps)

        val adapter = object : android.widget.BaseAdapter() {
            override fun getCount(): Int = filteredApps.size
            override fun getItem(position: Int): Any = filteredApps[position]
            override fun getItemId(position: Int): Long = position.toLong()

            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val item = filteredApps[position]

                val row = LinearLayout(this@MainActivity).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL
                    setPadding(dp(14), dp(12), dp(14), dp(12))
                    setBackgroundColor(Color.TRANSPARENT)
                }

                val iconView = ImageView(this@MainActivity).apply {
                    layoutParams = LinearLayout.LayoutParams(dp(40), dp(40))
                    setImageDrawable(item.icon)
                }

                val textWrap = LinearLayout(this@MainActivity).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(dp(12), 0, 0, 0)
                    layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                }

                val labelView = TextView(this@MainActivity).apply {
                    text = item.label
                    textSize = 14f
                    setTextColor(textPrimary)
                    setTypeface(typeface, Typeface.BOLD)
                }

                val pkgView = TextView(this@MainActivity).apply {
                    text = if (item.launchable) item.pkg else "${item.pkg}  •  No launcher activity"
                    textSize = 11f
                    setTextColor(textSecondary)
                    setLineSpacing(0f, 1.1f)
                }

                textWrap.addView(labelView)
                textWrap.addView(pkgView)

                row.addView(iconView)
                row.addView(textWrap)

                return row
            }
        }

        fun applyFilter(query: String) {
            val q = query.trim().lowercase()
            filteredApps.clear()
            if (q.isEmpty()) {
                filteredApps.addAll(allApps)
            } else {
                filteredApps.addAll(
                    allApps.filter {
                        it.label.lowercase().contains(q) || it.pkg.lowercase().contains(q)
                    }
                )
            }
            adapter.notifyDataSetChanged()
        }

        listView.adapter = adapter

        searchInput.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                applyFilter(s?.toString().orEmpty())
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        val cancelBtn = Button(this).apply {
            text = "Cancel"
            textSize = 13f
            setAllCaps(false)
            setTextColor(textPrimary)
            background = roundedFill(Color.parseColor("#1E2633"), 14)
            setPadding(dp(18), dp(10), dp(18), dp(10))
        }

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(22), dp(18), dp(22), dp(12))
            background = roundedBg(panelColor, borderColor, 22)
            addView(titleView)
            addView(subtitleView)
            addView(searchInput)
            addView(space(dp(12)))
            addView(
                listView,
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    dp(420)
                )
            )
            addView(space(dp(12)))
            addView(LinearLayout(this@MainActivity).apply {
                gravity = Gravity.END
                addView(cancelBtn)
            })
        }

        val dialog = AlertDialog.Builder(this)
            .setView(container)
            .setCancelable(true)
            .create()

        dialog.window?.setBackgroundDrawable(
            android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT)
        )

        listView.setOnItemClickListener { _, _, which, _ ->
            val item = filteredApps[which]
            applyLaunchAppMagicKeyMode(
                pkg = item.pkg,
                label = item.label,
                statusLabel = magicKeyStatusLabelRef ?: return@setOnItemClickListener,
                sliderButton = targetButton
            )

            if (!item.launchable) {
                Toast.makeText(
                    this,
                    "${item.label} saved, but it may not open because it has no launcher activity",
                    Toast.LENGTH_LONG
                ).show()
            }

            dialog.dismiss()
        }

        cancelBtn.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()

        dialog.window?.apply {
            setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))
            setDimAmount(0.65f)
        }
    }

    private fun isFanLedEnabledSaved(): Boolean {
        return prefs().getBoolean(fanLedEnabledKey, false)
    }

    private fun savedFanLedEffect(): String {
        return prefs().getString(fanLedEffectKey, "steady") ?: "steady"
    }

    private fun savedFanLedColor(): Int {
        return prefs().getInt(fanLedColorKey, 5)
    }

    private fun applyFanLedSelection(effect: String, color: Int) {
        if (effect.startsWith("preset:")) {
            applyFanPreset(effect.removePrefix("preset:"))
        } else {
            HardwareController.setFanLedEffect(effect, color)
        }
    }

    private fun saveFanLedState() {
        prefs().edit()
            .putBoolean(fanLedEnabledKey, fanLedEnabled)
            .putString(fanLedEffectKey, fanLedEffect)
            .putInt(fanLedColorKey, fanLedColor)
            .commit()
    }

    private fun applySavedFanLedStateOnLaunch() {
        fanLedEnabled = isFanLedEnabledSaved()
        fanLedEffect = savedFanLedEffect()
        fanLedColor = savedFanLedColor()
    }

    private fun isLogoLedEnabledSaved(): Boolean {
        return prefs().getBoolean(logoLedEnabledKey, true)
    }

    private fun savedLogoLedEffect(): String {
        return prefs().getString(logoLedEffectKey, "steady") ?: "steady"
    }

    private fun savedLogoLedColor(): Int {
        return prefs().getInt(logoLedColorKey, 1)
    }

    private fun saveLogoLedState() {
        prefs().edit()
            .putBoolean(logoLedEnabledKey, logoLedEnabled)
            .putString(logoLedEffectKey, logoLedEffect)
            .putInt(logoLedColorKey, logoLedColor)
            .commit()
    }

    private fun applySavedLogoLedStateOnLaunch() {
        logoLedEnabled = isLogoLedEnabledSaved()
        logoLedEffect = savedLogoLedEffect()
        logoLedColor = savedLogoLedColor()
    }

    private fun isShoulderLedEnabledSaved(): Boolean {
        return prefs().getBoolean(shoulderLedEnabledKey, true)
    }

    private fun savedShoulderLedEffect(): String {
        return prefs().getString(shoulderLedEffectKey, "breathe") ?: "breathe"
    }

    private fun savedShoulderLedColor(): Int {
        return prefs().getInt(shoulderLedColorKey, 8)
    }

    private fun saveShoulderLedState() {
        prefs().edit()
            .putBoolean(shoulderLedEnabledKey, shoulderLedEnabled)
            .putString(shoulderLedEffectKey, shoulderLedEffect)
            .putInt(shoulderLedColorKey, shoulderLedColor)
            .commit()
    }

    private fun applySavedShoulderLedStateOnLaunch() {
        shoulderLedEnabled = isShoulderLedEnabledSaved()
        shoulderLedEffect = savedShoulderLedEffect()
        shoulderLedColor = savedShoulderLedColor()
    }

    private fun isPumpEnabledSaved(): Boolean {
        return prefs().getBoolean(pumpEnabledKey, false)
    }

    private fun savedPumpProfile(): String {
        return prefs().getString(pumpProfileKey, "quick") ?: "quick"
    }

    private fun savePumpState() {
        prefs().edit()
            .putBoolean(pumpEnabledKey, pumpEnabled)
            .putString(pumpProfileKey, pumpProfile)
            .commit()
    }

    private fun applySavedPumpStateOnLaunch() {
        pumpEnabled = isPumpEnabledSaved()
        pumpProfile = savedPumpProfile().lowercase()
        if (pumpProfile != "slow" && pumpProfile != "medium" && pumpProfile != "quick" && pumpProfile != "experimental") {
            pumpProfile = "quick"
        }
    }

    private fun isPumpExperimentalAccepted(): Boolean {
        return prefs().getBoolean(pumpExperimentalAcceptedKey, false)
    }

    private fun setPumpExperimentalAccepted(accepted: Boolean) {
        prefs().edit().putBoolean(pumpExperimentalAcceptedKey, accepted).commit()
    }

    private fun isAutoPumpEnabledSaved(): Boolean {
        return prefs().getBoolean(autoPumpEnabledKey, false)
    }

    private fun saveAutoPumpState() {
        prefs().edit().putBoolean(autoPumpEnabledKey, autoPumpEnabled).commit()
    }

    private fun buildAutoPumpStatusText(): Pair<String, String> {
        val tempF = DashboardSnapshot.readCpuTempF().toFloatOrNull()
        if (tempF == null) {
            return "Pump Mode: AUTO • Unknown temp" to "Speed: ? • Freq: ?"
        }

        val profile = when {
            tempF >= 105f -> "Quick"
            tempF >= 90f -> "Medium"
            else -> "Slow"
        }

        val speed = when (profile) {
            "Quick" -> 80
            "Medium" -> 60
            else -> 40
        }

        return "Pump Mode: AUTO • $profile (${formatDisplayTempFromF(tempF)})" to
            "Speed: $speed • Freq: 4"
    }


    private fun buildCurrentHardwareProfile(name: String): HardwareProfile {
        return HardwareProfile(
            name = name,

            fanEnabled = HardwareController.isFanEnabled(),
            fanLevel = fanSeek.progress,
            autoFanEnabled = autoFanCurveEnabled,
            fanCurveMode = selectedCurve,

            pumpEnabled = pumpEnabled,
            pumpProfile = pumpProfile,
            autoPumpEnabled = autoPumpEnabled,

            fanLedEnabled = fanLedEnabled,
            fanLedEffect = fanLedEffect,
            fanLedColor = fanLedColor,

            logoLedEnabled = logoLedEnabled,
            logoLedEffect = logoLedEffect,
            logoLedColor = logoLedColor,

            shoulderLedEnabled = shoulderLedEnabled,
            shoulderLedEffect = shoulderLedEffect,
            shoulderLedColor = shoulderLedColor,

            triggerEnabled = getSharedPreferences("triggers", MODE_PRIVATE).getBoolean("triggers_auto_start", false),
            hapticsEnabled = getSharedPreferences("triggers", MODE_PRIVATE).getBoolean("haptics_enabled", true),
            leftTriggerAction = getSharedPreferences("triggers", MODE_PRIVATE).getString("left_trigger", "NONE") ?: "NONE",
            rightTriggerAction = getSharedPreferences("triggers", MODE_PRIVATE).getString("right_trigger", "NONE") ?: "NONE",
            intentUnlockRightTrigger = getSharedPreferences("triggers", MODE_PRIVATE).getBoolean("intent_unlock_right_trigger", true),
            triggersAutoStart = getSharedPreferences("triggers", MODE_PRIVATE).getBoolean("triggers_auto_start", false)
        )
    }

    private fun applyProfileToUiState(profile: HardwareProfile) {
        autoFanCurveEnabled = profile.autoFanEnabled
        selectedCurve = profile.fanCurveMode

        pumpEnabled = profile.pumpEnabled
        pumpProfile = profile.pumpProfile
        autoPumpEnabled = profile.autoPumpEnabled

        fanLedEnabled = profile.fanLedEnabled
        fanLedEffect = profile.fanLedEffect
        fanLedColor = profile.fanLedColor

        logoLedEnabled = profile.logoLedEnabled
        logoLedEffect = profile.logoLedEffect
        logoLedColor = profile.logoLedColor

        shoulderLedEnabled = profile.shoulderLedEnabled
        shoulderLedEffect = profile.shoulderLedEffect
        shoulderLedColor = profile.shoulderLedColor

        fanSeek.progress = profile.fanLevel

        getSharedPreferences("triggers", MODE_PRIVATE)
            .edit()
            .putString("left_trigger", profile.leftTriggerAction)
            .putString("right_trigger", profile.rightTriggerAction)
            .putBoolean("haptics_enabled", profile.hapticsEnabled)
            .putBoolean("intent_unlock_right_trigger", profile.intentUnlockRightTrigger)
            .putBoolean("triggers_auto_start", profile.triggersAutoStart)
            .apply()

        if (profile.triggersAutoStart) {
            HardwareController.enableTriggers()
            startService(Intent(this, TriggerRootService::class.java))
        }

        ProfileActions.afterProfileApplied(
            profile = profile,
            setAutoFanEnabledSaved = { enabled -> setAutoFanEnabledSaved(enabled) },
            savePumpState = { savePumpState() },
            saveAutoPumpState = { saveAutoPumpState() },
            saveFanLedState = { saveFanLedState() },
            saveLogoLedState = { saveLogoLedState() },
            saveShoulderLedState = { saveShoulderLedState() },
            startAutoFanService = { startAutoFanService() },
            stopAutoFanService = { stopAutoFanService() },
            startAutoPumpService = { startAutoPumpService() },
            stopAutoPumpService = { stopAutoPumpService() },
            refreshStatus = { refreshStatus() },
            refreshSmartPumpStatusViews = { refreshSmartPumpStatusViews() }
        )
    }

    private fun showSaveProfileDialog(onSaved: () -> Unit) {
        ProfileDialogs.showStyledSaveProfileDialog(
            context = this,
            textPrimary = textPrimary,
            textSecondary = textSecondary,
            panelColor = panelColor,
            borderColor = borderColor,
            typeface = typeface,
            dp = { value -> dp(value) },
            roundedBg = { fill, stroke, radius -> roundedBg(fill, stroke, radius) },
            actionButton = { text, isDanger, onClick ->
                actionButton(text, isDanger = isDanger, onClick = onClick)
            },
            space = { value -> space(value) },
            buildProfile = { name -> buildCurrentHardwareProfile(name) },
            onSaved = onSaved
        )
    }

    private fun showDeleteProfileDialog(profileName: String, onDeleted: () -> Unit) {
        ProfileDialogs.showDeleteProfileDialog(this, profileName) {
            ProfileManager.deleteProfile(this, profileName)
            Toast.makeText(this, "Deleted $profileName", Toast.LENGTH_SHORT).show()
            onDeleted()
        }
    }

    private fun refreshSmartPumpStatusViews() {
        val statusView = smartPumpStatusView ?: return
        val speedView = smartPumpSpeedView ?: return

        if (autoPumpEnabled) {
            val status = buildAutoPumpStatusText()
            statusView.text = status.first
            speedView.text = status.second
        } else {
            val manualLabel = pumpProfile.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase() else it.toString()
            }
            val manualSpeed = when (pumpProfile.lowercase()) {
                "slow" -> 40
                "medium" -> 60
                "quick" -> 80
                "experimental" -> 90
                else -> 60
            }
            statusView.text = "Pump Mode: MANUAL • $manualLabel"
            speedView.text = "Speed: $manualSpeed • Freq: 4"
        }
    }

    private fun applyPumpProfile(profile: String) {
        pumpProfile = profile
        pumpEnabled = true
        autoPumpEnabled = false
        savePumpState()
        saveAutoPumpState()
        stopAutoPumpService()
        HardwareController.setPumpProfile(profile)
        refreshStatus()
        refreshSmartPumpStatusViews()
    }

    private fun confirmExperimentalPumpThenApply() {
        if (isPumpExperimentalAccepted()) {
            applyPumpProfile("experimental")
            return
        }

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(22), dp(18), dp(22), dp(10))
            background = roundedBg(panelColor, borderColor, 22)
        }

        val titleView = TextView(this).apply {
            text = "Experimental Pump Mode"
            textSize = 20f
            setTextColor(textPrimary)
            setTypeface(typeface, Typeface.BOLD)
        }

        val bodyView = TextView(this).apply {
            text = "This mode overclocks the liquid cooling pump beyond the standard profiles. It may provide thermal or performance benefits under heavy load, but it can also increase wear, instability, heat, noise, and possible pump failure or reduced lifespan.\n\nUse only if you understand the risks."
            textSize = 14f
            setTextColor(textSecondary)
            setLineSpacing(0f, 1.15f)
            setPadding(0, dp(14), 0, 0)
        }

        val helperView = TextView(this).apply {
            text = "Recommended only for gaming or sustained high-performance workloads."
            textSize = 12f
            setTextColor(textSecondary)
            setPadding(0, dp(10), 0, 0)
        }

        val buttonRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.END
            setPadding(0, dp(18), 0, 0)
        }

        val cancelBtn = Button(this).apply {
            text = "Cancel"
            textSize = 13f
            setAllCaps(false)
            setTextColor(textPrimary)
            background = roundedFill(Color.parseColor("#1E2633"), 14)
            setPadding(dp(18), dp(10), dp(18), dp(10))
        }

        val acceptBtn = Button(this).apply {
            text = "I Understand"
            textSize = 13f
            setAllCaps(false)
            setTextColor(textPrimary)
            background = roundedFill(panelPressed, 14)
            setPadding(dp(18), dp(10), dp(18), dp(10))
        }

        buttonRow.addView(cancelBtn)
        buttonRow.addView(space(dp(10)))
        buttonRow.addView(acceptBtn)

        container.addView(titleView)
        container.addView(bodyView)
        container.addView(helperView)
        container.addView(buttonRow)

        val dialog = AlertDialog.Builder(this)
            .setView(container)
            .setCancelable(true)
            .create()

        dialog.window?.setBackgroundDrawable(
            android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT)
        )

        cancelBtn.setOnClickListener {
            dialog.dismiss()
        }

        acceptBtn.setOnClickListener {
            setPumpExperimentalAccepted(true)
            applyPumpProfile("experimental")
            dialog.dismiss()
        }

        dialog.show()

        dialog.window?.apply {
            setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))
            setDimAmount(0.65f)
        }
    }

    private fun startAutoFanService() {
        val intent = Intent(this, AutoFanService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun stopAutoFanService() {
        stopService(Intent(this, AutoFanService::class.java))
    }

    private fun startFanLedService() {
        val intent = Intent(this, FanLedService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun stopFanLedService() {
        stopService(Intent(this, FanLedService::class.java))
    }

    private fun startAutoPumpService() {
        val intent = Intent(this, AutoPumpService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun stopAutoPumpService() {
        stopService(Intent(this, AutoPumpService::class.java))
    }

    private fun enqueueFanLedRestore(delaySeconds: Long = 2) {
        val request = OneTimeWorkRequestBuilder<FanLedRestoreWorker>()
            .setInitialDelay(delaySeconds, TimeUnit.SECONDS)
            .addTag("fan_led_manual_restore")
            .build()

        WorkManager.getInstance(this).enqueueUniqueWork(
            "fan_led_manual_restore",
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    private fun showSupportedDeviceDialog() {
        val buildModel = Build.MODEL ?: "Unknown"
        val propModel = RootShell.execForOutput("getprop ro.product.model")?.trim() ?: "Unknown"

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(22), dp(18), dp(22), dp(10))
            background = roundedBg(panelColor, borderColor, 22)
        }

        val titleView = TextView(this).apply {
            text = "Supported Device Detected"
            textSize = 20f
            setTextColor(textPrimary)
            setTypeface(typeface, Typeface.BOLD)
        }

        val bodyView = TextView(this).apply {
            text = """
                Model check passed.

                Required model: NX809J
                Detected Build.MODEL: $buildModel
                Detected ro.product.model: $propModel

                Tap OK to continue launching Redmagic HW Controls.
            """.trimIndent()
            textSize = 14f
            setTextColor(textSecondary)
            setLineSpacing(0f, 1.15f)
            setPadding(0, dp(14), 0, 0)
        }

        val neverShowAgain = CheckBox(this).apply {
            text = "Never show again"
            textSize = 14f
            setTextColor(textPrimary)
            setPadding(0, dp(14), 0, 0)
            buttonTintList = android.content.res.ColorStateList.valueOf(accent)
        }

        val buttonRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.END
            setPadding(0, dp(18), 0, 0)
        }

        val okButton = Button(this).apply {
            text = "OK"
            textSize = 13f
            setAllCaps(false)
            setTextColor(textPrimary)
            background = roundedFill(panelPressed, 14)
            setPadding(dp(20), dp(10), dp(20), dp(10))
        }

        buttonRow.addView(okButton)

        container.addView(titleView)
        container.addView(bodyView)
        container.addView(neverShowAgain)
        container.addView(buttonRow)

        val dialog = AlertDialog.Builder(this)
            .setView(container)
            .setCancelable(false)
            .create()

        dialog.window?.setBackgroundDrawable(
            android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT)
        )

        okButton.setOnClickListener {
            setSkipSupportedDialog(neverShowAgain.isChecked)
            dialog.dismiss()
            launchMainUi()
        }

        dialog.show()

        dialog.window?.apply {
            setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))
            setDimAmount(0.65f)
        }
    }


    private fun showRootRequiredDialog() {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(22), dp(18), dp(22), dp(10))
            background = roundedBg(panelColor, borderColor, 22)
        }

        val titleView = TextView(this).apply {
            text = "Root Required"
            textSize = 20f
            setTextColor(textPrimary)
            setTypeface(typeface, Typeface.BOLD)
        }

        val bodyView = TextView(this).apply {
            text = "This app requires root access to launch.\n\nGrant root in Magisk, KernelSU, or APatch, then reopen the app."
            textSize = 14f
            setTextColor(textSecondary)
            setLineSpacing(0f, 1.15f)
            setPadding(0, dp(14), 0, 0)
        }

        val buttonRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.END
            setPadding(0, dp(18), 0, 0)
        }

        val closeButton = Button(this).apply {
            text = "Close App"
            textSize = 13f
            setAllCaps(false)
            setTextColor(textPrimary)
            background = roundedFill(danger, 14)
            setPadding(dp(20), dp(10), dp(20), dp(10))
        }

        buttonRow.addView(closeButton)

        container.addView(titleView)
        container.addView(bodyView)
        container.addView(buttonRow)

        val dialog = AlertDialog.Builder(this)
            .setView(container)
            .setCancelable(false)
            .create()

        dialog.window?.setBackgroundDrawable(
            android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT)
        )

        closeButton.setOnClickListener {
            dialog.dismiss()
            finishAffinity()
        }

        dialog.show()

        dialog.window?.apply {
            setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))
            setDimAmount(0.65f)
        }
    }

    private fun showUnsupportedDeviceDialog() {
        val buildModel = Build.MODEL ?: "Unknown"
        val propModel = RootShell.execForOutput("getprop ro.product.model")?.trim() ?: "Unknown"

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(22), dp(18), dp(22), dp(10))
            background = roundedBg(panelColor, borderColor, 22)
        }

        val titleView = TextView(this).apply {
            text = "Unsupported Device"
            textSize = 20f
            setTextColor(textPrimary)
            setTypeface(typeface, Typeface.BOLD)
        }

        val bodyView = TextView(this).apply {
            text = """
                This app only supports model NX809J.

                Detected Build.MODEL: $buildModel
                Detected ro.product.model: $propModel
            """.trimIndent()
            textSize = 14f
            setTextColor(textSecondary)
            setLineSpacing(0f, 1.15f)
            setPadding(0, dp(14), 0, 0)
        }

        val buttonRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.END
            setPadding(0, dp(18), 0, 0)
        }

        val closeButton = Button(this).apply {
            text = "Close App"
            textSize = 13f
            setAllCaps(false)
            setTextColor(textPrimary)
            background = roundedFill(danger, 14)
            setPadding(dp(20), dp(10), dp(20), dp(10))
        }

        buttonRow.addView(closeButton)

        container.addView(titleView)
        container.addView(bodyView)
        container.addView(buttonRow)

        val dialog = AlertDialog.Builder(this)
            .setView(container)
            .setCancelable(false)
            .create()

        dialog.window?.setBackgroundDrawable(
            android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT)
        )

        closeButton.setOnClickListener {
            dialog.dismiss()
            finishAffinity()
        }

        dialog.show()

        dialog.window?.apply {
            setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))
            setDimAmount(0.65f)
        }
    }

    private fun isSupportedDevice(): Boolean {
        val required = "NX809J"

        val buildModel = Build.MODEL ?: ""
        val propModel = RootShell.execForOutput("getprop ro.product.model")?.trim() ?: ""
        val propVendorModel = RootShell.execForOutput("getprop ro.product.vendor.model")?.trim() ?: ""
        val propMarketName = RootShell.execForOutput("getprop ro.product.marketname")?.trim() ?: ""

        return buildModel.contains(required, ignoreCase = true) ||
            propModel.contains(required, ignoreCase = true) ||
            propVendorModel.contains(required, ignoreCase = true) ||
            propMarketName.contains(required, ignoreCase = true)
    }

    private fun launchMainUi() {
        val topInset = getStatusBarHeight()

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(bgColor)
        }

        val contentFrame = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), topInset + dp(10), dp(16), dp(96))
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        val contentScroll = ScrollView(this).apply {
            isFillViewport = true
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
            addView(contentFrame)
        }

        homeTab = createHomeTab()
        coolingTab = createCoolingTab()
        controlsTab = createControlsTab()
        val hardwareTab = createHardwareTab()
        lightingTab = createLightingTab()

        contentFrame.addView(homeTab)
        contentFrame.addView(coolingTab)
        contentFrame.addView(controlsTab)
        contentFrame.addView(hardwareTab)
        contentFrame.addView(lightingTab)

        val navWrap = LinearLayout(this).apply {
            gravity = Gravity.CENTER
            setPadding(dp(18), 0, dp(18), dp(18))
            addView(bottomNavBar())
        }

        root.addView(contentScroll)
        root.addView(navWrap)

        setContentView(root)

        applySavedFanLedStateOnLaunch()
        applySavedLogoLedStateOnLaunch()
        applySavedShoulderLedStateOnLaunch()
        applySavedPumpStateOnLaunch()
        realTimePreviewEnabled = isRealTimePreviewEnabledSaved()
        useFahrenheit = isUseFahrenheitSaved()
        autoPumpEnabled = isAutoPumpEnabledSaved()

        if (fanLedEnabled) {
            applyFanLedSelection(fanLedEffect, fanLedColor)
            startFanLedService()
        } else {
            HardwareController.setFanLedEnabled(false)
            stopFanLedService()
        }

        if (logoLedEnabled) {
            HardwareController.setLogoLedEffect(logoLedEffect, logoLedColor)
        } else {
            HardwareController.setLogoLedEnabled(false)
        }

        if (shoulderLedEnabled) {
            HardwareController.setShoulderLedEffect(shoulderLedEffect, shoulderLedColor)
        } else {
            HardwareController.setShoulderLedEnabled(false)
        }

        if (pumpEnabled) {
            HardwareController.setPumpProfile(pumpProfile)
        } else {
            HardwareController.enablePump(false)
        }

        selectedCurve = getSelectedCurveSaved()
        autoFanCurveEnabled = isAutoFanEnabledSaved()
        autoCurveCheck.isChecked = autoFanCurveEnabled

        if (autoFanCurveEnabled) {
            curveStatusText.text = "Auto fan curve active • Running in background service"
        } else {
            curveStatusText.text = "Selected curve: $selectedCurve • Manual control"
        }

        when (selectedCurve) {
            "quiet" -> setActiveMode(quietCardRef)
            "turbo" -> setActiveMode(turboCardRef)
            else -> setActiveMode(balancedCardRef)
        }
        updateManualCurveUiState()
        switchTab("home")
        refreshStatus()
        startGameModeService()
    }

    private fun createHomeTab(): LinearLayout {
        val container = scrollTabContainer()
        // Usage Access Permission Card
        if (!hasUsageStatsPermission()) {
            val usageCard = sectionPanel().apply {

                val title = TextView(this@MainActivity).apply {
                    text = "Game Mode Permission Required"
                    textSize = 16f
                    setTextColor(textPrimary)
                    setTypeface(typeface, android.graphics.Typeface.BOLD)
                }

                val desc = TextView(this@MainActivity).apply {
                    text = "Grant Usage Access so Game Mode can detect running games."
                    textSize = 13f
                    setTextColor(textSecondary)
                    setPadding(0, dp(6), 0, dp(10))
                }

                val btn = Button(this@MainActivity).apply {
                    text = "Grant Usage Access"
                    textSize = 13f
                    setAllCaps(false)
                    setTextColor(textPrimary)
                    background = roundedFill(panelPressed, 14)
                    setOnClickListener {
                        openUsageStatsAccessSettings()
                    }
                }

                addView(title)
                addView(desc)
                addView(btn)
            }

            container.addView(usageCard)
        val gameSelectBtn = Button(this@MainActivity).apply {
            text = "Select Games for Game Mode"
            textSize = 13f
            setAllCaps(false)
            setTextColor(textPrimary)
            background = roundedFill(panelPressed, 14)
            setOnClickListener {
                showGamePickerDialog()
            }
        }

        container.addView(gameSelectBtn)
        val gameStatus = TextView(this@MainActivity).apply {
            textSize = 13f
            setTextColor(textSecondary)
            setPadding(0, dp(8), 0, dp(8))
        }

        updateGameModeStatusUI(gameStatus)
        container.addView(gameStatus)


        }


        val welcomeCard = sectionPanel().apply {

            val headerRow = LinearLayout(this@MainActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }

            val iconView = ImageView(this@MainActivity).apply {
                setImageResource(R.mipmap.ic_launcher)
                layoutParams = LinearLayout.LayoutParams(dp(60), dp(60))
            }

            val titleView = ledTitleText("Redmagic Control Center")

            headerRow.addView(iconView)
            headerRow.addView(titleView)

            addView(headerRow)
            addView(subtitleText("Cooling, lighting, triggers and hardware controls for Redmagic 11 Pro"))
        }

        val summaryCard = sectionPanel().apply {
            addView(sectionHeader("⌂", "WELCOME"))

            addView(bodyText("RedMagic HW Controls is a root-powered control center for RedMagic 11 Pro that brings key hardware features into one place with a cleaner interface than stock tools."))

            addView(bodyText("It lets you manage cooling behavior, fan profiles, micropump control, fan LED effects, logo lighting, shoulder LED strips, trigger tools, slider actions, and haptics directly from the app."))

            addView(bodyText("The app is built around real device paths and behavior confirmed on hardware so the controls feel practical, focused, and close to an OEM-style utility."))

            val linksRow = LinearLayout(this@MainActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(0, dp(14), 0, 0)
            }

            val githubBtn = segmentedChip("GitHub", false) {
                openUrl("https://github.com/austineyoung2000/Red")
            }

            val referenceBtn = segmentedChip("Reference", false) {
                openUrl("https://www.reddit.com/r/RedMagic/comments/1rtoako/red_magic_11_pro_hardware_control_guide_for/")
            }

            linksRow.addView(githubBtn)
            linksRow.addView(space(dp(8)))
            linksRow.addView(referenceBtn)

            addView(linksRow)
        }

        deviceModelValue = infoValue()
        deviceRomValue = infoValue()
        deviceCpuValue = infoValue()
        deviceRamValue = infoValue()

        val infoCard = sectionPanel().apply {
            addView(sectionHeader("ⓘ", "DEVICE INFO"))
            addView(infoRow("Model", deviceModelValue))
            addView(infoRow("ROM", deviceRomValue))
            addView(infoRow("CPU", deviceCpuValue))
            addView(infoRow("RAM", deviceRamValue))
        }

        rootChip = statusChip("ROOT --")
        fanChip = statusChip("FAN --")
        rpmChip = statusChip("RPM --")
        tempChip = statusChip("TEMP --")

        val statusRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL

            addView(rootChip, LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { marginEnd = dp(6) })

            addView(fanChip, LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { marginEnd = dp(6) })

            addView(rpmChip, LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { marginEnd = dp(6) })

            addView(tempChip, LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ))
        }

        val statusScroller = HorizontalScrollView(this).apply {
            isHorizontalScrollBarEnabled = false
            addView(statusRow)
        }

        val statusCard = sectionPanel().apply {
            addView(sectionHeader("◎", "LIVE STATUS"))
            addView(statusScroller)
        }

        container.addView(welcomeCard)
        val dashboardCard = sectionPanel().apply {
            addView(sectionHeader("◈", "LIVE DASHBOARD"))

            val dashboardText = TextView(this@MainActivity).apply {
                text = DashboardSnapshot.buildSummary(this@MainActivity)
                textSize = 13f
                setTextColor(textPrimary)
                setLineSpacing(0f, 1.15f)
                setPadding(0, 0, 0, dp(12))
            }

            val refreshBtn = actionButton("REFRESH DASHBOARD") {
                dashboardText.text = DashboardSnapshot.buildSummary(this@MainActivity)
            }

            addView(dashboardText)
            addView(singleRow(refreshBtn))
        }

        val automationCard = sectionPanel().apply {
            addView(sectionHeader("⚙", "AUTOMATION"))
            addView(bodyText("Auto Pump uses safe temperature rules and automatically shifts between Slow, Medium, and Quick."))
        }

        
        container.addView(summaryCard)
        container.addView(infoCard)
        container.addView(statusCard)

        return container
    }

    private fun createCoolingTab(): LinearLayout {
        val container = scrollTabContainer()

        tempText = TextView(this).apply {
            text = "Current temp: --"
            textSize = 13f
            setTextColor(textSecondary)
            setPadding(0, dp(6), 0, dp(4))
        }

        fanSeek = SeekBar(this).apply {
            max = 5
            progress = 0
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser && !autoFanCurveEnabled) {
                        HardwareController.setFanLevel(progress)
                        refreshStatus()
                    }
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
        }

        val fanOnBtn = actionButton("FAN ON") {
            HardwareController.enableFan(true)
            refreshStatus()
        }

        val fanOffBtn = actionButton("FAN OFF", isDanger = true) {
            HardwareController.enableFan(false)
            refreshStatus()
        }

        val rpmBtn = actionButton("READ RPM") {
            refreshStatus()
        }

        quietCardRef = LinearLayout(this)
        balancedCardRef = LinearLayout(this)
        turboCardRef = LinearLayout(this)

        val modeRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val quietChip = segmentedChip("Quiet", selectedCurve == "quiet") {
            if (autoFanCurveEnabled) return@segmentedChip
            selectedCurve = "quiet"
            setSelectedCurveSaved(selectedCurve)
            val level = HardwareController.applyFanCurve(selectedCurve)
            if (level != null) fanSeek.progress = level
            curveStatusText.text = "Selected curve: Quiet • Applied immediately"
        }

        val balancedChip = segmentedChip("Balanced", selectedCurve == "balanced") {
            if (autoFanCurveEnabled) return@segmentedChip
            selectedCurve = "balanced"
            setSelectedCurveSaved(selectedCurve)
            val level = HardwareController.applyFanCurve(selectedCurve)
            if (level != null) fanSeek.progress = level
            curveStatusText.text = "Selected curve: Balanced • Applied immediately"
        }

        val turboChip = segmentedChip("Turbo", selectedCurve == "turbo") {
            if (autoFanCurveEnabled) return@segmentedChip
            selectedCurve = "turbo"
            setSelectedCurveSaved(selectedCurve)
            val level = HardwareController.applyFanCurve(selectedCurve)
            if (level != null) fanSeek.progress = level
            curveStatusText.text = "Selected curve: Turbo • Applied immediately"
        }

        modeRow.addView(quietChip)
        modeRow.addView(space(dp(8)))
        modeRow.addView(balancedChip)
        modeRow.addView(space(dp(8)))
        modeRow.addView(turboChip)

        curveStatusText = TextView(this).apply {
            text = "Selected curve: ${selectedCurve.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }}"
            textSize = 13f
            setTextColor(textSecondary)
            setPadding(0, dp(6), 0, dp(4))
        }

        autoCurveCheck = CheckBox(this).apply {
            text = "Automatic fan control based on temperature"
            setTextColor(textPrimary)
            textSize = 13f
            setPadding(0, dp(6), 0, dp(4))
            setOnCheckedChangeListener { _, checked ->
                autoFanCurveEnabled = checked
                setAutoFanEnabledSaved(checked)
                updateManualCurveUiState()

                if (checked) {
                    startAutoFanService()
                    curveStatusText.text = "Auto fan curve active • Running in background service"
                } else {
                    stopAutoFanService()
                    curveStatusText.text = "Selected curve: $selectedCurve • Manual control"
                }

                refreshStatus()
            }
        }

        val coolingCard = sectionPanel().apply {
            addView(sectionHeader("❄", "COOLING"))
            addView(tempText)

            val tempUnitRow = LinearLayout(this@MainActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(0, dp(8), 0, dp(4))
            }

            val tempUnitLabel = TextView(this@MainActivity).apply {
                text = "Use Fahrenheit"
                textSize = 13f
                setTextColor(textPrimary)
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            }

            val tempUnitSwitch = android.widget.Switch(this@MainActivity).apply {
                isChecked = useFahrenheit
                setOnCheckedChangeListener { _, checked ->
                    useFahrenheit = checked
                    saveUseFahrenheit(checked)
                    refreshStatus()
                }
            }

            tempUnitRow.addView(tempUnitLabel)
            tempUnitRow.addView(tempUnitSwitch)

            addView(tempUnitRow)
            addView(subtleLabel("Fan level"))
            addView(fanSeek)
            addView(row(fanOnBtn, fanOffBtn))
            addView(singleRow(rpmBtn))
            addView(spacer(dp(16)))
            addView(sectionHeader("◉", "PUMP"))

            val pumpSection = LinearLayout(this@MainActivity).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(14), dp(14), dp(14), dp(14))
                background = roundedBg(Color.parseColor("#161D28"), Color.parseColor("#253041"), 18)
            }

            val pumpTitle = TextView(this@MainActivity).apply {
                text = "Liquid cooling circulation"
                textSize = 15f
                setTextColor(textPrimary)
                setTypeface(typeface, Typeface.BOLD)
            }

            val pumpSubtitle = TextView(this@MainActivity).apply {
                text = "Enable or disable the micropump"
                textSize = 12f
                setTextColor(textSecondary)
                setPadding(0, dp(4), 0, dp(12))
            }

            val circulationSwitch = android.widget.Switch(this@MainActivity).apply {
                isChecked = pumpEnabled
                setOnCheckedChangeListener { _, checked ->
                    pumpEnabled = checked
                    savePumpState()
                    if (pumpEnabled) {
                        HardwareController.setPumpProfile(pumpProfile)
                    } else {
                        HardwareController.enablePump(false)
                    }
                    refreshStatus()
                    refreshSmartPumpStatusViews()
                }
            }

            val circulationRow = LinearLayout(this@MainActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                addView(LinearLayout(this@MainActivity).apply {
                    orientation = LinearLayout.VERTICAL
                    addView(pumpTitle)
                    addView(pumpSubtitle)
                }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
                addView(circulationSwitch)
            }

            val flowRateLabel = TextView(this@MainActivity).apply {
                text = "Flow rate"
                textSize = 12f
                setTextColor(textSecondary)
                setPadding(0, dp(4), 0, dp(8))
            }

            val pumpRateRow = LinearLayout(this@MainActivity).apply {
                orientation = LinearLayout.HORIZONTAL
            }

            val slowBtn = segmentedChip("Slow", pumpProfile == "slow") {
                applyPumpProfile("slow")
            }

            val mediumBtn = segmentedChip("Medium", pumpProfile == "medium") {
                applyPumpProfile("medium")
            }

            val quickBtn = segmentedChip("Quick", pumpProfile == "quick") {
                applyPumpProfile("quick")
            }

            val experimentalBtn = segmentedChip("⚠ Exp", pumpProfile == "experimental") {
                confirmExperimentalPumpThenApply()
            }

            val chipParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            val gapParams = LinearLayout.LayoutParams(dp(6), ViewGroup.LayoutParams.WRAP_CONTENT)

            pumpRateRow.addView(slowBtn, chipParams)
            pumpRateRow.addView(space(dp(6)), gapParams)
            pumpRateRow.addView(mediumBtn, chipParams)
            pumpRateRow.addView(space(dp(6)), gapParams)
            pumpRateRow.addView(quickBtn, chipParams)
            pumpRateRow.addView(space(dp(6)), gapParams)
            pumpRateRow.addView(experimentalBtn, chipParams)

            val autoPumpTitle = TextView(this@MainActivity).apply {
                text = "Smart Pump Control"
                textSize = 14f
                setTextColor(textPrimary)
                setTypeface(typeface, Typeface.BOLD)
                setPadding(0, dp(14), 0, dp(4))
            }

            val autoPumpDesc = TextView(this@MainActivity).apply {
                text = "Dynamically adjusts pump speed based on device temperature."
                textSize = 12f
                setTextColor(textSecondary)
                setPadding(0, 0, 0, dp(8))
            }

            val autoPumpStatus = TextView(this@MainActivity).apply {
                textSize = 12f
                setTextColor(textSecondary)
                alpha = 0.95f
            }.also {
                smartPumpStatusView = it
            }

            val autoPumpSpeedInfo = TextView(this@MainActivity).apply {
                textSize = 12f
                setTextColor(textSecondary)
                setPadding(0, dp(2), 0, 0)
                alpha = 0.90f
            }.also {
                smartPumpSpeedView = it
            }

            fun setPumpManualControlsEnabled(enabled: Boolean) {
                val alphaValue = if (enabled) 1f else 0.40f

                slowBtn.isEnabled = enabled
                mediumBtn.isEnabled = enabled
                quickBtn.isEnabled = enabled
                experimentalBtn.isEnabled = enabled

                slowBtn.alpha = alphaValue
                mediumBtn.alpha = alphaValue
                quickBtn.alpha = alphaValue
                experimentalBtn.alpha = alphaValue
            }

            fun refreshAutoPumpUi() {
                if (autoPumpEnabled) {
                    val status = buildAutoPumpStatusText()
                    autoPumpStatus.text = status.first
                    autoPumpSpeedInfo.text = status.second
                    autoPumpStatus.visibility = View.VISIBLE
                    autoPumpSpeedInfo.visibility = View.VISIBLE
                    setPumpManualControlsEnabled(false)
                } else {
                    autoPumpStatus.text = "Pump Mode: MANUAL • ${pumpProfile.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }}"
                    val manualSpeed = when (pumpProfile.lowercase()) {
                        "slow" -> 40
                        "medium" -> 60
                        "quick" -> 80
                        "experimental" -> 90
                        else -> 60
                    }
                    autoPumpSpeedInfo.text = "Speed: $manualSpeed • Freq: 4"
                    autoPumpStatus.visibility = View.VISIBLE
                    autoPumpSpeedInfo.visibility = View.VISIBLE
                    setPumpManualControlsEnabled(true)
                }
                refreshSmartPumpStatusViews()
            }

            val autoPumpSwitch = android.widget.Switch(this@MainActivity).apply {
                isChecked = autoPumpEnabled
                setOnCheckedChangeListener { _, checked ->
                    autoPumpEnabled = checked
                    saveAutoPumpState()
                    if (checked) {
                        pumpEnabled = true
                        savePumpState()
                        HardwareController.setPumpProfile(pumpProfile)
                        startAutoPumpService()
                    } else {
                        stopAutoPumpService()
                    }
                    refreshAutoPumpUi()
                    refreshStatus()
                    refreshSmartPumpStatusViews()
                }
            }

            val autoPumpRow = LinearLayout(this@MainActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                addView(LinearLayout(this@MainActivity).apply {
                    orientation = LinearLayout.VERTICAL
                    addView(autoPumpTitle)
                    addView(autoPumpDesc)
                    addView(autoPumpStatus)
                    addView(autoPumpSpeedInfo)
                }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
                addView(autoPumpSwitch)
            }

            refreshAutoPumpUi()

            pumpSection.addView(circulationRow)
            pumpSection.addView(flowRateLabel)
            pumpSection.addView(pumpRateRow)
            pumpSection.addView(autoPumpRow)

            addView(pumpSection)
            addView(spacer(dp(16)))
            addView(sectionHeader("▦", "FAN CURVE"))
            addView(autoCurveCheck)
            addView(curveStatusText)
            addView(modeRow)
            addView(subtleLabel("Auto mode ramps fan by temperature and disables manual curve cards"))
            addView(subtleLabel("Quiet → low noise, stays between fan 0-1"))
            addView(subtleLabel("Balanced → moderate cooling, stays between fan 2-3"))
            addView(subtleLabel("Turbo → max cooling and sound, stays between fan 4-5"))
        }

        container.addView(coolingCard)

        return container
    }

    private fun applyFanLedPreviewIfEnabled() {
        if (!realTimePreviewEnabled) return
        if (fanLedEnabled) {
            applyFanLedSelection(fanLedEffect, fanLedColor)
        } else {
            HardwareController.setFanLedEnabled(false)
        }
    }

    private fun applyLogoLedPreviewIfEnabled() {
        if (!realTimePreviewEnabled) return
        if (logoLedEnabled) {
            HardwareController.setLogoLedEffect(logoLedEffect, logoLedColor)
        } else {
            HardwareController.setLogoLedEnabled(false)
        }
    }

    private fun applyShoulderLedPreviewIfEnabled() {
        if (!realTimePreviewEnabled) return
        if (shoulderLedEnabled) {
            HardwareController.setShoulderLedEffect(shoulderLedEffect, shoulderLedColor)
        } else {
            HardwareController.setShoulderLedEnabled(false)
        }
    }

    private fun createControlsTab(): LinearLayout {
        val container = scrollTabContainer()

        val rootCheckBtn = actionButton("CHECK ROOT") {
            val ok = RootShell.hasRoot()
            AlertDialog.Builder(this)
                .setTitle("Root Status")
                .setMessage(
                    if (ok) "Root access granted\n\nApp is running as root"
                    else "Root access NOT granted\n\nCheck your root manager"
                )
                .setPositiveButton("OK", null)
                .show()
            refreshStatus()
        }

        val refreshBtn = actionButton("REFRESH STATUS") {
            refreshStatus()
        }

        val systemCard = sectionPanel().apply {
            addView(sectionHeader("⚙", "SYSTEM"))
            addView(row(rootCheckBtn, refreshBtn))
        }

        val magicKeyStatusLabel = subtleLabel("Current: ${readMagicKeyModeLabel()}")
        magicKeyStatusLabelRef = magicKeyStatusLabel

        var sliderAppBtnRef: Button? = null

        val cameraBtn = smallActionButton("CAMERA") {
            applyStockMagicKeyMode("Camera", { HardwareController.setSliderOpenCamera() }, magicKeyStatusLabel, sliderAppBtnRef)
        }

        val gameSpaceBtn = smallActionButton("GAMESPACE") {
            applyStockMagicKeyMode("GameSpace", { HardwareController.setSliderOpenGameSpace() }, magicKeyStatusLabel, sliderAppBtnRef)
        }

        val soundModeBtn = smallActionButton("SOUND MODE") {
            applyStockMagicKeyMode("Sound Mode", { HardwareController.setSliderSoundMode() }, magicKeyStatusLabel, sliderAppBtnRef)
        }

        val flashlightBtn = smallActionButton("FLASHLIGHT") {
            applyStockMagicKeyMode("Flashlight", { HardwareController.setSliderFlashlight() }, magicKeyStatusLabel, sliderAppBtnRef)
        }

        val recorderBtn = smallActionButton("VOICE RECORDER") {
            applyStockMagicKeyMode("Voice Recorder", { HardwareController.setSliderVoiceRecorder() }, magicKeyStatusLabel, sliderAppBtnRef)
        }

        val stockFunctionsCard = sectionPanel().apply {
            addView(sectionHeader("⌘", "MAGIC KEY FUNCTIONS"))
            addView(bodyText("Choose one stock Magic Key action. Selecting a stock action disables app launch mode."))
            addView(magicKeyStatusLabel)
            addView(space(dp(8)))
            addView(flowRow(cameraBtn, gameSpaceBtn, soundModeBtn))
            addView(spacer(dp(8)))
            addView(flowRow(flashlightBtn, recorderBtn))
            addView(spacer(dp(8)))
            addView(singleRow(actionButton("DISABLE MAGIC KEY ACTION", isDanger = true) {
                disableMagicKeyMode(magicKeyStatusLabel, sliderAppBtnRef)
            }))
        }

        val sliderAppBtn = actionButton(
            "MAGIC KEY APP: ${resolveMagicKeyAppLabel(savedMagicKeyAppPackage())}"
        ) {}
        sliderAppBtnRef = sliderAppBtn
        sliderAppBtn.setOnClickListener {
            showMagicKeyAppPicker(sliderAppBtn)
        }

        val clearSliderAppBtn = actionButton("CLEAR APP SELECTION", isDanger = true) {
            disableMagicKeyMode(magicKeyStatusLabel, sliderAppBtn)
        }

        val sliderCard = sectionPanel().apply {
            addView(sectionHeader("↕", "SLIDER APP LAUNCH"))
            addView(bodyText("Choose one app for Magic Key launch mode. Selecting an app disables stock Magic Key functions."))
            addView(space(dp(12)))
            addView(singleRow(sliderAppBtn))
            addView(space(dp(12)))
            addView(singleRow(clearSliderAppBtn))
            addView(space(dp(4)))
            setPadding(dp(18), dp(18), dp(18), dp(26))
        }
        container.addView(systemCard)
        container.addView(stockFunctionsCard)
        container.addView(sliderCard)



        return container
    }

    private fun createHardwareTab(): LinearLayout {
        val container = scrollTabContainer()

        val configureTriggersBtn = actionButton("CONFIGURE TRIGGERS") {
            showTriggerSetupDialog()
        }

        val trigEnableBtn = actionButton("ENABLE TRIGGERS") {
            HardwareController.enableTriggers()
            startService(Intent(this, TriggerRootService::class.java))
            refreshStatus()
            Toast.makeText(this, "Triggers enabled", Toast.LENGTH_SHORT).show()
        }

        val triggerCard = sectionPanel().apply {
            addView(sectionHeader("⌥", "TRIGGERS"))
            addView(bodyText("Map shoulder triggers to quick actions or re-enable them if the system has disabled them."))
            addView(space(dp(10)))
            
            val hapticSwitch = android.widget.Switch(this@MainActivity).apply {
                val prefs = getSharedPreferences("triggers", MODE_PRIVATE)
                isChecked = prefs.getBoolean("haptics_enabled", true)

                setOnCheckedChangeListener { _, checked ->
                    prefs.edit().putBoolean("haptics_enabled", checked).apply()
                    Toast.makeText(
                        this@MainActivity,
                        "Haptics " + (if (checked) "enabled" else "disabled"),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            val hapticRow = LinearLayout(this@MainActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL

                addView(TextView(this@MainActivity).apply {
                    text = "Haptic feedback"
                    textSize = 14f
                    setTextColor(textPrimary)
                }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))

                addView(hapticSwitch)
            }

            addView(space(dp(8)))
            addView(hapticRow)


            val intentUnlockSwitch = android.widget.Switch(this@MainActivity).apply {
                val prefs = getSharedPreferences("triggers", MODE_PRIVATE)
                isChecked = prefs.getBoolean("intent_unlock_right_trigger", true)

                setOnCheckedChangeListener { _, checked ->
                    prefs.edit().putBoolean("intent_unlock_right_trigger", checked).apply()
                    Toast.makeText(
                        this@MainActivity,
                        "Intent Unlock Trigger " + (if (checked) "enabled" else "disabled"),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            val intentUnlockRow = LinearLayout(this@MainActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(0, dp(8), 0, 0)

                addView(TextView(this@MainActivity).apply {
                    text = "Intent Unlock Trigger"
                    textSize = 14f
                    setTextColor(textPrimary)
                }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))

                addView(intentUnlockSwitch)
            }

            addView(space(dp(8)))
            addView(intentUnlockRow)

            val autoStartSwitch = android.widget.Switch(this@MainActivity).apply {
                val prefs = getSharedPreferences("triggers", MODE_PRIVATE)
                isChecked = prefs.getBoolean("triggers_auto_start", false)

                setOnCheckedChangeListener { _, checked ->
                    prefs.edit().putBoolean("triggers_auto_start", checked).apply()

                    if (checked) {
                        HardwareController.enableTriggers()
                        startService(Intent(this@MainActivity, TriggerRootService::class.java))
                    }

                    Toast.makeText(
                        this@MainActivity,
                        "Auto-start triggers " + (if (checked) "enabled" else "disabled"),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            val autoStartRow = LinearLayout(this@MainActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(0, dp(8), 0, 0)

                addView(TextView(this@MainActivity).apply {
                    text = "Auto-start triggers"
                    textSize = 14f
                    setTextColor(textPrimary)
                }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))

                addView(autoStartSwitch)
            }

            val intentUnlockDesc = TextView(this@MainActivity).apply {
                text = "Prevents accidental touches. Double tap to activate the right trigger, then use it normally until it times out."
                textSize = 12f
                setTextColor(textSecondary)
                setPadding(0, dp(4), 0, dp(4))
            }

            addView(space(dp(4)))
            addView(intentUnlockDesc)

            addView(space(dp(8)))
            addView(autoStartRow)

            val autoStartDesc = TextView(this@MainActivity).apply {
                text = "Automatically enable triggers and start the service on boot or when the app launches."
                textSize = 12f
                setTextColor(textSecondary)
                setPadding(0, dp(4), 0, dp(4))
            }

            addView(space(dp(4)))
            addView(autoStartDesc)


addView(row(configureTriggersBtn, trigEnableBtn))
        }

        val vibrateBtn = actionButton("TEST HAPTIC") {
            HardwareController.vibrate(durationMs = 100, gain = 220)
            Toast.makeText(this, "Haptic test sent", Toast.LENGTH_SHORT).show()
        }

        val hapticsCard = sectionPanel().apply {
            addView(sectionHeader("≈", "HAPTICS"))
            addView(bodyText("Quick vibration test for hardware haptics."))
            addView(space(dp(10)))
            addView(singleRow(vibrateBtn))
        }

        container.addView(triggerCard)
        container.addView(hapticsCard)

        val profilesCard = sectionPanel().apply {
            addView(sectionHeader("★", "HARDWARE PROFILES"))
            addView(bodyText("Save and apply full hardware presets for fan, pump, LEDs, triggers, and haptics."))

            val profileList = LinearLayout(this@MainActivity).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(0, dp(10), 0, 0)
            }

            fun renderProfiles() {
                val profiles = ProfileManager.loadProfiles(this@MainActivity)
                ProfileDialogs.renderProfiles(
                    context = this@MainActivity,
                    profileList = profileList,
                    profiles = profiles,
                    subtleLabel = { text -> subtleLabel(text) },
                    actionButton = { text, isDanger, onClick -> actionButton(text, isDanger = isDanger, onClick = onClick) },
                    space = { value -> space(value) },
                    dp = { value -> dp(value) },
                    onApplyProfile = { profile ->
                        HardwareController.applyHardwareProfile(profile)
                        applyProfileToUiState(profile)
                        Toast.makeText(this@MainActivity, "Applied ${profile.name}", Toast.LENGTH_SHORT).show()
                    },
                    onDeleteProfile = { profile ->
                        showDeleteProfileDialog(profile.name) {
                            renderProfiles()
                        }
                    }
                )
            }

            val saveBtn = actionButton("SAVE CURRENT PROFILE") {
                showSaveProfileDialog {
                    renderProfiles()
                }
            }

            addView(singleRow(saveBtn))
            renderProfiles()
            addView(profileList)
        }

        container.addView(profilesCard)

        return container
    }

    private fun createLightingTab(): LinearLayout {
        val container = scrollTabContainer()

        val previewCard = sectionPanel().apply {
            addView(sectionHeader("⚡", "PREVIEW"))
            addView(TextView(this@MainActivity).apply {
                text = "Apply LED color/effect changes instantly while selecting"
                textSize = 13f
                setTextColor(textSecondary)
                setPadding(0, 0, 0, dp(10))
            })

            val previewSwitch = android.widget.Switch(this@MainActivity).apply {
                isChecked = realTimePreviewEnabled
                setOnCheckedChangeListener { _, checked ->
                    realTimePreviewEnabled = checked
                    saveRealTimePreviewEnabled(checked)
                }
            }

            val previewRow = LinearLayout(this@MainActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                addView(TextView(this@MainActivity).apply {
                    text = "Real-time LED preview"
                    textSize = 14f
                    setTextColor(textPrimary)
                }, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
                addView(previewSwitch)
            }

            addView(previewRow)
        }

        container.addView(previewCard)

        val fanLedCard = sectionPanel().apply {
            addView(sectionHeader("✦", "FAN LED"))

            val fanLedSummary = TextView(this@MainActivity).apply {
                text = "Customize fan LED effect and color"
                textSize = 13f
                setTextColor(textSecondary)
                setPadding(0, 0, 0, dp(10))
            }

            val customizeFanBtn = actionButton("CUSTOMIZE FAN LED") {
                showFanLedDialog()
            }
            addView(fanLedSummary)
            addView(singleRow(customizeFanBtn))
        }

        val logoCard = sectionPanel().apply {
            addView(sectionHeader("◈", "LOGO LED"))

            val logoSummary = TextView(this@MainActivity).apply {
                text = "Customize logo LED effect and color"
                textSize = 13f
                setTextColor(textSecondary)
                setPadding(0, 0, 0, dp(10))
            }

            val customizeLogoBtn = actionButton("CUSTOMIZE LOGO LED") {
                showLogoLedDialog()
            }

            addView(logoSummary)
            addView(singleRow(customizeLogoBtn))
        }

        val shoulderCard = sectionPanel().apply {
            addView(sectionHeader("⟫", "SHOULDER LEDS"))

            val shoulderSummary = TextView(this@MainActivity).apply {
                text = "Customize shoulder LED strips effect and color"
                textSize = 13f
                setTextColor(textSecondary)
                setPadding(0, 0, 0, dp(10))
            }

            val customizeShoulderBtn = actionButton("CUSTOMIZE SHOULDER LEDS") {
                showShoulderLedDialog()
            }

            addView(shoulderSummary)
            addView(singleRow(customizeShoulderBtn))
        }

        container.addView(fanLedCard)
        container.addView(logoCard)
        container.addView(shoulderCard)


        val gameModeStatusText = TextView(this).apply {
            text = "Status: Inactive"
            textSize = 13f
            setTextColor(textSecondary)
            setPadding(0, dp(6), 0, dp(4))
        }

        val gameModeAppsText = TextView(this).apply {
            text = gameModeAppsSummary()
            textSize = 13f
            setTextColor(textSecondary)
            setPadding(0, dp(2), 0, dp(10))
        }.also { gameModeAppsTextRef = it }

        val chooseGamesBtn = actionButton("CHOOSE GAMES") {
            showGameModeAppPicker()
        }

        val editGameProfileBtn = actionButton("EDIT GAME PROFILE") {
            showGameModeProfileDialog()
        }

        val gameModeCard = sectionPanel().apply {
            addView(sectionHeader("🎮", "GAME MODE"))
            addView(bodyText("Automatically applies a separate hardware profile when a selected game is launched. When the game closes or goes to background, your normal profile remains in control."))
            addView(gameModeStatusText)
            addView(infoRow("Apps", gameModeAppsText))
            addView(row(chooseGamesBtn, editGameProfileBtn))
            addView(subtleLabel("This is automatic behavior only. There is no manual toggle here."))
        }

        container.addView(gameModeCard)
        return container
    }



    private var dialogRefreshLogoLed: (() -> Unit)? = null


    private var dialogRefreshShoulderLed: (() -> Unit)? = null


    private var dialogRefreshPump: (() -> Unit)? = null
    private var magicKeyStatusLabelRef: TextView? = null



    private fun showTriggerSetupDialog() {
        val prefs = getSharedPreferences("triggers", MODE_PRIVATE)

        val labels = arrayOf(
            "None",
            "Volume Up",
            "Volume Down",
            "Play / Pause",
            "Next Track",
            "Previous Track"
        )
        val values = arrayOf(
            "NONE",
            "VOL_UP",
            "VOL_DOWN",
            "PLAY_PAUSE",
            "NEXT",
            "PREVIOUS"
        )

        fun indexOfValue(value: String): Int {
            val i = values.indexOf(value)
            return if (i >= 0) i else 0
        }

        var leftChoice = indexOfValue(prefs.getString("left_trigger", "VOL_DOWN") ?: "VOL_DOWN")
        var rightChoice = indexOfValue(prefs.getString("right_trigger", "VOL_UP") ?: "VOL_UP")

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(22), dp(18), dp(22), dp(12))
            background = roundedBg(panelColor, borderColor, 22)
        }

        val titleView = TextView(this).apply {
            text = "Trigger Mapping"
            textSize = 20f
            setTextColor(textPrimary)
            setTypeface(typeface, Typeface.BOLD)
        }

        val subtitleView = TextView(this).apply {
            text = "Set left and right shoulder triggers independently."
            textSize = 13f
            setTextColor(textSecondary)
            setPadding(0, dp(8), 0, dp(12))
        }

        val leftLabel = TextView(this).apply {
            text = "Left Trigger (F7)"
            textSize = 13f
            setTextColor(textSecondary)
            setPadding(0, 0, 0, dp(6))
        }

        val leftGroup = android.widget.RadioGroup(this).apply {
            orientation = android.widget.RadioGroup.VERTICAL
        }

        labels.forEachIndexed { index, label ->
            leftGroup.addView(android.widget.RadioButton(this).apply {
                text = label
                textSize = 14f
                setTextColor(textPrimary)
                buttonTintList = android.content.res.ColorStateList.valueOf(accent)
                isChecked = index == leftChoice
                setOnCheckedChangeListener { _, checked ->
                    if (checked) leftChoice = index
                }
            })
        }

        val rightLabel = TextView(this).apply {
            text = "Right Trigger (F8)"
            textSize = 13f
            setTextColor(textSecondary)
            setPadding(0, dp(14), 0, dp(6))
        }

        val rightGroup = android.widget.RadioGroup(this).apply {
            orientation = android.widget.RadioGroup.VERTICAL
        }

        labels.forEachIndexed { index, label ->
            rightGroup.addView(android.widget.RadioButton(this).apply {
                text = label
                textSize = 14f
                setTextColor(textPrimary)
                buttonTintList = android.content.res.ColorStateList.valueOf(accent)
                isChecked = index == rightChoice
                setOnCheckedChangeListener { _, checked ->
                    if (checked) rightChoice = index
                }
            })
        }

        val buttonRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.END
            setPadding(0, dp(18), 0, 0)
        }

        val cancelBtn = Button(this).apply {
            text = "Cancel"
            textSize = 13f
            setAllCaps(false)
            setTextColor(textPrimary)
            background = roundedFill(Color.parseColor("#1E2633"), 14)
            setPadding(dp(18), dp(10), dp(18), dp(10))
        }

        val saveBtn = Button(this).apply {
            text = "Save"
            textSize = 13f
            setAllCaps(false)
            setTextColor(textPrimary)
            background = roundedFill(panelPressed, 14)
            setPadding(dp(20), dp(10), dp(20), dp(10))
        }

        buttonRow.addView(cancelBtn)
        buttonRow.addView(space(dp(10)))
        buttonRow.addView(saveBtn)

        container.addView(titleView)
        container.addView(subtitleView)
        container.addView(leftLabel)
        container.addView(leftGroup)
        container.addView(rightLabel)
        container.addView(rightGroup)
        container.addView(buttonRow)

        val scroll = ScrollView(this).apply {
            isFillViewport = true
            addView(
                container,
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            )
        }

        val dialog = AlertDialog.Builder(this)
            .setView(scroll)
            .setCancelable(true)
            .create()

        cancelBtn.setOnClickListener {
            dialog.dismiss()
        }

        saveBtn.setOnClickListener {
            prefs.edit()
                .putString("left_trigger", values[leftChoice])
                .putString("right_trigger", values[rightChoice])
                .apply()

            Toast.makeText(
                this,
                "Saved: Left = ${labels[leftChoice]}, Right = ${labels[rightChoice]}",
                Toast.LENGTH_SHORT
            ).show()

            dialog.dismiss()
        }

        dialog.show()
        dialog.window?.apply {
            setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))
            setDimAmount(0.65f)
        }
    }


    private fun initDefaultTriggerMappings() {
        val p = getSharedPreferences("triggers", MODE_PRIVATE)
        if (!p.contains("left_trigger")) {
            p.edit()
                .putString("left_trigger", "VOL_DOWN")
                .putString("right_trigger", "VOL_UP")
                .apply()
        }
    }

    private fun showGameModeProfileDialog() {
        GameModeUi.showGameModeProfileDialog(
            activity = this,
            current = getSavedGameModeProfile(),
            deps = GameModeUi.Deps(
                textPrimary = textPrimary,
                textSecondary = textSecondary,
                panelColor = panelColor,
                borderColor = borderColor,
                panelPressed = panelPressed,
                accent = accent,
                typeface = typeface,
                dp = { value -> dp(value) },
                roundedBg = { fill, stroke, radius -> roundedBg(fill, stroke, radius) },
                roundedFill = { color, radius -> roundedFill(color, radius) },
                filterChip = { label, selected, onClick -> filterChip(label, selected, onClick) },
                space = { value -> space(value) },
                colorDotDrawable = { hex, selected -> colorDotDrawable(hex, selected) },
                colorDotGeneric = { hex, selected, onClick -> colorDotGeneric(hex, selected, onClick) },
            ),
            onSaveProfile = { profile -> saveGameModeProfile(profile) }
        )
    }

    private fun showPumpProfileDialog() {
        PumpDialogUi.showPumpProfileDialog(
            activity = this,
            originalEnabled = pumpEnabled,
            originalProfile = pumpProfile,
            currentProfile = { pumpProfile },
            setPumpEnabled = { value -> pumpEnabled = value },
            setPumpProfile = { value -> pumpProfile = value },
            applyHardwareProfile = { value -> HardwareController.setPumpProfile(value) },
            disablePump = { HardwareController.enablePump(false) },
            savePumpState = { savePumpState() },
            confirmExperimentalPumpThenApply = { confirmExperimentalPumpThenApply() },
            setDialogRefreshPump = { callback -> dialogRefreshPump = callback },
            deps = PumpDialogUi.Deps(
                textPrimary = textPrimary,
                textSecondary = textSecondary,
                panelColor = panelColor,
                borderColor = borderColor,
                panelPressed = panelPressed,
                typeface = typeface,
                dp = { value -> dp(value) },
                roundedBg = { fill, stroke, radius -> roundedBg(fill, stroke, radius) },
                roundedFill = { color, radius -> roundedFill(color, radius) },
                filterChip = { label, selected, onClick -> filterChip(label, selected, onClick) },
                space = { value -> space(value) }
            )
        )
    }

    private fun showShoulderLedDialog() {
        ShoulderLedDialogUi.showShoulderLedDialog(
            activity = this,
            originalEnabled = shoulderLedEnabled,
            originalEffect = shoulderLedEffect,
            originalColor = shoulderLedColor,
            currentEnabled = { shoulderLedEnabled },
            currentEffect = { shoulderLedEffect },
            currentColor = { shoulderLedColor },
            setEnabled = { value -> shoulderLedEnabled = value },
            setEffect = { value -> shoulderLedEffect = value },
            setColor = { value -> shoulderLedColor = value },
            applyPreviewIfEnabled = { applyShoulderLedPreviewIfEnabled() },
            applyEffect = { effect, color -> HardwareController.setShoulderLedEffect(effect, color) },
            disableLed = { HardwareController.setShoulderLedEnabled(false) },
            saveState = { saveShoulderLedState() },
            startFanLedService = { startFanLedService() },
            stopFanLedService = { stopFanLedService() },
            anyLedEnabled = { fanLedEnabled || logoLedEnabled || shoulderLedEnabled },
            setDialogRefresh = { callback -> dialogRefreshShoulderLed = callback },
            deps = ShoulderLedDialogUi.Deps(
                textPrimary = textPrimary,
                textSecondary = textSecondary,
                panelColor = panelColor,
                borderColor = borderColor,
                panelPressed = panelPressed,
                accent = accent,
                typeface = typeface,
                dp = { value -> dp(value) },
                roundedBg = { fill, stroke, radius -> roundedBg(fill, stroke, radius) },
                roundedFill = { color, radius -> roundedFill(color, radius) },
                space = { value -> space(value) },
                filterChip = { label, selected, onClick -> filterChip(label, selected, onClick) },
                colorDotGeneric = { hex, selected, onClick -> colorDotGeneric(hex, selected, onClick) },
                colorDotDrawable = { hex, selected -> colorDotDrawable(hex, selected) }
            )
        )
    }

    private fun showLogoLedDialog() {
        val originalEnabled = logoLedEnabled
        val originalEffect = logoLedEffect
        val originalColor = logoLedColor

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(22), dp(18), dp(22), dp(12))
            background = roundedBg(panelColor, borderColor, 22)
        }

        val titleView = TextView(this).apply {
            text = "Logo LED"
            textSize = 20f
            setTextColor(textPrimary)
            setTypeface(typeface, Typeface.BOLD)
        }

        val subtitleView = TextView(this).apply {
            text = "Customize logo LED with instant preview"
            textSize = 13f
            setTextColor(textSecondary)
            setPadding(0, dp(8), 0, 0)
        }

        val enableCheck = CheckBox(this).apply {
            text = "Enable logo light"
            isChecked = logoLedEnabled
            textSize = 14f
            setTextColor(textPrimary)
            buttonTintList = android.content.res.ColorStateList.valueOf(accent)
            setPadding(0, dp(14), 0, 0)
            setOnCheckedChangeListener { _, checked ->
                logoLedEnabled = checked
                if (checked) {
                    HardwareController.setLogoLedEffect(logoLedEffect, logoLedColor)
                } else {
                    HardwareController.setLogoLedEnabled(false)
                }
            }
        }

        val effectLabel = TextView(this).apply {
            text = "Effect"
            textSize = 12f
            setTextColor(textSecondary)
            setPadding(0, dp(16), 0, dp(8))
        }

        val effectsRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val steadyBtn = filterChip("Steady", logoLedEffect == "steady") {
            logoLedEffect = "steady"
            if (logoLedEnabled) {
                HardwareController.setLogoLedEffect(logoLedEffect, logoLedColor)
            }
            dialogRefreshLogoLed?.invoke()
        }

        val breatheBtn = filterChip("Breathe", logoLedEffect == "breathe") {
            logoLedEffect = "breathe"
            if (logoLedEnabled) {
                HardwareController.setLogoLedEffect(logoLedEffect, logoLedColor)
            }
            dialogRefreshLogoLed?.invoke()
        }

        val flashingBtn = filterChip("Flashing", logoLedEffect == "flashing") {
            logoLedEffect = "flashing"
            if (logoLedEnabled) {
                HardwareController.setLogoLedEffect(logoLedEffect, logoLedColor)
            }
            dialogRefreshLogoLed?.invoke()
        }

        effectsRow.addView(steadyBtn)
        effectsRow.addView(space(dp(8)))
        effectsRow.addView(breatheBtn)
        effectsRow.addView(space(dp(8)))
        effectsRow.addView(flashingBtn)

        val colorLabel = TextView(this).apply {
            text = "Color"
            textSize = 12f
            setTextColor(textSecondary)
            setPadding(0, dp(16), 0, dp(10))
        }

        val colorRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            addView(colorDotGeneric("#FF0000", logoLedColor == 1) {
                logoLedColor = 1
                applyLogoLedPreviewIfEnabled()
                dialogRefreshLogoLed?.invoke()
            })
            addView(space(dp(10)))
            addView(colorDotGeneric("#FF8C00", logoLedColor == 3) {
                logoLedColor = 3
                applyLogoLedPreviewIfEnabled()
                dialogRefreshLogoLed?.invoke()
            })
            addView(space(dp(10)))
            addView(colorDotGeneric("#FFD600", logoLedColor == 4) {
                logoLedColor = 4
                applyLogoLedPreviewIfEnabled()
                dialogRefreshLogoLed?.invoke()
            })
            addView(space(dp(10)))
            addView(colorDotGeneric("#00E676", logoLedColor == 5) {
                logoLedColor = 5
                applyLogoLedPreviewIfEnabled()
                dialogRefreshLogoLed?.invoke()
            })
        }

        val colorRow2 = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, dp(10), 0, 0)
            addView(colorDotGeneric("#00E5FF", logoLedColor == 6) {
                logoLedColor = 6
                applyLogoLedPreviewIfEnabled()
                dialogRefreshLogoLed?.invoke()
            })
            addView(space(dp(10)))
            addView(colorDotGeneric("#1565FF", logoLedColor == 7) {
                logoLedColor = 7
                applyLogoLedPreviewIfEnabled()
                dialogRefreshLogoLed?.invoke()
            })
            addView(space(dp(10)))
            addView(colorDotGeneric("#A020F0", logoLedColor == 8) {
                logoLedColor = 8
                applyLogoLedPreviewIfEnabled()
                dialogRefreshLogoLed?.invoke()
            })
            addView(space(dp(10)))
            addView(colorDotGeneric("#FF69B4", logoLedColor == 9) {
                logoLedColor = 9
                applyLogoLedPreviewIfEnabled()
                dialogRefreshLogoLed?.invoke()
            })
        }

        val buttonRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.END
            setPadding(0, dp(18), 0, 0)
        }

        val cancelBtn = Button(this).apply {
            text = "Cancel"
            textSize = 13f
            setAllCaps(false)
            setTextColor(textPrimary)
            background = roundedFill(Color.parseColor("#1E2633"), 14)
            setPadding(dp(18), dp(10), dp(18), dp(10))
        }

        val saveBtn = Button(this).apply {
            text = "Save"
            textSize = 13f
            setAllCaps(false)
            setTextColor(textPrimary)
            background = roundedFill(panelPressed, 14)
            setPadding(dp(20), dp(10), dp(20), dp(10))
        }

        buttonRow.addView(cancelBtn)
        buttonRow.addView(space(dp(10)))
        buttonRow.addView(saveBtn)

        container.addView(titleView)
        container.addView(subtitleView)
        container.addView(enableCheck)
        container.addView(effectLabel)
        container.addView(effectsRow)
        container.addView(colorLabel)
        container.addView(colorRow)
        container.addView(colorRow2)
        container.addView(buttonRow)

        val dialog = AlertDialog.Builder(this)
            .setView(container)
            .setCancelable(true)
            .create()

        dialog.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))

        cancelBtn.setOnClickListener {
            logoLedEnabled = originalEnabled
            logoLedEffect = originalEffect
            logoLedColor = originalColor

            if (logoLedEnabled) {
                HardwareController.setLogoLedEffect(logoLedEffect, logoLedColor)
            } else {
                HardwareController.setLogoLedEnabled(false)
            }

            dialog.dismiss()
        }

        saveBtn.setOnClickListener {
            saveLogoLedState()
            if (logoLedEnabled) {
                HardwareController.setLogoLedEffect(logoLedEffect, logoLedColor)
            } else {
                HardwareController.setLogoLedEnabled(false)
            }
            if (fanLedEnabled || logoLedEnabled || shoulderLedEnabled) {
                startFanLedService()
            } else {
                stopFanLedService()
            }
            dialog.dismiss()
        }

        dialog.setOnCancelListener {
            logoLedEnabled = originalEnabled
            logoLedEffect = originalEffect
            logoLedColor = originalColor

            if (logoLedEnabled) {
                HardwareController.setLogoLedEffect(logoLedEffect, logoLedColor)
            } else {
                HardwareController.setLogoLedEnabled(false)
            }
        }

        fun repaint() {
            steadyBtn.background = roundedFill(
                if (logoLedEffect == "steady") panelPressed else Color.parseColor("#1E2633"),
                999
            )
            breatheBtn.background = roundedFill(
                if (logoLedEffect == "breathe") panelPressed else Color.parseColor("#1E2633"),
                999
            )
        }

        fun updateColorDots() {
            (colorRow.getChildAt(0) as View).background = colorDotDrawable("#FF0000", logoLedColor == 1)
            (colorRow.getChildAt(2) as View).background = colorDotDrawable("#FF8C00", logoLedColor == 3)
            (colorRow.getChildAt(4) as View).background = colorDotDrawable("#FFD600", logoLedColor == 4)
            (colorRow.getChildAt(6) as View).background = colorDotDrawable("#00E676", logoLedColor == 5)

            (colorRow2.getChildAt(0) as View).background = colorDotDrawable("#00E5FF", logoLedColor == 6)
            (colorRow2.getChildAt(2) as View).background = colorDotDrawable("#1565FF", logoLedColor == 7)
            (colorRow2.getChildAt(4) as View).background = colorDotDrawable("#A020F0", logoLedColor == 8)
            (colorRow2.getChildAt(6) as View).background = colorDotDrawable("#FF69B4", logoLedColor == 9)
        }

        fun refreshUi() {
            repaint()
            updateColorDots()
        }

        dialogRefreshLogoLed = { refreshUi() }

        refreshUi()
        dialog.show()

        dialog.window?.apply {
            setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))
            setDimAmount(0.65f)
        }
    }

    private fun showFanLedDialog() {
        val originalEnabled = fanLedEnabled
        val originalEffect = fanLedEffect
        val originalColor = fanLedColor

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(22), dp(18), dp(22), dp(12))
            background = roundedBg(panelColor, borderColor, 22)
        }

        val titleView = TextView(this).apply {
            text = "Fan LED"
            textSize = 20f
            setTextColor(textPrimary)
            setTypeface(typeface, Typeface.BOLD)
        }

        val subtitleView = TextView(this).apply {
            text = "Confirmed working options for fan LED"
            textSize = 13f
            setTextColor(textSecondary)
            setPadding(0, dp(8), 0, 0)
        }

        val enableCheck = CheckBox(this).apply {
            text = "Enable fan light"
            isChecked = fanLedEnabled
            textSize = 14f
            setTextColor(textPrimary)
            buttonTintList = android.content.res.ColorStateList.valueOf(accent)
            setPadding(0, dp(14), 0, 0)
            setOnCheckedChangeListener { _, checked ->
                fanLedEnabled = checked
                if (checked) {
                    applyFanLedSelection(fanLedEffect, fanLedColor)
                } else {
                    HardwareController.setFanLedEnabled(false)
                }
            }
        }

        val effectLabel = TextView(this).apply {
            text = "Effect"
            textSize = 12f
            setTextColor(textSecondary)
            setPadding(0, dp(16), 0, dp(8))
        }

        val effectsRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val steadyBtn = filterChip("Steady", fanLedEffect == "steady") {
            fanLedEffect = "steady"
            applyFanLedPreviewIfEnabled()
            dialogRefreshFanLed?.invoke()
        }

        val breatheBtn = filterChip("Breathe", fanLedEffect == "breathe") {
            fanLedEffect = "breathe"
            applyFanLedPreviewIfEnabled()
            dialogRefreshFanLed?.invoke()
        }

        val flashingBtn = filterChip("Flashing", fanLedEffect == "flashing") {
            fanLedEffect = "flashing"
            applyFanLedPreviewIfEnabled()
            dialogRefreshFanLed?.invoke()
        }

        effectsRow.addView(steadyBtn)
        effectsRow.addView(space(dp(8)))
        effectsRow.addView(breatheBtn)
        effectsRow.addView(space(dp(8)))
        effectsRow.addView(flashingBtn)

        val colorLabel = TextView(this).apply {
            text = "Color"
            textSize = 12f
            setTextColor(textSecondary)
            setPadding(0, dp(16), 0, dp(10))
        }

        val colorRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            addView(colorDot(1, "#FF0000") {
                fanLedColor = 1
                if (fanLedEffect.startsWith("preset:")) fanLedEffect = "steady"
                applyFanLedPreviewIfEnabled()
                dialogRefreshFanLed?.invoke()
            })
            addView(space(dp(10)))
            addView(colorDot(3, "#FF8C00") {
                fanLedColor = 3
                if (fanLedEffect.startsWith("preset:")) fanLedEffect = "steady"
                applyFanLedPreviewIfEnabled()
                dialogRefreshFanLed?.invoke()
            })
            addView(space(dp(10)))
            addView(colorDot(4, "#FFD600") {
                fanLedColor = 4
                if (fanLedEffect.startsWith("preset:")) fanLedEffect = "steady"
                applyFanLedPreviewIfEnabled()
                dialogRefreshFanLed?.invoke()
            })
            addView(space(dp(10)))
            addView(colorDot(5, "#00E676") {
                fanLedColor = 5
                if (fanLedEffect.startsWith("preset:")) fanLedEffect = "steady"
                applyFanLedPreviewIfEnabled()
                dialogRefreshFanLed?.invoke()
            })
        }

        val colorRow2 = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, dp(10), 0, 0)
            addView(colorDot(6, "#00E5FF") {
                fanLedColor = 6
                if (fanLedEffect.startsWith("preset:")) fanLedEffect = "steady"
                applyFanLedPreviewIfEnabled()
                dialogRefreshFanLed?.invoke()
            })
            addView(space(dp(10)))
            addView(colorDot(7, "#1565FF") {
                fanLedColor = 7
                if (fanLedEffect.startsWith("preset:")) fanLedEffect = "steady"
                applyFanLedPreviewIfEnabled()
                dialogRefreshFanLed?.invoke()
            })
            addView(space(dp(10)))
            addView(colorDot(8, "#A020F0") {
                fanLedColor = 8
                if (fanLedEffect.startsWith("preset:")) fanLedEffect = "steady"
                applyFanLedPreviewIfEnabled()
                dialogRefreshFanLed?.invoke()
            })
            addView(space(dp(10)))
            addView(colorDot(9, "#FF69B4") {
                fanLedColor = 9
                if (fanLedEffect.startsWith("preset:")) fanLedEffect = "steady"
                applyFanLedPreviewIfEnabled()
                dialogRefreshFanLed?.invoke()
            })
        }

        val buttonRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.END
            setPadding(0, dp(18), 0, 0)
        }

        val cancelBtn = Button(this).apply {
            text = "Cancel"
            textSize = 13f
            setAllCaps(false)
            setTextColor(textPrimary)
            background = roundedFill(Color.parseColor("#1E2633"), 14)
            setPadding(dp(18), dp(10), dp(18), dp(10))
        }

        val saveBtn = Button(this).apply {
            text = "Save"
            textSize = 13f
            setAllCaps(false)
            setTextColor(textPrimary)
            background = roundedFill(panelPressed, 14)
            setPadding(dp(20), dp(10), dp(20), dp(10))
        }

        buttonRow.addView(cancelBtn)
        buttonRow.addView(space(dp(10)))
        buttonRow.addView(saveBtn)

        container.addView(titleView)
        container.addView(subtitleView)
        container.addView(enableCheck)
        container.addView(effectLabel)
        container.addView(effectsRow)
        val presetBubbleRow1 = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, dp(10), 0, 0)
            addView(fanPresetBubble("#FF69B4", "#FF0000", "#FF8C00", "#FF8C00", presetValue = "0x3002101") { applyFanPreset("0x3002101") })
            addView(space(dp(10)))
            addView(fanPresetBubble("#1565FF", "#00E676", "#22D3EE", "#FF69B4", presetValue = "0x3002102") { applyFanPreset("0x3002102") })
            addView(space(dp(10)))
            addView(fanPresetBubble("#22D3EE", "#FF0000", "#FFD600", "#FF69B4", presetValue = "0x3002103") { applyFanPreset("0x3002103") })
            addView(space(dp(10)))
            addView(fanPresetBubble("#00E676", "#FF69B4", "#FF8C00", "#22D3EE", presetValue = "0x3002104") { applyFanPreset("0x3002104") })
        }

        val presetBubbleRow2 = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, dp(10), 0, 0)
            addView(fanPresetBubble("#00E676", "#A020F0", "#FF8C00", "#FF69B4", presetValue = "0x3002105") { applyFanPreset("0x3002105") })
            addView(space(dp(10)))
            addView(fanPresetBubble("#FF0000", "#FF0000", "#FF0000", "#FF0000", presetValue = "0x3002106") { applyFanPreset("0x3002106") })
            addView(space(dp(10)))
            addView(fanPresetBubble("#22D3EE", "#FF8C00", "#22D3EE", "#A020F0", presetValue = "0x3002107") { applyFanPreset("0x3002107") })
            addView(space(dp(10)))
            addView(fanPresetBubble("#22D3EE", "#FF0000", "#FF8C00", "#00E676", presetValue = "0x3002108") { applyFanPreset("0x3002108") })
        }

        container.addView(colorLabel)
        container.addView(colorRow)
        container.addView(colorRow2)
        container.addView(presetBubbleRow1)
        container.addView(presetBubbleRow2)
        container.addView(buttonRow)

        val dialog = AlertDialog.Builder(this)
            .setView(container)
            .setCancelable(true)
            .create()

        dialog.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))

        cancelBtn.setOnClickListener {
            fanLedEnabled = originalEnabled
            fanLedEffect = originalEffect
            fanLedColor = originalColor

            if (fanLedEnabled) {
                applyFanLedSelection(fanLedEffect, fanLedColor)
            } else {
                HardwareController.setFanLedEnabled(false)
            }

            dialog.dismiss()
        }

        saveBtn.setOnClickListener {
            saveFanLedState()
            if (fanLedEnabled) {
                applyFanLedSelection(fanLedEffect, fanLedColor)
            } else {
                HardwareController.setFanLedEnabled(false)
            }
            if (fanLedEnabled || logoLedEnabled || shoulderLedEnabled) {
                startFanLedService()
            } else {
                stopFanLedService()
            }
            dialog.dismiss()
        }

        dialog.setOnCancelListener {
            fanLedEnabled = originalEnabled
            fanLedEffect = originalEffect
            fanLedColor = originalColor

            if (fanLedEnabled) {
                applyFanLedSelection(fanLedEffect, fanLedColor)
            } else {
                HardwareController.setFanLedEnabled(false)
            }
        }

        fun repaint() {
            steadyBtn.background = roundedFill(
                if (fanLedEffect == "steady") panelPressed else Color.parseColor("#1E2633"),
                999
            )
            breatheBtn.background = roundedFill(
                if (fanLedEffect == "breathe") panelPressed else Color.parseColor("#1E2633"),
                999
            )
        }

        fun updateColorDots() {
            (colorRow.getChildAt(0) as View).background = colorDotDrawable("#FF0000", fanLedColor == 1)
            (colorRow.getChildAt(2) as View).background = colorDotDrawable("#FF8C00", fanLedColor == 3)
            (colorRow.getChildAt(4) as View).background = colorDotDrawable("#FFD600", fanLedColor == 4)
            (colorRow.getChildAt(6) as View).background = colorDotDrawable("#00E676", fanLedColor == 5)

            (colorRow2.getChildAt(0) as View).background = colorDotDrawable("#00E5FF", fanLedColor == 6)
            (colorRow2.getChildAt(2) as View).background = colorDotDrawable("#1565FF", fanLedColor == 7)
            (colorRow2.getChildAt(4) as View).background = colorDotDrawable("#A020F0", fanLedColor == 8)
            (colorRow2.getChildAt(6) as View).background = colorDotDrawable("#FF69B4", fanLedColor == 9)
        }

        repaint()
        updateColorDots()

        fun refreshUi() {
            repaint()
            updateColorDots()
            presetBubbleRow1.invalidate()
            presetBubbleRow2.invalidate()
            for (i in 0 until presetBubbleRow1.childCount) {
                presetBubbleRow1.getChildAt(i).invalidate()
            }
            for (i in 0 until presetBubbleRow2.childCount) {
                presetBubbleRow2.getChildAt(i).invalidate()
            }
        }

        dialogRefreshFanLed = { refreshUi() }

        dialog.show()

        dialog.window?.apply {
            setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))
            setDimAmount(0.65f)
        }
    }

    private var dialogRefreshFanLed: (() -> Unit)? = null

    private fun filterChip(label: String, selected: Boolean, onClick: () -> Unit): Button {
        return Button(this).apply {
            text = label
            textSize = 11f
            setAllCaps(false)
            maxLines = 1
            ellipsize = android.text.TextUtils.TruncateAt.END
            isSingleLine = true
            minWidth = 0
            minimumWidth = 0
            setTextColor(textPrimary)
            background = roundedFill(
                if (selected) panelPressed else Color.parseColor("#1E2633"),
                16
            )
            setPadding(dp(10), dp(6), dp(10), dp(6))
            setOnClickListener { onClick() }
            applyPressEffect(this)
        }
    }


    private fun applyFanPreset(effectValue: String) {
        fanLedEnabled = true
        fanLedEffect = "preset:$effectValue"
        fanLedColor = -1

        HardwareController.setFanLedStockPreset(effectValue)
        dialogRefreshFanLed?.invoke()
    }

    private fun fanPresetBubble(
        vararg hexes: String,
        presetValue: String,
        onClick: () -> Unit
    ): View {
        require(hexes.size == 4) { "fanPresetBubble requires exactly 4 colors" }

        return object : View(this) {
            private val fillPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)
            private val ringPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                style = android.graphics.Paint.Style.STROKE
                strokeWidth = dp(3).toFloat()
            }

            init {
                val size = dp(42)
                layoutParams = LinearLayout.LayoutParams(size, size)
                isClickable = true
                isFocusable = true
                setOnClickListener { onClick() }
            }

            override fun onDraw(canvas: android.graphics.Canvas) {
                super.onDraw(canvas)

                val pad = dp(3).toFloat()
                val rect = android.graphics.RectF(
                    pad,
                    pad,
                    width.toFloat() - pad,
                    height.toFloat() - pad
                )

                val saveCount = canvas.save()
                val clipPath = android.graphics.Path().apply {
                    addOval(rect, android.graphics.Path.Direction.CW)
                }
                canvas.clipPath(clipPath)

                val midX = rect.centerX()
                val midY = rect.centerY()

                fillPaint.color = Color.parseColor(hexes[0])
                canvas.drawRect(rect.left, rect.top, midX, midY, fillPaint)

                fillPaint.color = Color.parseColor(hexes[1])
                canvas.drawRect(midX, rect.top, rect.right, midY, fillPaint)

                fillPaint.color = Color.parseColor(hexes[2])
                canvas.drawRect(rect.left, midY, midX, rect.bottom, fillPaint)

                fillPaint.color = Color.parseColor(hexes[3])
                canvas.drawRect(midX, midY, rect.right, rect.bottom, fillPaint)

                canvas.restoreToCount(saveCount)

                ringPaint.color = if (fanLedEffect == "preset:$presetValue") {
                    Color.WHITE
                } else {
                    Color.TRANSPARENT
                }
                canvas.drawOval(rect, ringPaint)
            }
        }
    }

    private fun colorDot(colorId: Int, hex: String, onClick: () -> Unit): View {
        return View(this).apply {
            val size = dp(42)
            layoutParams = LinearLayout.LayoutParams(size, size)
            background = colorDotDrawable(hex, fanLedColor == colorId)
            setOnClickListener { onClick() }
        }
    }

    private fun colorDotDrawable(hex: String, selected: Boolean): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(Color.parseColor(hex))
            setStroke(dp(3), if (selected) Color.WHITE else Color.TRANSPARENT)
        }
    }

    private fun colorDotGeneric(hex: String, selected: Boolean, onClick: () -> Unit): View {
        return View(this).apply {
            val size = dp(42)
            layoutParams = LinearLayout.LayoutParams(size, size)
            background = colorDotDrawable(hex, selected)
            setOnClickListener { onClick() }
        }
    }


    private fun switchTab(tab: String) {
        homeTab.visibility = if (tab == "home") View.VISIBLE else View.GONE
        coolingTab.visibility = if (tab == "cooling") View.VISIBLE else View.GONE
        controlsTab.visibility = if (tab == "controls") View.VISIBLE else View.GONE
        val hardwareTab = (controlsTab.parent as ViewGroup).getChildAt(3)
        hardwareTab.visibility = if (tab == "hardware") View.VISIBLE else View.GONE
        lightingTab.visibility = if (tab == "lighting") View.VISIBLE else View.GONE

        setNavSelected(homeNav, tab == "home")
        setNavSelected(coolingNav, tab == "cooling")
        setNavSelected(controlsNav, tab == "controls")
        val hardwareNav = (controlsNav.parent as LinearLayout).getChildAt(3) as LinearLayout
        setNavSelected(hardwareNav, tab == "hardware")
        setNavSelected(lightingNav, tab == "lighting")
    }

    private fun setNavSelected(nav: LinearLayout, selected: Boolean) {
        nav.background = roundedFill(
            if (selected) panelPressed else Color.TRANSPARENT,
            18
        )
        nav.alpha = if (selected) 1f else 0.72f
    }

    private fun bottomNavBar(): LinearLayout {
        homeNav = navItem("⌂", "Home") { switchTab("home") }
        coolingNav = navItem("❄", "Cooling") { switchTab("cooling") }
        controlsNav = navItem("⌘", "Controls") { switchTab("controls") }
        val hardwareNav = navItem("⌥", "Hardware") { switchTab("hardware") }
        lightingNav = navItem("✦", "Lighting") { switchTab("lighting") }

        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(dp(10), dp(8), dp(10), dp(8))
            background = roundedTopBar()
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

            addView(homeNav, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
            addView(coolingNav, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
            addView(controlsNav, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
            addView(hardwareNav, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
            addView(lightingNav, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        }
    }

    private fun navItem(icon: String, label: String, onClick: () -> Unit): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(10), dp(10), dp(10), dp(10))
            isClickable = true
            isFocusable = true
            setOnClickListener { onClick() }

            addView(TextView(this@MainActivity).apply {
                text = icon
                textSize = 20f
                setTextColor(textPrimary)
                gravity = Gravity.CENTER
            })

            addView(TextView(this@MainActivity).apply {
                text = label
                textSize = 12f
                setTextColor(textPrimary)
                gravity = Gravity.CENTER
                setPadding(0, dp(4), 0, 0)
            })
        }
    }

    private fun roundedTopBar(): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(Color.parseColor("#11161F"))
            cornerRadius = dp(24).toFloat()
            setStroke(dp(1), borderColor)
        }
    }

    private fun updateManualCurveUiState() {
        val alpha = if (autoFanCurveEnabled) 0.40f else 1f

        quietCardRef.alpha = alpha
        balancedCardRef.alpha = alpha
        turboCardRef.alpha = alpha

        quietCardRef.isEnabled = !autoFanCurveEnabled
        balancedCardRef.isEnabled = !autoFanCurveEnabled
        turboCardRef.isEnabled = !autoFanCurveEnabled

        quietCardRef.isClickable = !autoFanCurveEnabled
        balancedCardRef.isClickable = !autoFanCurveEnabled
        turboCardRef.isClickable = !autoFanCurveEnabled
    }

    private fun refreshStatus() {
        val rooted = RootShell.hasRoot()
        val fanEnabled = HardwareController.isFanEnabled()
        val rpm = HardwareController.readFanRpm()
        val tempF = HardwareController.readTemperatureF()

        deviceModelValue.text = Build.MODEL ?: "Unknown"
        deviceRomValue.text = HardwareController.readShortRomFingerprint()
        deviceCpuValue.text = HardwareController.readCpuModel()
        deviceRamValue.text = HardwareController.readRamInfo()

        rootChip.text = if (rooted) "ROOT ON" else "ROOT OFF"
        fanChip.text = if (fanEnabled) "FAN ON" else "FAN OFF"
        rpmChip.text = "RPM ${rpm ?: "--"}"
        tempChip.text = if (tempF != null) "TEMP ${formatDisplayTempFromF(tempF)}" else "TEMP --"

        tempText.text = if (tempF != null) "Current temp: ${formatDisplayTempFromF(tempF)}" else "Current temp: --"

        setChipState(rootChip, rooted)
        setChipState(fanChip, fanEnabled)
        setChipState(rpmChip, (rpm ?: 0) > 0)
        setChipState(tempChip, tempF != null)
    }

    private fun openUrl(url: String) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (_: Throwable) {
        }
    }

    private fun scrollTabContainer(): LinearLayout {
        val inner = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )

            addView(ScrollView(this@MainActivity).apply {
                setBackgroundColor(bgColor)
                addView(inner)
            })

            tag = inner
        }.also { outer ->
            val scroll = outer.getChildAt(0) as ScrollView
            val child = scroll.getChildAt(0) as LinearLayout
            outer.removeAllViews()
            outer.addView(scroll.apply { removeAllViews(); addView(child) })
        }
    }

    private fun titleText(text: String): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = 22f
            setTextColor(textPrimary)
            setTypeface(typeface, Typeface.BOLD)
            letterSpacing = 0.04f
        }
    }

    private fun subtitleText(text: String): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = 13f
            setTextColor(textSecondary)
            setPadding(0, dp(4), 0, 0)
        }
    }

    private fun ledTitleText(text: String): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = 20f
            setTextColor(textPrimary)
            setTypeface(typeface, Typeface.BOLD)
            setPadding(dp(14), 0, 0, 0)
            setShadowLayer(dp(6).toFloat(), 0f, 0f, Color.parseColor("#5AA9FF"))

            val animator = ValueAnimator.ofObject(
                ArgbEvaluator(),
                Color.parseColor("#7CC0FF"),
                Color.parseColor("#E8EEF7"),
                Color.parseColor("#7CC0FF")
            ).apply {
                duration = 1800L
                repeatCount = ValueAnimator.INFINITE
                repeatMode = ValueAnimator.RESTART
                interpolator = LinearInterpolator()
                addUpdateListener { anim ->
                    val c = anim.animatedValue as Int
                    setTextColor(c)
                    setShadowLayer(dp(8).toFloat(), 0f, 0f, c)
                }
            }

            post { animator.start() }
        }
    }

    private fun bodyText(text: String): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = 13f
            setTextColor(textSecondary)
            setPadding(0, dp(4), 0, 0)
        }
    }

    private fun infoRow(label: String, valueView: TextView): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.TOP
            setPadding(0, 0, 0, dp(6))

            val labelView = TextView(this@MainActivity).apply {
                text = label
                textSize = 13f
                setTextColor(textSecondary)
                setTypeface(typeface, Typeface.BOLD)
                layoutParams = LinearLayout.LayoutParams(dp(64), ViewGroup.LayoutParams.WRAP_CONTENT)
            }

            valueView.layoutParams = LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
            )

            addView(labelView)
            addView(valueView)
        }
    }

    private fun infoValue(): TextView {
        return TextView(this).apply {
            text = "--"
            textSize = 13f
            setTextColor(textPrimary)
            setLineSpacing(0f, 1.1f)
            isSingleLine = false
            maxLines = 4
        }
    }

    private fun sectionHeader(icon: String, text: String): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 0, 0, dp(10))

            val iconView = TextView(this@MainActivity).apply {
                this.text = icon
                textSize = 11f
                setTextColor(textSecondary)
                gravity = Gravity.CENTER
                background = roundedFill(Color.parseColor("#1A2230"), 10)
                setPadding(dp(7), dp(5), dp(7), dp(5))
            }

            val labelView = TextView(this@MainActivity).apply {
                this.text = text
                textSize = 11f
                setTextColor(accent)
                setTypeface(typeface, Typeface.BOLD)
                letterSpacing = 0.06f
                setPadding(dp(8), 0, 0, 0)
            }

            addView(iconView)
            addView(labelView)
        }
    }

    private fun sectionPanel(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(18), dp(18), dp(18))
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dp(22).toFloat()
                setColor(panelColor)
                setStroke(dp(1), Color.parseColor("#2A3444"))
            }
            elevation = dp(3).toFloat()
            translationZ = dp(1).toFloat()
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dp(14)
            }
        }
    }

    private fun statusChip(text: String): TextView {
        return TextView(this).apply {
            this.text = text
            setTextColor(textPrimary)
            textSize = 10f
            setTypeface(typeface, Typeface.BOLD)
            gravity = Gravity.CENTER
            isSingleLine = true
            minHeight = dp(40)
            minimumWidth = dp(80)
            includeFontPadding = false
            setPadding(dp(12), dp(6), dp(12), dp(6))
            background = roundedFill(chipOn, 999)
        }
    }

    private fun setChipState(view: TextView, active: Boolean) {
        view.background = roundedFill(if (active) chipActive else chipOn, 14)
        view.setTextColor(textPrimary)
    }

    private fun modeCard(title: String, subtitle: String, onClick: () -> Unit): LinearLayout {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), dp(14), dp(14), dp(14))
            background = roundedBg(panelColor, borderColor, 18)
            layoutParams = LinearLayout.LayoutParams(dp(170), ViewGroup.LayoutParams.WRAP_CONTENT)
            minimumHeight = dp(108)
            isClickable = true
            isFocusable = true
        }

        val titleView = TextView(this).apply {
            text = title
            textSize = 16f
            setTextColor(textPrimary)
            setTypeface(typeface, Typeface.BOLD)
        }

        val subtitleView = TextView(this).apply {
            text = subtitle
            textSize = 12f
            setTextColor(textSecondary)
            setPadding(0, dp(4), 0, dp(10))
        }

        val chooseBtn = Button(this).apply {
            text = "CHOOSE"
            textSize = 12f
            setAllCaps(false)
            setTextColor(textPrimary)
            background = roundedFill(Color.parseColor("#1E2633"), 12)
            setPadding(dp(10), dp(10), dp(10), dp(10))
            setOnClickListener { onClick() }
            applyPressEffect(this)
        }

        card.addView(titleView)
        card.addView(subtitleView)
        card.addView(chooseBtn)

        card.setOnClickListener { onClick() }
        applyPressEffect(card)

        return card
    }

    private fun setActiveMode(active: LinearLayout) {
        val normal = roundedBg(panelColor, borderColor, 18)
        val selected = roundedBg(panelPressed, highlightBorder, 18)

        quietCardRef.background = normal
        balancedCardRef.background = normal
        turboCardRef.background = normal

        active.background = selected
    }

    private fun subtleLabel(text: String): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = 12f
            setTextColor(textSecondary)
            setPadding(0, dp(4), 0, 0)
        }
    }

    private fun premiumToggleRow(
        title: String,
        subtitle: String,
        checked: Boolean,
        onToggle: (Boolean) -> Unit
    ): LinearLayout {
        val titleView = TextView(this).apply {
            text = title
            textSize = 15f
            setTextColor(textPrimary)
            setTypeface(typeface, Typeface.BOLD)
        }

        val subtitleView = TextView(this).apply {
            text = subtitle
            textSize = 12f
            setTextColor(textSecondary)
            setPadding(0, dp(4), 0, 0)
        }

        val textCol = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(titleView)
            addView(subtitleView)
        }

        val toggle = android.widget.Switch(this).apply {
            isChecked = checked
            setOnCheckedChangeListener { _, isChecked -> onToggle(isChecked) }
        }

        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(14), dp(14), dp(14), dp(14))
            background = roundedBg(Color.parseColor("#161D28"), Color.parseColor("#253041"), 18)

            addView(textCol, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
            addView(toggle)
        }
    }

    private fun segmentedChip(
        label: String,
        selected: Boolean,
        onClick: () -> Unit
    ): Button {
        return Button(this).apply {
            text = label
            textSize = 12f
            setAllCaps(false)
            setTextColor(textPrimary)
            background = roundedFill(
                if (selected) panelPressed else Color.parseColor("#1A2230"),
                999
            )
            setPadding(dp(16), dp(10), dp(16), dp(10))
            setOnClickListener { onClick() }
            applyPressEffect(this)
        }
    }

    private fun actionButton(text: String, isDanger: Boolean = false, onClick: () -> Unit): Button {
        return Button(this).apply {
            this.text = text
            textSize = 13f
            setTextColor(textPrimary)
            setAllCaps(false)
            background = roundedFill(if (isDanger) danger else panelPressed, 16)
            setPadding(dp(12), dp(14), dp(12), dp(14))
            setOnClickListener { onClick() }
            applyPressEffect(this, if (isDanger) Color.parseColor("#733943") else Color.parseColor("#293447"))
        }
    }

    private fun row(left: Button, right: Button): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = dp(8) }

            left.layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginEnd = dp(6)
            }
            right.layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginStart = dp(6)
            }

            addView(left)
            addView(right)
        }
    }

    private fun singleRow(button: Button): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = dp(8) }

            button.layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            addView(button)
        }
    }

    private fun applyPressEffect(view: View, pressedColor: Int = Color.parseColor("#253040")) {
        val normalBg = view.background
        view.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.alpha = 0.96f
                    v.animate().scaleX(0.985f).scaleY(0.985f).setDuration(80).start()
                    v.background = when (v) {
                        is Button -> roundedFill(pressedColor, 16)
                        else -> roundedBg(pressedColor, highlightBorder, 18)
                    }
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    v.alpha = 1f
                    v.animate().scaleX(1f).scaleY(1f).setDuration(90).start()
                    v.background = normalBg
                }
            }
            false
        }
    }

    private fun roundedBg(fill: Int, stroke: Int, radiusDp: Int): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dp(radiusDp).toFloat()
            setColor(fill)
            setStroke(dp(1), stroke)
        }
    }

    private fun roundedFill(fill: Int, radiusDp: Int): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dp(radiusDp).toFloat()
            setColor(fill)
        }
    }

    private fun space(width: Int): TextView {
        return TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(width, 1)
        }
    }

    private fun spacer(height: Int): TextView {
        return TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                height
            )
        }
    }

    private fun getStatusBarHeight(): Int {
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId > 0) resources.getDimensionPixelSize(resourceId) else dp(24)
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    private fun smallActionButton(label: String, isDanger: Boolean = false, onClick: () -> Unit): Button {
        return Button(this).apply {
            text = label
            textSize = 12f
            setAllCaps(false)
            setTextColor(textPrimary)
            background = roundedFill(if (isDanger) danger else panelPressed, 14)
            setPadding(dp(12), dp(10), dp(12), dp(10))
            setOnClickListener { onClick() }
            minHeight = dp(44)
        }
    }

    private fun flowRow(vararg views: View): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 0, 0, dp(6))
            views.forEachIndexed { index, v ->
                val params = LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1f
                )
                if (index > 0) params.marginStart = dp(6)
                addView(v, params)
            }
        }
    }


    private fun showGamePickerDialog() {
        showGamePickerDialogUI(this) {
            updateGameModeStatusUI(magicKeyStatusLabelRef ?: return@showGamePickerDialogUI)
        }
    }




    private fun saveProfileForPackage(pkg: String) {
        saveProfileForPackageStorage(
            context = this,
            pkg = pkg,
            fanEnabled = HardwareController.isFanEnabled(),
            fanLevel = fanSeek.progress,
            fanLedEnabled = fanLedEnabled,
            fanLedEffect = fanLedEffect,
            fanLedColor = fanLedColor,
            fanLedModeType = if (fanLedEffect.startsWith("preset:")) "preset" else "basic",
            fanLedPresetValue = if (fanLedEffect.startsWith("preset:")) fanLedEffect.removePrefix("preset:") else ""
        )
    }

    private fun getSavedGamePackages(): MutableSet<String> {
        return getSavedGamePackagesStorage(this)
    }



    private fun updateGameModeStatusUI(textView: TextView) {
        textView.text = getGameModeStatusTextStorage(this)
    }


}

