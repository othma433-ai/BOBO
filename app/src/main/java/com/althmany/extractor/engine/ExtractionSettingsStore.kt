package com.althmany.extractor.engine

import android.content.Context
import com.althmany.extractor.profile.UnifiedRuntimeRepository
import com.althmany.extractor.data.ExtractionMode
import com.althmany.extractor.data.ExtractionPreferences
import com.althmany.extractor.data.SpeedProfile

class ExtractionSettingsStore(private val context: Context) {
    private val prefs = context.getSharedPreferences("extraction_settings", Context.MODE_PRIVATE)

    fun get(): ExtractionPreferences = ExtractionPreferences(
        mode = runCatching {
            ExtractionMode.valueOf(prefs.getString("mode", ExtractionMode.DEEP.name)!!)
        }.getOrDefault(ExtractionMode.DEEP),
        speed = when (UnifiedRuntimeRepository.speed(context)) {
            UnifiedRuntimeSettingsStore.Speed.HYPER -> SpeedProfile.HYPER
            UnifiedRuntimeSettingsStore.Speed.ADAPTIVE -> SpeedProfile.ADAPTIVE
            UnifiedRuntimeSettingsStore.Speed.SAFE -> SpeedProfile.SAFE
        },
        maxScrollIterations = prefs.getInt("max_scroll_iterations", 2_000).coerceIn(100, 10_000),
        betweenItemsDelayMs = prefs.getLong("between_items_delay_ms", 0L).coerceIn(0L, 60_000L),
        strictEndProof = prefs.getBoolean("strict_end", true),
        autoRecoverWhatsApp = prefs.getBoolean("auto_recover", true),
        targetWhatsAppPackage = prefs.getString("target_whatsapp_package", null),
        continuousScrollEnabled = prefs.getBoolean("continuous_scroll", true),
        fastEndVerificationEnabled = prefs.getBoolean("fast_end_enabled", true),
        fastEndVerificationMs = prefs.getLong("fast_end_ms", 260L).coerceIn(120L, 700L),
        hiddenSearchNavigation = prefs.getBoolean("hidden_search_navigation", true),
        checkpointEnabled = prefs.getBoolean("checkpoint_enabled", true)
    )

    fun setMode(mode: ExtractionMode) {
        prefs.edit().putString("mode", mode.name).apply()
    }

    fun setSpeed(speed: SpeedProfile) {
        val shared = when (speed) {
            SpeedProfile.HYPER -> UnifiedRuntimeSettingsStore.Speed.HYPER
            SpeedProfile.SAFE -> UnifiedRuntimeSettingsStore.Speed.SAFE
            else -> UnifiedRuntimeSettingsStore.Speed.ADAPTIVE
        }
        UnifiedRuntimeRepository.setSpeed(context, shared)
    }

    fun setMaxScrollIterations(value: Int) {
        prefs.edit().putInt("max_scroll_iterations", value.coerceIn(100, 10_000)).apply()
    }


    fun setBetweenItemsDelayMs(value: Long) {
        prefs.edit().putLong("between_items_delay_ms", value.coerceIn(0L, 60_000L)).apply()
    }


    fun setContinuousScrollEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("continuous_scroll", enabled).apply()
    }

    fun setFastEndVerificationEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("fast_end_enabled", enabled).apply()
    }

    fun setFastEndVerificationMs(value: Long) {
        prefs.edit().putLong("fast_end_ms", value.coerceIn(120L, 700L)).apply()
    }

    fun setHiddenSearchNavigation(enabled: Boolean) {
        prefs.edit().putBoolean("hidden_search_navigation", enabled).apply()
    }

    fun setCheckpointEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("checkpoint_enabled", enabled).apply()
    }

    fun setTargetWhatsAppPackage(packageName: String?) {
        prefs.edit().apply {
            if (packageName == null) remove("target_whatsapp_package") else putString("target_whatsapp_package", packageName)
        }.apply()
    }
}
