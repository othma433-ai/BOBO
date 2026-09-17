package com.althmany.extractor.engine.health

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RuntimeRecoveryBridgeTest {
    @Test
    fun targetLossBecomesPauseAndShowControls() {
        val instruction = RuntimeRecoveryBridge.from(
            SmartRecoveryDecision(
                directive = RecoveryDirective.PAUSE_OUTSIDE_TARGET,
                preserveCheckpoint = true,
                requireFreshTargetBeforeResume = true,
                reason = "target-left"
            )
        )
        assertEquals(ControllerRecoveryAction.PAUSE_AND_SHOW_CONTROLS, instruction.action)
        assertTrue(instruction.preserveCheckpoint)
        assertTrue(instruction.requireFreshTarget)
    }

    @Test
    fun backendHandoverIsOnlyARequestToRuntimeManager() {
        val instruction = RuntimeRecoveryBridge.from(
            SmartRecoveryDecision(
                directive = RecoveryDirective.HANDOVER_BACKEND,
                preserveCheckpoint = true,
                requireFreshTargetBeforeResume = true,
                reason = "hard-backend-failure"
            )
        )
        assertEquals(ControllerRecoveryAction.REQUEST_BACKEND_HANDOVER, instruction.action)
    }
}
