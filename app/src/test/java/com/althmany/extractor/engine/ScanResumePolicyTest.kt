package com.althmany.extractor.engine

import com.althmany.extractor.data.ScanStatus
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ScanResumePolicyTest {

    @Test
    fun pending_isEligibleForNormalResume() {
        assertTrue(ScanResumePolicy.shouldRunInPendingOnly(ScanStatus.PENDING))
    }

    @Test
    fun completedDefinitiveResults_areNeverAutoRescanned() {
        val terminal = listOf(
            ScanStatus.DIRECT,
            ScanStatus.APPROVAL,
            ScanStatus.REQUEST_PENDING,
            ScanStatus.ALREADY_MEMBER,
            ScanStatus.EXPIRED,
            ScanStatus.INVALID,
            ScanStatus.FULL,
            ScanStatus.REMOVED,
            ScanStatus.ACCOUNT_LIMIT
        )
        terminal.forEach {
            assertFalse("status=$it", ScanResumePolicy.shouldRunInPendingOnly(it))
        }
    }

    @Test
    fun uncertainResults_areNotAutoRescanned() {
        assertFalse(ScanResumePolicy.shouldRunInPendingOnly(ScanStatus.UNKNOWN))
        assertFalse(ScanResumePolicy.shouldRunInPendingOnly(ScanStatus.NETWORK_ERROR))
        assertFalse(ScanResumePolicy.shouldRunInPendingOnly(ScanStatus.ERROR))
    }

    @Test
    fun uncertainResults_areEligibleOnlyForExplicitUncertainRecheck() {
        assertTrue(ScanResumePolicy.shouldRunInUncertainOnly(ScanStatus.UNKNOWN))
        assertTrue(ScanResumePolicy.shouldRunInUncertainOnly(ScanStatus.NETWORK_ERROR))
        assertTrue(ScanResumePolicy.shouldRunInUncertainOnly(ScanStatus.ERROR))
        assertFalse(ScanResumePolicy.shouldRunInUncertainOnly(ScanStatus.DIRECT))
    }

    @Test
    fun interruptedScanning_isRecoveredToPending() {
        assertTrue(ScanResumePolicy.shouldRecoverInterrupted(ScanStatus.SCANNING))
        assertFalse(ScanResumePolicy.shouldRecoverInterrupted(ScanStatus.DIRECT))
    }
}
