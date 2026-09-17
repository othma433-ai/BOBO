package com.althmany.extractor.engine.health

enum class RecoveryDirective {
    CONTINUE,
    SLOW_DOWN,
    RETRY_SAME_BACKEND,
    HANDOVER_BACKEND,
    PAUSE_OUTSIDE_TARGET,
    BLOCK
}

data class SmartRecoveryInput(
    val targetVisible: Boolean,
    val preferenceAuto: Boolean,
    val backendHealthy: Boolean,
    val alternateBackendReady: Boolean,
    val consecutiveFailures: Int,
    val staleUiConfirmed: Boolean,
    val hardBackendFailure: Boolean
)

data class SmartRecoveryDecision(
    val directive: RecoveryDirective,
    val preserveCheckpoint: Boolean,
    val requireFreshTargetBeforeResume: Boolean,
    val reason: String
)

object SmartRecoveryDecisionEngine {
    fun decide(input: SmartRecoveryInput): SmartRecoveryDecision {
        if (!input.targetVisible) {
            return SmartRecoveryDecision(
                RecoveryDirective.PAUSE_OUTSIDE_TARGET,
                preserveCheckpoint = true,
                requireFreshTargetBeforeResume = true,
                reason = "target-left"
            )
        }

        if (input.hardBackendFailure) {
            if (input.preferenceAuto && input.alternateBackendReady) {
                return SmartRecoveryDecision(
                    RecoveryDirective.HANDOVER_BACKEND,
                    preserveCheckpoint = true,
                    requireFreshTargetBeforeResume = true,
                    reason = "hard-backend-failure"
                )
            }
            return SmartRecoveryDecision(
                RecoveryDirective.BLOCK,
                preserveCheckpoint = true,
                requireFreshTargetBeforeResume = true,
                reason = "hard-backend-failure-no-safe-alternate"
            )
        }

        if (!input.backendHealthy) {
            return SmartRecoveryDecision(
                RecoveryDirective.RETRY_SAME_BACKEND,
                preserveCheckpoint = true,
                requireFreshTargetBeforeResume = true,
                reason = "backend-degraded"
            )
        }

        if (input.staleUiConfirmed || input.consecutiveFailures >= 2) {
            return SmartRecoveryDecision(
                RecoveryDirective.RETRY_SAME_BACKEND,
                preserveCheckpoint = true,
                requireFreshTargetBeforeResume = true,
                reason = "stale-or-repeated-failure"
            )
        }

        if (input.consecutiveFailures == 1) {
            return SmartRecoveryDecision(
                RecoveryDirective.SLOW_DOWN,
                preserveCheckpoint = true,
                requireFreshTargetBeforeResume = false,
                reason = "single-transient-failure"
            )
        }

        return SmartRecoveryDecision(
            RecoveryDirective.CONTINUE,
            preserveCheckpoint = true,
            requireFreshTargetBeforeResume = false,
            reason = "healthy"
        )
    }
}
