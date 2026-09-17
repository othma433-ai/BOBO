package com.althmany.extractor.engine.performance

class TelemetryWindow(
    private val maxSamples: Int = 24
) {
    private val uiLatency = ArrayDeque<Long>()
    private val snapshotLatency = ArrayDeque<Long>()
    private val transitionLatency = ArrayDeque<Long>()
    private var staleEvents: Int = 0
    private var actionFailures: Int = 0
    private var actions: Int = 0

    fun recordUiLatency(ms: Long) = add(uiLatency, ms)
    fun recordSnapshotLatency(ms: Long) = add(snapshotLatency, ms)
    fun recordTransitionLatency(ms: Long) = add(transitionLatency, ms)

    fun recordAction(success: Boolean) {
        actions = (actions + 1).coerceAtMost(maxSamples)
        if (!success) actionFailures = (actionFailures + 1).coerceAtMost(maxSamples)
        trimCounters()
    }

    fun recordStaleUi() {
        staleEvents = (staleEvents + 1).coerceAtMost(maxSamples)
    }

    fun snapshot(
        consecutiveFailures: Int,
        targetVisible: Boolean,
        backendHealthy: Boolean
    ): RuntimeTelemetry {
        val denom = maxOf(1, actions)
        return RuntimeTelemetry(
            uiLatencyMs = median(uiLatency),
            snapshotLatencyMs = median(snapshotLatency),
            staleUiRate = staleEvents.toDouble() / maxOf(1, maxSamples),
            failedActionRate = actionFailures.toDouble() / denom,
            transitionLatencyMs = median(transitionLatency),
            consecutiveFailures = consecutiveFailures,
            targetVisible = targetVisible,
            backendHealthy = backendHealthy
        )
    }

    private fun add(queue: ArrayDeque<Long>, value: Long) {
        queue.add(value.coerceAtLeast(0))
        while (queue.size > maxSamples) queue.removeFirst()
    }

    private fun trimCounters() {
        if (actions < maxSamples) return
        actionFailures = actionFailures.coerceAtMost(actions)
    }

    private fun median(values: Collection<Long>): Long {
        if (values.isEmpty()) return 0
        val s = values.sorted()
        return s[s.size / 2]
    }
}
