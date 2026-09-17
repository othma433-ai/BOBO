package com.althmany.extractor.runtime

import com.althmany.extractor.profile.RuntimeBackendKind
import com.althmany.extractor.profile.RuntimeBackendPreference

object SmartBackendPolicy {
    fun resolve(input: SmartBackendInput): RuntimeBackendDecision {
        fun decision(kind: RuntimeBackendKind, reason: String) = RuntimeBackendDecision(
            requestedBackend = input.preference,
            effectiveBackend = kind,
            blocked = kind == RuntimeBackendKind.NONE,
            reasonCode = reason
        )

        return when (input.preference) {
            RuntimeBackendPreference.ACCESSIBILITY -> {
                if (input.accessibility.healthy) decision(RuntimeBackendKind.ACCESSIBILITY, "MANUAL_ACCESSIBILITY_READY")
                else decision(RuntimeBackendKind.NONE, "MANUAL_ACCESSIBILITY_NOT_READY")
            }
            RuntimeBackendPreference.SHIZUKU -> {
                if (input.shizuku.healthy) decision(RuntimeBackendKind.SHIZUKU, "MANUAL_SHIZUKU_READY")
                else decision(RuntimeBackendKind.NONE, "MANUAL_SHIZUKU_NOT_READY")
            }
            RuntimeBackendPreference.AUTO -> {
                if (input.remoteTarget) {
                    when {
                        input.shizuku.healthy -> decision(RuntimeBackendKind.SHIZUKU, "AUTO_REMOTE_SHIZUKU_READY")
                        input.accessibility.healthy -> decision(RuntimeBackendKind.ACCESSIBILITY, "AUTO_REMOTE_PROFILE_SAFE_ACCESSIBILITY")
                        else -> decision(RuntimeBackendKind.NONE, "AUTO_REMOTE_NO_SAFE_BACKEND")
                    }
                } else {
                    when {
                        input.accessibility.healthy -> decision(RuntimeBackendKind.ACCESSIBILITY, "AUTO_LOCAL_ACCESSIBILITY_READY")
                        input.shizuku.healthy -> decision(RuntimeBackendKind.SHIZUKU, "AUTO_LOCAL_SHIZUKU_FALLBACK")
                        else -> decision(RuntimeBackendKind.NONE, "AUTO_LOCAL_NO_BACKEND")
                    }
                }
            }
        }
    }
}
