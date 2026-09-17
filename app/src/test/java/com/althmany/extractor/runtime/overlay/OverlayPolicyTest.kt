package com.althmany.extractor.runtime.overlay

import org.junit.Assert.*
import org.junit.Test

class OverlayPolicyTest {
    @Test fun inactive_isHidden() {
        val ui = OverlayPolicy.resolve(false, true, OverlaySessionState.RUNNING)
        assertEquals(OverlayMode.HIDDEN, ui.mode)
        assertFalse(ui.showStop)
    }

    @Test fun activeWithoutPermission_usesNotificationFallback() {
        val ui = OverlayPolicy.resolve(true, false, OverlaySessionState.RUNNING)
        assertEquals(OverlayMode.NOTIFICATION_ONLY, ui.mode)
        assertTrue(ui.showPause)
        assertTrue(ui.showStop)
    }

    @Test fun outsideTarget_showsResumeAndReturn() {
        val ui = OverlayPolicy.resolve(true, true, OverlaySessionState.PAUSED_OUTSIDE_TARGET)
        assertEquals(OverlayMode.FLOATING, ui.mode)
        assertFalse(ui.showPause)
        assertTrue(ui.showResume)
        assertTrue(ui.showReturnToTarget)
    }

    @Test fun terminal_isHidden() {
        val ui = OverlayPolicy.resolve(true, true, OverlaySessionState.TERMINAL)
        assertEquals(OverlayMode.HIDDEN, ui.mode)
    }
}
