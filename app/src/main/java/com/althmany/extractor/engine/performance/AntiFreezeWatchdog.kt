package com.althmany.extractor.engine.performance

enum class StallKind {
    NONE,
    SLOW,
    STALE_UI,
    BACKEND_DEGRADED,
    TARGET_LOST,
    HARD_STALL
}

data class WatchdogSample(
    val nowMs: Long,
    val lastProgressMs: Long,
    val repeatedSignatureCount: Int,
    val backendHealthy: Boolean,
    val targetVisible: Boolean
)

data class WatchdogDecision(
    val kind: StallKind,
    val pauseInput: Boolean,
    val requestRecovery: Boolean,
    val reason: String
)

object AntiFreezeWatchdog {
    fun evaluate(sample: WatchdogSample): WatchdogDecision {
        if (!sample.targetVisible) {
            return WatchdogDecision(
                StallKind.TARGET_LOST, true, true, "target lost"
            )
        }
        if (!sample.backendHealthy) {
            return WatchdogDecision(
                StallKind.BACKEND_DEGRADED, true, true, "backend unhealthy"
            )
        }

        val noProgressMs = (sample.nowMs - sample.lastProgressMs).coerceAtLeast(0)
        if (sample.repeatedSignatureCount >= 5 && noProgressMs >= 2_500) {
            return WatchdogDecision(
                StallKind.STALE_UI, true, true, "repeated UI signature"
            )
        }
        if (noProgressMs >= 8_000) {
            return WatchdogDecision(
                StallKind.HARD_STALL, true, true, "no progress >= 8s"
            )
        }
        if (noProgressMs >= 3_000) {
            return WatchdogDecision(
                StallKind.SLOW, false, false, "slow progress"
            )
        }
        return WatchdogDecision(
            StallKind.NONE, false, false, "healthy"
        )
    }
}
