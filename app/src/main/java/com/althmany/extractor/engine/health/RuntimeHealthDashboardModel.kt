package com.althmany.extractor.engine.health

import com.althmany.extractor.engine.performance.AdaptiveRuntimePerformanceHub
import com.althmany.extractor.engine.performance.RuntimePerformanceOwner

data class RuntimeHealthDashboardRow(
    val owner: RuntimePerformanceOwner,
    val health: RuntimeHealthScore,
    val performanceMode: String,
    val uiLatencyMs: Long,
    val snapshotLatencyMs: Long,
    val staleUiRate: Double,
    val failedActionRate: Double,
    val consecutiveFailures: Int
)

object RuntimeHealthDashboardModel {
    fun capture(owner: RuntimePerformanceOwner, recoveryActive: Boolean = false): RuntimeHealthDashboardRow {
        val s = AdaptiveRuntimePerformanceHub.snapshot(owner)
        val t = s.telemetry
        val h = RuntimeHealthScorer.score(
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
        return RuntimeHealthDashboardRow(
            owner = owner,
            health = h,
            performanceMode = s.decision.mode.name,
            uiLatencyMs = t.uiLatencyMs,
            snapshotLatencyMs = t.snapshotLatencyMs,
            staleUiRate = t.staleUiRate,
            failedActionRate = t.failedActionRate,
            consecutiveFailures = t.consecutiveFailures
        )
    }
}
