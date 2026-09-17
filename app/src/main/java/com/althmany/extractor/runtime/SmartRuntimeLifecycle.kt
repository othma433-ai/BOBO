package com.althmany.extractor.runtime

import android.content.Context
import com.althmany.extractor.engine.RuntimeOperation
import com.althmany.extractor.runtime.overlay.FloatingControlService
import com.althmany.extractor.runtime.recovery.TargetRecoveryCoordinator

/** Application-scoped lifecycle bridge for the single RuntimeOperationCoordinator owner. */
object SmartRuntimeLifecycle {
    @Volatile private var appContext: Context? = null

    fun initialize(context: Context) {
        appContext = context.applicationContext
    }

    fun onAcquired(operation: RuntimeOperation) {
        val context = appContext ?: return
        TargetRecoveryCoordinator.onOperationStarted(context, operation)
        FloatingControlService.start(context)
    }

    fun onReleased(operation: RuntimeOperation) {
        val context = appContext ?: return
        TargetRecoveryCoordinator.onOperationStopped(context)
    }
}
