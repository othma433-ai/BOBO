package com.althmany.extractor.engine.performance

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AntiFreezeWatchdogTest {
    @Test
    fun targetLossStopsInputAndRequestsRecovery() {
        val d = AntiFreezeWatchdog.evaluate(
            WatchdogSample(
                nowMs = 5000,
                lastProgressMs = 4500,
                repeatedSignatureCount = 0,
                backendHealthy = true,
                targetVisible = false
            )
        )
        assertEquals(StallKind.TARGET_LOST, d.kind)
        assertTrue(d.pauseInput)
        assertTrue(d.requestRecovery)
    }

    @Test
    fun repeatedUiSignatureTripsStaleRecovery() {
        val d = AntiFreezeWatchdog.evaluate(
            WatchdogSample(
                nowMs = 9000,
                lastProgressMs = 5000,
                repeatedSignatureCount = 5,
                backendHealthy = true,
                targetVisible = true
            )
        )
        assertEquals(StallKind.STALE_UI, d.kind)
        assertTrue(d.pauseInput)
    }

    @Test
    fun slowPathDoesNotImmediatelyRecover() {
        val d = AntiFreezeWatchdog.evaluate(
            WatchdogSample(
                nowMs = 5000,
                lastProgressMs = 1500,
                repeatedSignatureCount = 1,
                backendHealthy = true,
                targetVisible = true
            )
        )
        assertEquals(StallKind.SLOW, d.kind)
        assertFalse(d.requestRecovery)
    }
}
