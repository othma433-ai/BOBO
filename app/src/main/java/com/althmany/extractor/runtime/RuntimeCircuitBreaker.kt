package com.althmany.extractor.runtime

class RuntimeCircuitBreaker(
    private val commandKillThreshold: Int = 2,
    private val commandKillWindowMs: Long = 5_000L,
    private val openDurationMs: Long = 12_000L
) {
    private val killTimes = ArrayDeque<Long>()
    private var openUntilMs: Long = 0L
    private var state: CircuitState = CircuitState.CLOSED

    fun record(kind: RuntimeFailureKind, nowMs: Long): CircuitDecision {
        if (kind != RuntimeFailureKind.SHIZUKU_DUMP_KILLED) return snapshot(nowMs)
        prune(nowMs)
        killTimes.add(nowMs)
        if (killTimes.size >= commandKillThreshold) {
            state = CircuitState.OPEN
            openUntilMs = nowMs + openDurationMs
            killTimes.clear()
        }
        return snapshot(nowMs)
    }

    fun canAttempt(nowMs: Long): CircuitDecision {
        if (state == CircuitState.OPEN && nowMs >= openUntilMs) {
            state = CircuitState.HALF_OPEN
        }
        return snapshot(nowMs)
    }

    fun recordSuccess(nowMs: Long): CircuitDecision {
        state = CircuitState.CLOSED
        openUntilMs = 0L
        killTimes.clear()
        return snapshot(nowMs)
    }

    fun snapshot(nowMs: Long): CircuitDecision {
        if (state == CircuitState.OPEN && nowMs >= openUntilMs) state = CircuitState.HALF_OPEN
        val retry = if (state == CircuitState.OPEN) (openUntilMs - nowMs).coerceAtLeast(0L) else 0L
        return CircuitDecision(
            state = state,
            allowed = state != CircuitState.OPEN,
            retryAfterMs = retry
        )
    }

    private fun prune(nowMs: Long) {
        while (killTimes.isNotEmpty() && nowMs - killTimes.first() > commandKillWindowMs) {
            killTimes.removeFirst()
        }
    }
}
