package com.althmany.extractor.engine

import com.althmany.extractor.profile.RuntimeBackendKind
import com.althmany.extractor.profile.RuntimeBackendPreference

enum class ScanRecoveryStep {
    RETRY_ACCESSIBILITY,
    RETRY_SHIZUKU,
    SWITCH_TO_ACCESSIBILITY,
    SWITCH_TO_SHIZUKU
}

data class ScanRecoveryPlan(
    val steps: List<ScanRecoveryStep>,
    val maySwitchBackend: Boolean,
    val continueSameItem: Boolean = true,
    val mustVerifyBeforeContinue: Boolean = true
)

/**
 * Backend-neutral recovery contract for the read-only Scan pipeline.
 *
 * Manual backend modes never cross over to another backend.
 * AUTO retries the current backend first, then falls back only when the
 * alternative backend is actually available.
 */
object ScanRuntimeRecoveryPolicy {

    fun plan(
        preference: RuntimeBackendPreference,
        failedBackend: RuntimeBackendKind,
        accessibilityCanRecover: Boolean,
        shizukuReady: Boolean
    ): ScanRecoveryPlan {
        return when (preference) {
            RuntimeBackendPreference.ACCESSIBILITY ->
                ScanRecoveryPlan(
                    steps = listOf(ScanRecoveryStep.RETRY_ACCESSIBILITY),
                    maySwitchBackend = false
                )

            RuntimeBackendPreference.SHIZUKU ->
                ScanRecoveryPlan(
                    steps = listOf(ScanRecoveryStep.RETRY_SHIZUKU),
                    maySwitchBackend = false
                )

            RuntimeBackendPreference.AUTO -> when (failedBackend) {
                RuntimeBackendKind.ACCESSIBILITY -> {
                    val steps = buildList {
                        add(ScanRecoveryStep.RETRY_ACCESSIBILITY)
                        if (shizukuReady) add(ScanRecoveryStep.SWITCH_TO_SHIZUKU)
                    }
                    ScanRecoveryPlan(
                        steps = steps,
                        maySwitchBackend = steps.any { it == ScanRecoveryStep.SWITCH_TO_SHIZUKU }
                    )
                }

                RuntimeBackendKind.SHIZUKU -> {
                    val steps = buildList {
                        add(ScanRecoveryStep.RETRY_SHIZUKU)
                        if (accessibilityCanRecover) add(ScanRecoveryStep.SWITCH_TO_ACCESSIBILITY)
                    }
                    ScanRecoveryPlan(
                        steps = steps,
                        maySwitchBackend = steps.any { it == ScanRecoveryStep.SWITCH_TO_ACCESSIBILITY }
                    )
                }

                RuntimeBackendKind.NONE -> {
                    val steps = buildList {
                        if (accessibilityCanRecover) add(ScanRecoveryStep.SWITCH_TO_ACCESSIBILITY)
                        if (shizukuReady) add(ScanRecoveryStep.SWITCH_TO_SHIZUKU)
                    }
                    ScanRecoveryPlan(
                        steps = steps,
                        maySwitchBackend = steps.isNotEmpty()
                    )
                }
            }
        }
    }
}
