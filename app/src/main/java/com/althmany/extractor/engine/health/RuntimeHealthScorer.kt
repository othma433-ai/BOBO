package com.althmany.extractor.engine.health

enum class SmartHealthLevel {
    HEALTHY, DEGRADED, RECOVERING, BLOCKED
}

data class RuntimeHealthInput(
    val targetVisible: Boolean,
    val backendHealthy: Boolean,
    val uiLatencyMs: Long,
    val snapshotLatencyMs: Long,
    val staleUiRate: Double,
    val failedActionRate: Double,
    val consecutiveFailures: Int,
    val recoveryActive: Boolean
)

data class RuntimeHealthScore(
    val level: SmartHealthLevel,
    val score: Int,
    val reason: String
)

object RuntimeHealthScorer {
    fun score(input: RuntimeHealthInput): RuntimeHealthScore {
        if (!input.targetVisible) {
            return RuntimeHealthScore(
                SmartHealthLevel.BLOCKED,
                0,
                "target-not-visible"
            )
        }
        if (!input.backendHealthy) {
            return RuntimeHealthScore(
                if (input.recoveryActive) SmartHealthLevel.RECOVERING else SmartHealthLevel.BLOCKED,
                if (input.recoveryActive) 25 else 5,
                "backend-unhealthy"
            )
        }
        if (input.recoveryActive) {
            return RuntimeHealthScore(
                SmartHealthLevel.RECOVERING,
                45,
                "recovery-active"
            )
        }

        var score = 100
        if (input.uiLatencyMs > 900) score -= 15
        else if (input.uiLatencyMs > 450) score -= 7

        if (input.snapshotLatencyMs > 1200) score -= 15
        else if (input.snapshotLatencyMs > 600) score -= 7

        score -= (input.staleUiRate.coerceIn(0.0, 1.0) * 30.0).toInt()
        score -= (input.failedActionRate.coerceIn(0.0, 1.0) * 30.0).toInt()
        score -= (input.consecutiveFailures.coerceIn(0, 3) * 8)

        score = score.coerceIn(0, 100)

        return if (score >= 75) {
            RuntimeHealthScore(SmartHealthLevel.HEALTHY, score, "runtime-stable")
        } else {
            RuntimeHealthScore(SmartHealthLevel.DEGRADED, score, "runtime-degraded")
        }
    }
}
