package com.althmany.extractor.runtime

import com.althmany.extractor.engine.RuntimeOperation
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RuntimeCommandBusTest {
    @Test
    fun duplicatePauseIsCoalescedUntilCommandChanges() {
        val bus = RuntimeCommandBus()
        assertTrue(bus.send(RuntimeCommand(RuntimeOperation.SCAN, RuntimeCommandType.PAUSE)))
        assertFalse(bus.send(RuntimeCommand(RuntimeOperation.SCAN, RuntimeCommandType.PAUSE)))
        assertTrue(bus.send(RuntimeCommand(RuntimeOperation.SCAN, RuntimeCommandType.RESUME)))
        assertTrue(bus.send(RuntimeCommand(RuntimeOperation.SCAN, RuntimeCommandType.PAUSE)))
    }

    @Test
    fun resetAllowsFreshCommandForSameOperation() {
        val bus = RuntimeCommandBus()
        assertTrue(bus.send(RuntimeCommand(RuntimeOperation.EXTRACTION, RuntimeCommandType.STOP)))
        assertFalse(bus.send(RuntimeCommand(RuntimeOperation.EXTRACTION, RuntimeCommandType.STOP)))
        bus.resetForOperation(RuntimeOperation.EXTRACTION)
        assertTrue(bus.send(RuntimeCommand(RuntimeOperation.EXTRACTION, RuntimeCommandType.STOP)))
    }
}
