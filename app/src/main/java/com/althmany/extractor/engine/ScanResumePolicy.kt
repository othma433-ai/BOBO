package com.althmany.extractor.engine

import com.althmany.extractor.data.ScanStatus

/**
 * Canonical resume policy for Scan.
 *
 * Normal resume is single-pass: only PENDING rows run.
 * SCANNING rows are recovered to PENDING after interruption.
 * UNKNOWN / NETWORK_ERROR / ERROR require an explicit UNCERTAIN_ONLY recheck.
 * Confirmed results are never selected automatically again.
 */
object ScanResumePolicy {
    fun shouldRunInPendingOnly(status: ScanStatus): Boolean =
        status == ScanStatus.PENDING

    fun shouldRunInUncertainOnly(status: ScanStatus): Boolean =
        status in setOf(
            ScanStatus.UNKNOWN,
            ScanStatus.NETWORK_ERROR,
            ScanStatus.ERROR
        )

    fun shouldRecoverInterrupted(status: ScanStatus): Boolean =
        status == ScanStatus.SCANNING
}
