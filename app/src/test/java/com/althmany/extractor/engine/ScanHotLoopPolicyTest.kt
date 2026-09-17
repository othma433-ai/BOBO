package com.althmany.extractor.engine

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ScanHotLoopPolicyTest {

    @Test
    fun notifierIsThrottledInsideHotLoop() {
        assertTrue(ScanHotLoopPolicy.shouldNotify(lastMs = 0L, nowMs = 500L))
        assertFalse(ScanHotLoopPolicy.shouldNotify(lastMs = 300L, nowMs = 500L))
    }

    @Test
    fun statsRefreshIsBatched() {
        assertFalse(ScanHotLoopPolicy.shouldRefreshStats(completedItems = 1))
        assertTrue(ScanHotLoopPolicy.shouldRefreshStats(completedItems = 20))
        assertTrue(ScanHotLoopPolicy.shouldRefreshStats(completedItems = 40))
    }

    @Test
    fun finalItemAlwaysRefreshesStats() {
        assertTrue(ScanHotLoopPolicy.shouldRefreshStats(completedItems = 7, finalItem = true))
    }

    @Test
    fun staleScreenPollingRemainsShortButNonZero() {
        assertTrue(ScanHotLoopPolicy.stalePollDelayMs in 20L..80L)
    }

    @Test
    fun hotLoopKeepsSnapshotLimitAtTwo() {
        assertTrue(ScanHotLoopPolicy.maxSnapshotsPerLink == 2)
    }
}
