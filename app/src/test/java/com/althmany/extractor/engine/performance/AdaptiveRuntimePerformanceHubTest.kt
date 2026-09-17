package com.althmany.extractor.engine.performance

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AdaptiveRuntimePerformanceHubTest {
    @Before
    fun setup() {
        AdaptiveRuntimePerformanceHub.resetAllForTests()
    }

    @After
    fun teardown() {
        AdaptiveRuntimePerformanceHub.resetAllForTests()
    }

    @Test
    fun healthyScanBecomesFast() {
        repeat(8) {
            AdaptiveRuntimePerformanceHub.recordUiLatency(RuntimePerformanceOwner.SCAN, 180)
            AdaptiveRuntimePerformanceHub.recordSnapshotLatency(RuntimePerformanceOwner.SCAN, 260)
            AdaptiveRuntimePerformanceHub.recordTransitionLatency(RuntimePerformanceOwner.SCAN, 420)
            AdaptiveRuntimePerformanceHub.recordAction(RuntimePerformanceOwner.SCAN, true)
        }

        val d = AdaptiveRuntimePerformanceHub.snapshot(RuntimePerformanceOwner.SCAN).decision
        assertEquals(RuntimePerformanceMode.FAST, d.mode)
    }

    @Test
    fun repeatedSignatureTripsStaleWatchdog() {
        AdaptiveRuntimePerformanceHub.recordSignature(RuntimePerformanceOwner.EXTRACTION, "same")
        repeat(5) {
            AdaptiveRuntimePerformanceHub.recordSignature(RuntimePerformanceOwner.EXTRACTION, "same")
        }
        AdaptiveRuntimePerformanceHub.advanceClockForTests(3000)

        val d = AdaptiveRuntimePerformanceHub.watchdog(RuntimePerformanceOwner.EXTRACTION)
        assertEquals(StallKind.STALE_UI, d.kind)
        assertTrue(d.requestRecovery)
    }

    @Test
    fun backendFailureForcesRecoveryTiming() {
        AdaptiveRuntimePerformanceHub.recordBackendHealthy(RuntimePerformanceOwner.PUBLISH, false)
        val snap = AdaptiveRuntimePerformanceHub.snapshot(RuntimePerformanceOwner.PUBLISH)
        assertEquals(RuntimePerformanceMode.RECOVERY, snap.decision.mode)
        assertEquals(0, snap.decision.timing.maxImmediateRetries)
    }
}
