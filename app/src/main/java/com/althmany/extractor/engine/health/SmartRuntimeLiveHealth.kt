package com.althmany.extractor.engine.health

import com.althmany.extractor.engine.performance.AdaptiveRuntimePerformanceHub
import com.althmany.extractor.engine.performance.RuntimePerformanceOwner

data class LiveHealthSnapshot(
    val owner: RuntimePerformanceOwner,
    val level: SmartHealthLevel,
    val score: Int,
    val performanceMode: String,
    val recoveryDirective: RecoveryDirective,
    val reason: String,
    val uiLatencyMs: Long,
    val snapshotLatencyMs: Long,
    val staleUiRate: Double,
    val failedActionRate: Double,
    val consecutiveFailures: Int
)

object SmartRuntimeLiveHealth {
    fun capture(
        owner: RuntimePerformanceOwner,
        preferenceAuto: Boolean,
        alternateBackendReady: Boolean,
        recoveryActive: Boolean = false,
        hardBackendFailure: Boolean = false
    ): LiveHealthSnapshot {
        val perf = AdaptiveRuntimePerformanceHub.snapshot(owner)
        val t = perf.telemetry
        val health = RuntimeHealthScorer.score(
            RuntimeHealthInput(
                targetVisible = t.targetVisible,
                backendHealthy = t.backendHealthy,
                uiLatencyMs = t.uiLatencyMs,
                snapshotLatencyMs = t.snapshotLatencyMs,
                staleUiRate = t.staleUiRate,
                failedActionRate = t.failedActionRate,
                consecutiveFailures = t.consecutiveFailures,
                recoveryActive = recoveryActive
            )
        )
        val recovery = SmartRecoveryDecisionEngine.decide(
            SmartRecoveryInput(
                targetVisible = t.targetVisible,
                preferenceAuto = preferenceAuto,
                backendHealthy = t.backendHealthy,
                alternateBackendReady = alternateBackendReady,
                consecutiveFailures = t.consecutiveFailures,
                staleUiConfirmed = perf.repeatedSignatureCount >= 5,
                hardBackendFailure = hardBackendFailure
            )
        )
        return LiveHealthSnapshot(
            owner = owner,
            level = health.level,
            score = health.score,
            performanceMode = perf.decision.mode.name,
            recoveryDirective = recovery.directive,
            reason = "${health.reason};${recovery.reason}",
            uiLatencyMs = t.uiLatencyMs,
            snapshotLatencyMs = t.snapshotLatencyMs,
            staleUiRate = t.staleUiRate,
            failedActionRate = t.failedActionRate,
            consecutiveFailures = t.consecutiveFailures
        )
    }
}
