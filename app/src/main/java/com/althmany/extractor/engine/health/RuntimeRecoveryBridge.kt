package com.althmany.extractor.engine.health

enum class ControllerRecoveryAction {
    NONE,
    APPLY_SAFER_TIMING,
    RETRY_CURRENT_ITEM,
    REQUEST_BACKEND_HANDOVER,
    PAUSE_AND_SHOW_CONTROLS,
    STOP_INPUT_AND_REPORT
}

data class ControllerRecoveryInstruction(
    val action: ControllerRecoveryAction,
    val preserveCheckpoint: Boolean,
    val requireFreshTarget: Boolean,
    val reason: String
)

/**
 * Pure decision mapper. It never clicks, launches WhatsApp, pauses a controller,
 * or changes a backend directly. Existing runtime controllers remain the sole executors.
 */
object RuntimeRecoveryBridge {
    fun from(decision: SmartRecoveryDecision): ControllerRecoveryInstruction =
        when (decision.directive) {
            RecoveryDirective.CONTINUE -> ControllerRecoveryInstruction(
                ControllerRecoveryAction.NONE,
                decision.preserveCheckpoint,
                decision.requireFreshTargetBeforeResume,
                decision.reason
            )
            RecoveryDirective.SLOW_DOWN -> ControllerRecoveryInstruction(
                ControllerRecoveryAction.APPLY_SAFER_TIMING,
                decision.preserveCheckpoint,
                decision.requireFreshTargetBeforeResume,
                decision.reason
            )
            RecoveryDirective.RETRY_SAME_BACKEND -> ControllerRecoveryInstruction(
                ControllerRecoveryAction.RETRY_CURRENT_ITEM,
                decision.preserveCheckpoint,
                decision.requireFreshTargetBeforeResume,
                decision.reason
            )
            RecoveryDirective.HANDOVER_BACKEND -> ControllerRecoveryInstruction(
                ControllerRecoveryAction.REQUEST_BACKEND_HANDOVER,
                decision.preserveCheckpoint,
                decision.requireFreshTargetBeforeResume,
                decision.reason
            )
            RecoveryDirective.PAUSE_OUTSIDE_TARGET -> ControllerRecoveryInstruction(
                ControllerRecoveryAction.PAUSE_AND_SHOW_CONTROLS,
                decision.preserveCheckpoint,
                decision.requireFreshTargetBeforeResume,
                decision.reason
            )
            RecoveryDirective.BLOCK -> ControllerRecoveryInstruction(
                ControllerRecoveryAction.STOP_INPUT_AND_REPORT,
                decision.preserveCheckpoint,
                decision.requireFreshTargetBeforeResume,
                decision.reason
            )
        }
}
