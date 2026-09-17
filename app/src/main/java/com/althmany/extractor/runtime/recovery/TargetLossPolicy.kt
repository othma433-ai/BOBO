package com.althmany.extractor.runtime.recovery

enum class TargetPresenceMode { UNKNOWN, VISIBLE, SUSPECTED_LOST, LOST }

data class TargetLossState(
    val mode: TargetPresenceMode = TargetPresenceMode.UNKNOWN,
    val firstMissingAtMs: Long = 0L,
    val consecutiveMissing: Int = 0,
    val pauseRequired: Boolean = false
)

/**
 * Debounces target loss so transient activity/window transitions do not pause a run.
 * A real loss requires both a time budget and repeated evidence.
 */
class TargetLossPolicy(
    private val graceMs: Long = 450L,
    private val confirmationsRequired: Int = 2
) {
    init {
        require(graceMs >= 0L)
        require(confirmationsRequired >= 1)
    }

    fun observe(previous: TargetLossState, targetVisible: Boolean, nowMs: Long): TargetLossState {
        if (targetVisible) {
            return TargetLossState(mode = TargetPresenceMode.VISIBLE)
        }

        val firstMissing = if (previous.mode == TargetPresenceMode.VISIBLE || previous.firstMissingAtMs <= 0L) {
            nowMs
        } else previous.firstMissingAtMs
        val missing = if (previous.mode == TargetPresenceMode.VISIBLE || previous.mode == TargetPresenceMode.UNKNOWN) {
            1
        } else previous.consecutiveMissing + 1
        val oldEnough = nowMs - firstMissing >= graceMs
        val confirmed = missing >= confirmationsRequired
        val lost = oldEnough && confirmed
        return TargetLossState(
            mode = if (lost) TargetPresenceMode.LOST else TargetPresenceMode.SUSPECTED_LOST,
            firstMissingAtMs = firstMissing,
            consecutiveMissing = missing,
            pauseRequired = lost
        )
    }
}
