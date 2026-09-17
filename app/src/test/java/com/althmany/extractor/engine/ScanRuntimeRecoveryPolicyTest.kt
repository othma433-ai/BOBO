package com.althmany.extractor.engine

import com.althmany.extractor.profile.RuntimeBackendKind
import com.althmany.extractor.profile.RuntimeBackendPreference
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ScanRuntimeRecoveryPolicyTest {

    @Test
    fun accessibilityOnly_neverFallsBackToShizuku() {
        val plan = ScanRuntimeRecoveryPolicy.plan(
            preference = RuntimeBackendPreference.ACCESSIBILITY,
            failedBackend = RuntimeBackendKind.ACCESSIBILITY,
            accessibilityCanRecover = false,
            shizukuReady = true
        )

        assertEquals(listOf(ScanRecoveryStep.RETRY_ACCESSIBILITY), plan.steps)
        assertFalse(plan.maySwitchBackend)
    }

    @Test
    fun shizukuOnly_neverFallsBackToAccessibility() {
        val plan = ScanRuntimeRecoveryPolicy.plan(
            preference = RuntimeBackendPreference.SHIZUKU,
            failedBackend = RuntimeBackendKind.SHIZUKU,
            accessibilityCanRecover = true,
            shizukuReady = false
        )

        assertEquals(listOf(ScanRecoveryStep.RETRY_SHIZUKU), plan.steps)
        assertFalse(plan.maySwitchBackend)
    }

    @Test
    fun auto_accessibilityFailure_retriesThenFallsBackToShizuku() {
        val plan = ScanRuntimeRecoveryPolicy.plan(
            preference = RuntimeBackendPreference.AUTO,
            failedBackend = RuntimeBackendKind.ACCESSIBILITY,
            accessibilityCanRecover = false,
            shizukuReady = true
        )

        assertEquals(
            listOf(
                ScanRecoveryStep.RETRY_ACCESSIBILITY,
                ScanRecoveryStep.SWITCH_TO_SHIZUKU
            ),
            plan.steps
        )
        assertTrue(plan.maySwitchBackend)
    }

    @Test
    fun auto_shizukuFailure_canFallBackToAccessibilityWhenReachable() {
        val plan = ScanRuntimeRecoveryPolicy.plan(
            preference = RuntimeBackendPreference.AUTO,
            failedBackend = RuntimeBackendKind.SHIZUKU,
            accessibilityCanRecover = true,
            shizukuReady = false
        )

        assertEquals(
            listOf(
                ScanRecoveryStep.RETRY_SHIZUKU,
                ScanRecoveryStep.SWITCH_TO_ACCESSIBILITY
            ),
            plan.steps
        )
        assertTrue(plan.maySwitchBackend)
    }

    @Test
    fun recoveryAlwaysKeepsSameItem() {
        val plan = ScanRuntimeRecoveryPolicy.plan(
            preference = RuntimeBackendPreference.AUTO,
            failedBackend = RuntimeBackendKind.ACCESSIBILITY,
            accessibilityCanRecover = false,
            shizukuReady = true
        )

        assertTrue(plan.continueSameItem)
        assertTrue(plan.mustVerifyBeforeContinue)
    }
}
