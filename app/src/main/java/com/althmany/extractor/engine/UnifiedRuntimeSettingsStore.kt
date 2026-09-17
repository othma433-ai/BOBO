package com.althmany.extractor.engine

import android.content.Context

/**
 * Shared general runtime preferences for Scan / Extraction / Publish.
 * Feature-specific choices stay in their own stores; general runtime speed is canonical here.
 */
object UnifiedRuntimeSettingsStore {
    private const val PREFS = "althmany_unified_runtime_settings_v5"
    private const val KEY_SPEED = "runtime_speed"

    enum class Speed { HYPER, ADAPTIVE, SAFE }

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun speed(context: Context): Speed = runCatching {
        Speed.valueOf(prefs(context).getString(KEY_SPEED, Speed.ADAPTIVE.name) ?: Speed.ADAPTIVE.name)
    }.getOrDefault(Speed.ADAPTIVE)

    fun setSpeed(context: Context, value: Speed) {
        prefs(context).edit().putString(KEY_SPEED, value.name).apply()
    }
}
