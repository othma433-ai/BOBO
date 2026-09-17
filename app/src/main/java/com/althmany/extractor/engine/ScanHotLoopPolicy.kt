package com.althmany.extractor.engine

/**
 * Performance policy for the Scan hot loop.
 *
 * Durability writes (mark SCANNING + final result) are deliberately NOT removed.
 * Optimisation is limited to UI/notification/statistics churn and polling cadence.
 */
object ScanHotLoopPolicy {
    const val notificationIntervalMs: Long = 400L
    const val statsRefreshEveryItems: Int = 20
    const val stalePollDelayMs: Long = 35L
    const val maxSnapshotsPerLink: Int = 2

    fun shouldNotify(lastMs: Long, nowMs: Long): Boolean =
        lastMs <= 0L || nowMs - lastMs >= notificationIntervalMs

    fun shouldRefreshStats(completedItems: Int, finalItem: Boolean = false): Boolean =
        finalItem || (completedItems > 0 && completedItems % statsRefreshEveryItems == 0)
}
