package com.althmany.extractor.engine.performance

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

enum class RuntimePerformanceOwner {
    EXTRACTION, SCAN, PUBLISH, SENDER
}

data class RuntimePerformanceSnapshot(
    val owner: RuntimePerformanceOwner,
    val telemetry: RuntimeTelemetry,
    val decision: PerformanceDecision,
    val lastProgressMs: Long,
    val repeatedSignatureCount: Int
)

object AdaptiveRuntimePerformanceHub {
    private data class OwnerState(
        val window: TelemetryWindow = TelemetryWindow(),
        var consecutiveFailures: Int = 0,
        var targetVisible: Boolean = true,
        var backendHealthy: Boolean = true,
        var lastSignature: String? = null,
        var repeatedSignatureCount: Int = 0,
        var lastProgressMs: Long = 0L
    )

    private val states = ConcurrentHashMap<RuntimePerformanceOwner, OwnerState>()
    private val clockOffset = AtomicLong(0L)

    private fun state(owner: RuntimePerformanceOwner): OwnerState =
        states.getOrPut(owner) { OwnerState(lastProgressMs = nowMs()) }

    fun recordUiLatency(owner: RuntimePerformanceOwner, ms: Long) {
        state(owner).window.recordUiLatency(ms)
    }

    fun recordSnapshotLatency(owner: RuntimePerformanceOwner, ms: Long) {
        state(owner).window.recordSnapshotLatency(ms)
    }

    fun recordTransitionLatency(owner: RuntimePerformanceOwner, ms: Long) {
        state(owner).window.recordTransitionLatency(ms)
    }

    fun recordAction(owner: RuntimePerformanceOwner, success: Boolean) {
        val s = state(owner)
        s.window.recordAction(success)
        if (success) {
            s.consecutiveFailures = 0
            s.lastProgressMs = nowMs()
        } else {
            s.consecutiveFailures += 1
        }
    }

    fun recordTargetVisible(owner: RuntimePerformanceOwner, visible: Boolean) {
        val s = state(owner)
        s.targetVisible = visible
        if (visible) s.lastProgressMs = nowMs()
    }

    fun recordBackendHealthy(owner: RuntimePerformanceOwner, healthy: Boolean) {
        state(owner).backendHealthy = healthy
    }

    fun recordSignature(owner: RuntimePerformanceOwner, signature: String?) {
        if (signature.isNullOrBlank()) return
        val s = state(owner)
        if (s.lastSignature == signature) {
            s.repeatedSignatureCount += 1
            s.window.recordStaleUi()
        } else {
            s.lastSignature = signature
            s.repeatedSignatureCount = 0
            s.lastProgressMs = nowMs()
        }
    }

    fun markProgress(owner: RuntimePerformanceOwner) {
        val s = state(owner)
        s.lastProgressMs = nowMs()
        s.consecutiveFailures = 0
        s.repeatedSignatureCount = 0
    }

    fun snapshot(owner: RuntimePerformanceOwner): RuntimePerformanceSnapshot {
        val s = state(owner)
        val telemetry = s.window.snapshot(
            consecutiveFailures = s.consecutiveFailures,
            targetVisible = s.targetVisible,
            backendHealthy = s.backendHealthy
        )
        return RuntimePerformanceSnapshot(
            owner = owner,
            telemetry = telemetry,
            decision = SmartPerformanceEngine.decide(telemetry),
            lastProgressMs = s.lastProgressMs,
            repeatedSignatureCount = s.repeatedSignatureCount
        )
    }

    fun timing(owner: RuntimePerformanceOwner): RuntimeTiming = snapshot(owner).decision.timing

    fun watchdog(owner: RuntimePerformanceOwner): WatchdogDecision {
        val s = state(owner)
        return AntiFreezeWatchdog.evaluate(
            WatchdogSample(
                nowMs = nowMs(),
                lastProgressMs = s.lastProgressMs,
                repeatedSignatureCount = s.repeatedSignatureCount,
                backendHealthy = s.backendHealthy,
                targetVisible = s.targetVisible
            )
        )
    }

    fun reset(owner: RuntimePerformanceOwner) {
        states.remove(owner)
    }

    internal fun resetAllForTests() {
        states.clear()
        clockOffset.set(0L)
    }

    internal fun advanceClockForTests(ms: Long) {
        clockOffset.addAndGet(ms)
    }

    private fun nowMs(): Long = System.currentTimeMillis() + clockOffset.get()
}
