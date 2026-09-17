package com.althmany.extractor.runtime

import com.althmany.extractor.engine.RuntimeOperation
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RuntimeCommandBusDebounceTest {
    @Test
    fun repeatedCommandIsOnlySuppressedInsideDebounceWindow() {
        var now = 1_000L
        val bus = RuntimeCommandBus(nowMs = { now }, duplicateDebounceMs = 250L)
        val command = RuntimeCommand(RuntimeOperation.SCAN, RuntimeCommandType.RETURN_TO_TARGET)

        assertTrue(bus.send(command))
        now += 100L
        assertFalse(bus.send(command))
        now += 300L
        assertTrue(bus.send(command))
    }
}
