package com.althmany.extractor.engine.health

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RuntimeHealthScorerTest {
    @Test
    fun healthyRuntimeScoresHealthy() {
        val score = RuntimeHealthScorer.score(
            RuntimeHealthInput(
                targetVisible = true,
                backendHealthy = true,
                uiLatencyMs = 180,
                snapshotLatencyMs = 260,
                staleUiRate = 0.01,
                failedActionRate = 0.01,
                consecutiveFailures = 0,
                recoveryActive = false
            )
        )
        assertEquals(SmartHealthLevel.HEALTHY, score.level)
        assertTrue(score.score >= 75)
    }

    @Test
    fun missingTargetBlocksImmediately() {
        val score = RuntimeHealthScorer.score(
            RuntimeHealthInput(
                targetVisible = false,
                backendHealthy = true,
                uiLatencyMs = 0,
                snapshotLatencyMs = 0,
                staleUiRate = 0.0,
                failedActionRate = 0.0,
                consecutiveFailures = 0,
                recoveryActive = false
            )
        )
        assertEquals(SmartHealthLevel.BLOCKED, score.level)
        assertEquals(0, score.score)
    }
}
