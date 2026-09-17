package com.althmany.extractor.runtime

class AdaptivePerformanceGovernor {
    private var lastMode: RuntimePerformanceMode = RuntimePerformanceMode.BALANCED

    fun choose(t: PerformanceTelemetry): RuntimePacing {
        val desired = when {
            t.health == RuntimeHealthState.RECOVERING || t.health == RuntimeHealthState.BLOCKED -> RuntimePerformanceMode.RECOVERY
            t.health == RuntimeHealthState.DEGRADED || t.staleUiCount >= 2 || t.failureCount >= 2 -> RuntimePerformanceMode.SAFE
            (t.snapshotLatencyMs ?: 9999L) <= 160L && t.staleUiCount == 0 && t.failureCount == 0 -> RuntimePerformanceMode.FAST
            else -> RuntimePerformanceMode.BALANCED
        }

        lastMode = desired
        return pacing(desired)
    }

    private fun pacing(mode: RuntimePerformanceMode): RuntimePacing = when (mode) {
        RuntimePerformanceMode.FAST -> RuntimePacing(mode, 35L, 45L, 2, 400L)
        RuntimePerformanceMode.BALANCED -> RuntimePacing(mode, 55L, 80L, 2, 500L)
        RuntimePerformanceMode.SAFE -> RuntimePacing(mode, 100L, 140L, 2, 700L)
        RuntimePerformanceMode.RECOVERY -> RuntimePacing(mode, 180L, 240L, 1, 900L)
    }
}
