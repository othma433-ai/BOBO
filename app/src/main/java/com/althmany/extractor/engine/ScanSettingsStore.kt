package com.althmany.extractor.engine

import android.content.Context
import com.althmany.extractor.profile.UnifiedRuntimeRepository

class ScanSettingsStore(private val context: Context) {
    private val prefs = context.getSharedPreferences("scan_settings", Context.MODE_PRIVATE)

    fun loadSpeed(): ScanSpeedProfile = when (UnifiedRuntimeRepository.speed(context)) {
        UnifiedRuntimeSettingsStore.Speed.HYPER -> ScanSpeedProfile.HYPER
        UnifiedRuntimeSettingsStore.Speed.ADAPTIVE -> ScanSpeedProfile.ADAPTIVE
        UnifiedRuntimeSettingsStore.Speed.SAFE -> ScanSpeedProfile.SAFE
    }

    fun saveSpeed(value: ScanSpeedProfile) {
        UnifiedRuntimeRepository.setSpeed(context, when (value) {
            ScanSpeedProfile.HYPER -> UnifiedRuntimeSettingsStore.Speed.HYPER
            ScanSpeedProfile.SAFE -> UnifiedRuntimeSettingsStore.Speed.SAFE
            ScanSpeedProfile.ADAPTIVE -> UnifiedRuntimeSettingsStore.Speed.ADAPTIVE
        })
    }

    fun loadScope(): ScanScope = runCatching {
        ScanScope.valueOf(prefs.getString("scope", ScanScope.PENDING_ONLY.name)!!)
    }.getOrDefault(ScanScope.PENDING_ONLY)

    fun saveScope(value: ScanScope) {
        prefs.edit().putString("scope", value.name).apply()
    }

    fun loadActionMode(): ScanActionMode = ScanActionMode.SCAN_ONLY

    fun saveActionMode(value: ScanActionMode) { /* scan is read-only */ }

    fun loadRequestToJoinEnabled(): Boolean = false

    fun saveRequestToJoinEnabled(value: Boolean) { /* scan is read-only */ }

    fun loadMaxAttempts(): Int = 1

    fun saveMaxAttempts(value: Int) { /* single-flight by design */ }
}
