package com.althmany.extractor.runtime

import com.althmany.extractor.engine.RuntimeOperation
import com.althmany.extractor.profile.RuntimeBackendKind
import com.althmany.extractor.profile.RuntimeBackendPreference
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SmartRuntimeManagerTest {
    private fun cap(ok: Boolean, safe: Boolean = true) = BackendCapability(ok, ok, ok, safe)

    @Test
    fun localAutoStartsOnAccessibilityWhenBothAreHealthy() {
        val manager = SmartRuntimeManager(RuntimeCommandBus())
        val session = manager.begin(
            RuntimeOperation.SCAN, "com.whatsapp", 0, false,
            RuntimeBackendPreference.AUTO, cap(true), cap(true)
        )
        assertEquals(RuntimeBackendKind.ACCESSIBILITY, session.effectiveBackend)
        assertEquals(RuntimeSessionState.RUNNING, session.state)
        assertTrue(session.inputAuthorized)
    }

    @Test
    fun targetLossPausesAndKeepsCheckpoint() {
        val manager = SmartRuntimeManager(RuntimeCommandBus(), nowMs = { 500L })
        manager.begin(
            RuntimeOperation.EXTRACTION, "com.whatsapp", 0, false,
            RuntimeBackendPreference.AUTO, cap(true), cap(true)
        )
        manager.checkpoint("group-17", "message-991", 44)
        val paused = manager.markTargetLost()
        assertEquals(RuntimeSessionState.PAUSED_OUTSIDE_TARGET, paused.state)
        assertEquals("group-17", paused.checkpoint?.itemId)
        assertFalse(paused.inputAuthorized)
    }

    @Test
    fun autoHandoverKeepsSameItemAndRequiresFreshVerification() {
        val manager = SmartRuntimeManager(RuntimeCommandBus())
        manager.begin(
            RuntimeOperation.SCAN, "com.whatsapp.w4b", 0, false,
            RuntimeBackendPreference.AUTO, cap(false), cap(true)
        )
        manager.checkpoint("79", "scan:79", 100)
        val plan = manager.requestHandover(
            RuntimeFailureKind.SHIZUKU_DUMP_KILLED,
            accessibility = cap(true),
            shizuku = cap(false)
        )
        assertTrue(plan.allowed)
        assertTrue(plan.sameItem)
        assertTrue(plan.requireFreshTargetVerification)
        assertEquals(RuntimeBackendKind.ACCESSIBILITY, plan.toBackend)

        val recovering = manager.applyHandover(plan)
        assertEquals("79", recovering.checkpoint?.itemId)
        assertFalse(recovering.inputAuthorized)
        assertEquals(RuntimeSessionState.RECOVERING, recovering.state)

        val resumed = manager.markFreshTargetVerified(222)
        assertEquals(RuntimeBackendKind.ACCESSIBILITY, resumed.effectiveBackend)
        assertEquals("79", resumed.checkpoint?.itemId)
        assertTrue(resumed.inputAuthorized)
    }

    @Test
    fun manualBackendNeverCrossesOver() {
        val manager = SmartRuntimeManager(RuntimeCommandBus())
        manager.begin(
            RuntimeOperation.SCAN, "com.whatsapp.w4b", 0, false,
            RuntimeBackendPreference.SHIZUKU, cap(true), cap(true)
        )
        val plan = manager.requestHandover(
            RuntimeFailureKind.SHIZUKU_DUMP_KILLED,
            accessibility = cap(true),
            shizuku = cap(false)
        )
        assertFalse(plan.allowed)
        assertEquals(RuntimeBackendKind.NONE, plan.toBackend)
    }

    @Test
    fun wrongOwnerCommandIsIgnored() {
        val manager = SmartRuntimeManager(RuntimeCommandBus())
        val before = manager.begin(
            RuntimeOperation.SCAN, "com.whatsapp", 0, false,
            RuntimeBackendPreference.AUTO, cap(true), cap(false)
        )
        val after = manager.applyCommand(RuntimeCommand(RuntimeOperation.EXTRACTION, RuntimeCommandType.STOP))
        assertEquals(before, after)
    }
}
