package com.althmany.extractor.engine

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ScanScreenFreshnessGateTest {

    @Test
    fun identicalSnapshotAfterOpeningNextLink_isRejectedAsStale() {
        val before = ScanScreenFreshnessGate.signature(listOf("Old Group", "Join group"))
        val after = ScanScreenFreshnessGate.signature(listOf("Old Group", "Join group"))

        assertFalse(
            ScanScreenFreshnessGate.isFresh(
                baselineSignature = before,
                candidateSignature = after,
                observedUiEventAfterOpen = false
            )
        )
    }

    @Test
    fun changedSnapshot_isFreshWithoutWaitingForExtraEvent() {
        val before = ScanScreenFreshnessGate.signature(listOf("Old Group", "Join group"))
        val after = ScanScreenFreshnessGate.signature(listOf("New Group", "Request to join"))

        assertTrue(
            ScanScreenFreshnessGate.isFresh(
                baselineSignature = before,
                candidateSignature = after,
                observedUiEventAfterOpen = false
            )
        )
    }

    @Test
    fun sameSignatureCanBeAcceptedWhenNewUiEventConfirmsFreshNavigation() {
        val sig = ScanScreenFreshnessGate.signature(listOf("WhatsApp", "Network error"))

        assertTrue(
            ScanScreenFreshnessGate.isFresh(
                baselineSignature = sig,
                candidateSignature = sig,
                observedUiEventAfterOpen = true
            )
        )
    }

    @Test
    fun signatureIgnoresOrderingNoise() {
        val a = ScanScreenFreshnessGate.signature(listOf("Join group", "My Group", "12 members"))
        val b = ScanScreenFreshnessGate.signature(listOf("12 members", "My Group", "Join group"))

        assertTrue(a == b)
    }

    @Test
    fun materiallyDifferentTextProducesDifferentSignature() {
        val a = ScanScreenFreshnessGate.signature(listOf("Group A", "Join group"))
        val b = ScanScreenFreshnessGate.signature(listOf("Group B", "Join group"))

        assertNotEquals(a, b)
    }
}
