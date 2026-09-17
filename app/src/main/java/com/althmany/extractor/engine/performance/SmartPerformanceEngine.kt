package com.althmany.extractor.engine.performance

enum class RuntimePerformanceMode {
    FAST, BALANCED, SAFE, RECOVERY
}

data class RuntimeTelemetry(
    val uiLatencyMs: Long,
    val snapshotLatencyMs: Long,
    val staleUiRate: Double,
    val failedActionRate: Double,
    val transitionLatencyMs: Long,
    val consecutiveFailures: Int,
    val targetVisible: Boolean,
    val backendHealthy: Boolean
)

data class RuntimeTiming(
    val actionDelayMs: Long,
    val pollDelayMs: Long,
    val verifyDelayMs: Long,
    val timeoutMs: Long,
    val maxImmediateRetries: Int
)

data class PerformanceDecision(
    val mode: RuntimePerformanceMode,
    val timing: RuntimeTiming,
    val reason: String
)

object SmartPerformanceEngine {
    fun decide(t: RuntimeTelemetry): PerformanceDecision {
        if (!t.targetVisible || !t.backendHealthy || t.consecutiveFailures >= 3) {
            return PerformanceDecision(
                RuntimePerformanceMode.RECOVERY,
                RuntimeTiming(
                    actionDelayMs = 420,
                    pollDelayMs = 220,
                    verifyDelayMs = 320,
                    timeoutMs = 12_000,
                    maxImmediateRetries = 0
                ),
                "recovery: target/backend/failure gate"
            )
        }

        if (
            t.staleUiRate >= 0.25 ||
            t.failedActionRate >= 0.20 ||
            t.uiLatencyMs >= 1100 ||
            t.snapshotLatencyMs >= 1400 ||
            t.transitionLatencyMs >= 1800
        ) {
            return PerformanceDecision(
                RuntimePerformanceMode.SAFE,
                RuntimeTiming(
                    actionDelayMs = 260,
                    pollDelayMs = 140,
                    verifyDelayMs = 220,
                    timeoutMs = 10_000,
                    maxImmediateRetries = 1
                ),
                "safe: degraded responsiveness"
            )
        }

        if (
            t.staleUiRate <= 0.05 &&
            t.failedActionRate <= 0.05 &&
            t.uiLatencyMs <= 300 &&
            t.snapshotLatencyMs <= 450 &&
            t.transitionLatencyMs <= 650
        ) {
            return PerformanceDecision(
                RuntimePerformanceMode.FAST,
                RuntimeTiming(
                    actionDelayMs = 70,
                    pollDelayMs = 45,
                    verifyDelayMs = 110,
                    timeoutMs = 6_000,
                    maxImmediateRetries = 1
                ),
                "fast: healthy low-latency path"
            )
        }

        return PerformanceDecision(
            RuntimePerformanceMode.BALANCED,
            RuntimeTiming(
                actionDelayMs = 140,
                pollDelayMs = 80,
                verifyDelayMs = 160,
                timeoutMs = 8_000,
                maxImmediateRetries = 1
            ),
            "balanced: default adaptive path"
        )
    }
}
