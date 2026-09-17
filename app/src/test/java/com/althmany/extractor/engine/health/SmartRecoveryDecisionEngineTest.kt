package com.althmany.extractor.engine.health

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SmartRecoveryDecisionEngineTest {
    @Test
    fun targetLossPausesAndPreservesCheckpoint() {
        val d = SmartRecoveryDecisionEngine.decide(
            SmartRecoveryInput(
                targetVisible = false,
                preferenceAuto = true,
                backendHealthy = true,
                alternateBackendReady = true,
                consecutiveFailures = 0,
                staleUiConfirmed = false,
                hardBackendFailure = false
            )
        )
        assertEquals(RecoveryDirective.PAUSE_OUTSIDE_TARGET, d.directive)
        assertTrue(d.preserveCheckpoint)
        assertTrue(d.requireFreshTargetBeforeResume)
    }

    @Test
    fun hardFailureAutoHandsOverOnlyWhenAlternateReady() {
        val d = SmartRecoveryDecisionEngine.decide(
            SmartRecoveryInput(
                targetVisible = true,
                preferenceAuto = true,
                backendHealthy = false,
                alternateBackendReady = true,
                consecutiveFailures = 3,
                staleUiConfirmed = false,
                hardBackendFailure = true
            )
        )
        assertEquals(RecoveryDirective.HANDOVER_BACKEND, d.directive)
    }

    @Test
    fun manualModeNeverCrossesBackendOnHardFailure() {
        val d = SmartRecoveryDecisionEngine.decide(
            SmartRecoveryInput(
                targetVisible = true,
                preferenceAuto = false,
                backendHealthy = false,
                alternateBackendReady = true,
                consecutiveFailures = 3,
                staleUiConfirmed = false,
                hardBackendFailure = true
            )
        )
        assertEquals(RecoveryDirective.BLOCK, d.directive)
    }

    @Test
    fun singleFailureSlowsDownInsteadOfOverRecovering() {
        val d = SmartRecoveryDecisionEngine.decide(
            SmartRecoveryInput(
                targetVisible = true,
                preferenceAuto = true,
                backendHealthy = true,
                alternateBackendReady = true,
                consecutiveFailures = 1,
                staleUiConfirmed = false,
                hardBackendFailure = false
            )
        )
        assertEquals(RecoveryDirective.SLOW_DOWN, d.directive)
    }
}
