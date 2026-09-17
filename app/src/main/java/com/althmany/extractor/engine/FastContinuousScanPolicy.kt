package com.althmany.extractor.engine

enum class FastScanAction {
    SAVE_AND_NEXT,
    SHORT_VERIFY,
    SAVE_UNKNOWN_AND_NEXT
}

/**
 * Read-only fast path for Scan.
 *
 * A definitive WhatsApp signal is persisted immediately.
 * An uncertain first snapshot gets one short verification snapshot.
 * An uncertain second snapshot is persisted as UNKNOWN and the queue advances.
 *
 * Membership actions are deliberately outside this policy.
 */
object FastContinuousScanPolicy {
    const val canClickMembershipAction: Boolean = false
    const val maxSnapshotsPerLink: Int = 2
    const val shortVerifyDelayMs: Long = 120L

    fun next(decision: InviteScanDecision, snapshotIndex: Int): FastScanAction {
        require(snapshotIndex >= 1) { "snapshotIndex must start at 1" }

        if (decision.definitive) return FastScanAction.SAVE_AND_NEXT
        if (snapshotIndex < maxSnapshotsPerLink) return FastScanAction.SHORT_VERIFY
        return FastScanAction.SAVE_UNKNOWN_AND_NEXT
    }
}
