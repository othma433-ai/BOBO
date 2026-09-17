package com.althmany.extractor.runtime

import android.content.Context
import com.althmany.extractor.engine.RuntimeOperation
import com.althmany.extractor.runtime.overlay.RuntimeControlRouter

/**
 * Single process-wide runtime command gateway.
 *
 * UI surfaces (overlay, notification, app screen) submit commands here. The kernel records the
 * command through RuntimeCommandBus, mirrors state into SmartRuntimeManager when a session exists,
 * and delegates the accepted command to the one operation-scoped controller router.
 */
object SmartRuntimeKernel {
    val commandBus: RuntimeCommandBus = RuntimeCommandBus()
    val manager: SmartRuntimeManager = SmartRuntimeManager(commandBus)

    fun submit(
        context: Context,
        operation: RuntimeOperation,
        type: RuntimeCommandType,
        source: String
    ): Boolean {
        val command = RuntimeCommand(operation = operation, type = type, source = source)
        if (!commandBus.send(command)) return false

        // RuntimeManager is advisory until a domain controller has opened a session. Do not let a
        // missing session block an otherwise valid user control command during staged migration.
        runCatching { manager.applyCommand(command) }
        RuntimeControlRouter.dispatchAccepted(context, command)
        return true
    }
}
