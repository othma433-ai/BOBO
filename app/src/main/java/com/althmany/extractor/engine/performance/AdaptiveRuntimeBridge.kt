package com.althmany.extractor.engine.performance

/**
 * Thin bridge for controllers. It deliberately owns no UI action and no coroutine.
 * Controllers remain the only executors; this bridge only supplies timing/health advice.
 */
object AdaptiveRuntimeBridge {
    fun scanTiming(): RuntimeTiming =
        AdaptiveRuntimePerformanceHub.timing(RuntimePerformanceOwner.SCAN)

    fun extractionTiming(): RuntimeTiming =
        AdaptiveRuntimePerformanceHub.timing(RuntimePerformanceOwner.EXTRACTION)

    fun publishTiming(): RuntimeTiming =
        AdaptiveRuntimePerformanceHub.timing(RuntimePerformanceOwner.PUBLISH)

    fun scanWatchdog(): WatchdogDecision =
        AdaptiveRuntimePerformanceHub.watchdog(RuntimePerformanceOwner.SCAN)

    fun extractionWatchdog(): WatchdogDecision =
        AdaptiveRuntimePerformanceHub.watchdog(RuntimePerformanceOwner.EXTRACTION)

    fun publishWatchdog(): WatchdogDecision =
        AdaptiveRuntimePerformanceHub.watchdog(RuntimePerformanceOwner.PUBLISH)
}
