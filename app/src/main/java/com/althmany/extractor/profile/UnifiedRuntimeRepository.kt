package com.althmany.extractor.profile

import android.content.Context
import com.althmany.extractor.engine.UnifiedRuntimeSettingsStore

/**
 * Single public facade for all general runtime configuration shared by
 * Join / Scan / Extraction / Publish. Feature-specific settings stay in
 * their own stores, while backend, target/profile and speed are canonical here.
 */
object UnifiedRuntimeRepository {
    fun resolve(context: Context, selectedPackageOverride: String? = null): UnifiedRuntimeSnapshot =
        UnifiedRuntimeTargetStore.resolve(context, selectedPackageOverride)

    fun preference(context: Context): RuntimeBackendPreference =
        UnifiedRuntimeTargetStore.preference(context)

    fun setPreference(context: Context, value: RuntimeBackendPreference) =
        UnifiedRuntimeTargetStore.setPreference(context, value)

    fun selectedPackage(context: Context): String? = UnifiedRuntimeTargetStore.selectedPackage(context)
    fun selectedAndroidUserId(context: Context): Int = UnifiedRuntimeTargetStore.selectedAndroidUserId(context)
    fun isRemoteTarget(context: Context): Boolean = UnifiedRuntimeTargetStore.isRemoteTarget(context)

    fun setLocalTarget(context: Context, packageName: String?) =
        UnifiedRuntimeTargetStore.setLocalTarget(context, packageName)

    fun setRemoteTarget(context: Context, target: UnifiedRemoteTarget) =
        UnifiedRuntimeTargetStore.setRemoteTarget(context, target)

    suspend fun discoverRemoteTargets(context: Context): List<UnifiedRemoteTarget> =
        UnifiedRuntimeTargetStore.discoverRemoteTargets(context)

    fun speed(context: Context): UnifiedRuntimeSettingsStore.Speed =
        UnifiedRuntimeSettingsStore.speed(context)

    fun setSpeed(context: Context, value: UnifiedRuntimeSettingsStore.Speed) =
        UnifiedRuntimeSettingsStore.setSpeed(context, value)
}
