package com.althmany.extractor.runtime

import com.althmany.extractor.engine.RuntimeOperation
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference

enum class RuntimeCommandType { PAUSE, RESUME, STOP, RETURN_TO_TARGET }

data class RuntimeCommand(
    val operation: RuntimeOperation,
    val type: RuntimeCommandType,
    val source: String = "UI"
)

/**
 * Single command channel for overlay, notifications and app UI.
 * Duplicate taps are suppressed only for a short debounce window, never forever.
 */
class RuntimeCommandBus(
    private val nowMs: () -> Long = { System.currentTimeMillis() },
    private val duplicateDebounceMs: Long = 250L
) {
    private val _commands = MutableSharedFlow<RuntimeCommand>(
        replay = 0,
        extraBufferCapacity = 16
    )
    val commands: SharedFlow<RuntimeCommand> = _commands.asSharedFlow()

    private val lastAccepted = AtomicReference<RuntimeCommand?>(null)
    private val lastAcceptedAt = AtomicLong(0L)

    fun send(command: RuntimeCommand): Boolean {
        val previous = lastAccepted.get()
        val now = nowMs()
        if (
            previous?.operation == command.operation &&
            previous.type == command.type &&
            now - lastAcceptedAt.get() in 0 until duplicateDebounceMs
        ) return false

        if (!_commands.tryEmit(command)) return false
        lastAccepted.set(command)
        lastAcceptedAt.set(now)
        return true
    }

    fun resetForOperation(operation: RuntimeOperation) {
        while (true) {
            val current = lastAccepted.get() ?: return
            if (current.operation != operation) return
            if (lastAccepted.compareAndSet(current, null)) {
                lastAcceptedAt.set(0L)
                return
            }
        }
    }
}
