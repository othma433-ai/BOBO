package com.althmany.extractor.runtime

object RuntimeHealthMonitor {
    fun evaluate(t: RuntimeTelemetry): RuntimeHealthSnapshot {
        if (t.permissionLost) return RuntimeHealthSnapshot(RuntimeHealthState.BLOCKED, "PERMISSION_LOST", 0)
        if (!t.networkAvailable) return RuntimeHealthSnapshot(RuntimeHealthState.RECOVERING, "NETWORK_WAIT", 35)
        if (t.recovering) return RuntimeHealthSnapshot(RuntimeHealthState.RECOVERING, "RECOVERY_ACTIVE", 45)
        if (!t.backendReady) return RuntimeHealthSnapshot(RuntimeHealthState.BLOCKED, "BACKEND_NOT_READY", 10)
        if (!t.targetVerified) return RuntimeHealthSnapshot(RuntimeHealthState.RECOVERING, "TARGET_NOT_VERIFIED", 35)
        if (!t.heartbeatFresh) return RuntimeHealthSnapshot(RuntimeHealthState.DEGRADED, "HEARTBEAT_STALE", 55)

        var score = 100
        if ((t.snapshotLatencyMs ?: 0L) >= 700L) score -= 20
        if (t.staleUiCount >= 2) score -= 20
        score -= (t.consecutiveBackendFailures.coerceAtMost(3) * 10)

        return if (score >= 75) {
            RuntimeHealthSnapshot(RuntimeHealthState.HEALTHY, "HEALTHY", score)
        } else {
            RuntimeHealthSnapshot(RuntimeHealthState.DEGRADED, "RUNTIME_PRESSURE", score.coerceAtLeast(0))
        }
    }
}
