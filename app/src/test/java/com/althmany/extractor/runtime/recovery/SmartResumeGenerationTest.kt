package com.althmany.extractor.runtime.recovery

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SmartResumeGenerationTest {
    @Test
    fun remoteResumeAcceptsFreshFrameEvenWhenContentSignatureIsUnchanged() {
        val gate = SmartResumeGate(confirmationsRequired = 2)
        var state = ResumeVerificationState(
            previousSignature = 77,
            requireSignatureChange = true,
            lastEventGeneration = 100
        )
        state = gate.observe(state, targetVisible = true, signature = 77, eventGeneration = 101)
        assertFalse(state.ready)
        state = gate.observe(state, targetVisible = true, signature = 77, eventGeneration = 102)
        assertTrue(state.ready)
    }
}
