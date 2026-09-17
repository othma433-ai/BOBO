package com.althmany.extractor.engine.performance

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SmartPerformanceEngineTest {
    @Test
    fun healthyLowLatencyPathUsesFastMode() {
        val d = SmartPerformanceEngine.decide(
            RuntimeTelemetry(
                uiLatencyMs = 180,
                snapshotLatencyMs = 250,
                staleUiRate = 0.01,
                failedActionRate = 0.01,
                transitionLatencyMs = 400,
                consecutiveFailures = 0,
                targetVisible = true,
                backendHealthy = true
            )
        )
        assertEquals(RuntimePerformanceMode.FAST, d.mode)
        assertTrue(d.timing.pollDelayMs <= 50)
    }

    @Test
    fun degradedPathUsesSafeMode() {
        val d = SmartPerformanceEngine.decide(
            RuntimeTelemetry(
                uiLatencyMs = 1200,
                snapshotLatencyMs = 1600,
                staleUiRate = 0.30,
                failedActionRate = 0.22,
                transitionLatencyMs = 1900,
                consecutiveFailures = 1,
                targetVisible = true,
                backendHealthy = true
            )
        )
        assertEquals(RuntimePerformanceMode.SAFE, d.mode)
    }

    @Test
    fun unhealthyBackendUsesRecoveryMode() {
        val d = SmartPerformanceEngine.decide(
            RuntimeTelemetry(
                uiLatencyMs = 0,
                snapshotLatencyMs = 0,
                staleUiRate = 0.0,
                failedActionRate = 0.0,
                transitionLatencyMs = 0,
                consecutiveFailures = 0,
                targetVisible = true,
                backendHealthy = false
            )
        )
        assertEquals(RuntimePerformanceMode.RECOVERY, d.mode)
        assertEquals(0, d.timing.maxImmediateRetries)
    }
}
