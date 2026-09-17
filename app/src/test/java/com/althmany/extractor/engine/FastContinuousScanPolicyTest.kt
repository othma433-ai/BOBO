package com.althmany.extractor.engine

import com.althmany.extractor.data.ScanStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FastContinuousScanPolicyTest {
    @Test
    fun definitiveDecision_movesImmediatelyToNextLink() {
        val decision = InviteScanDecision(
            status = ScanStatus.DIRECT,
            detail = "direct",
            definitive = true,
            confidence = 99,
            signalCode = "DIRECT_JOIN"
        )

        val action = FastContinuousScanPolicy.next(decision, snapshotIndex = 1)

        assertEquals(FastScanAction.SAVE_AND_NEXT, action)
    }

    @Test
    fun uncertainFirstSnapshot_requestsOneShortVerification() {
        val decision = InviteScanDecision(
            status = ScanStatus.UNKNOWN,
            detail = "waiting",
            definitive = false,
            confidence = 30,
            signalCode = "PREVIEW_VISIBLE"
        )

        val action = FastContinuousScanPolicy.next(decision, snapshotIndex = 1)

        assertEquals(FastScanAction.SHORT_VERIFY, action)
    }

    @Test
    fun uncertainSecondSnapshot_isSavedUnknownInsteadOfWaitingLong() {
        val decision = InviteScanDecision(
            status = ScanStatus.UNKNOWN,
            detail = "waiting",
            definitive = false,
            confidence = 30,
            signalCode = "PREVIEW_VISIBLE"
        )

        val action = FastContinuousScanPolicy.next(decision, snapshotIndex = 2)

        assertEquals(FastScanAction.SAVE_UNKNOWN_AND_NEXT, action)
    }

    @Test
    fun policyNeverRequestsMembershipAction() {
        assertFalse(FastContinuousScanPolicy.canClickMembershipAction)
    }
}
