package com.althmany.extractor.runtime.recovery

import android.content.Context
import android.os.SystemClock
import com.althmany.extractor.engine.RuntimeOperation
import com.althmany.extractor.engine.RuntimeOperationCoordinator
import com.althmany.extractor.profile.UnifiedRuntimeTargetStore
import com.althmany.extractor.runtime.SmartRuntimeKernel
import com.althmany.extractor.runtime.overlay.FloatingControlService
import com.althmany.extractor.runtime.overlay.RuntimeControlRouter
import java.util.concurrent.atomic.AtomicLong

/**
 * Process-wide target-loss guard. It freezes the current owner, persists recovery intent, and only
 * authorizes resume after exact-target freshness proof.
 */
object TargetRecoveryCoordinator {
    private val supervisor = TargetLossSupervisor()
    private val eventGeneration = AtomicLong(0L)
    @Volatile private var pauseIssuedForLoss = false

    fun onOperationStarted(context: Context, operation: RuntimeOperation) {
        val store = RuntimeRecoveryStore(context)
        val target = UnifiedRuntimeTargetStore.resolve(context)
        store.operation = operation
        store.expectedPackage = target.selectedWhatsAppPackage
        store.remoteTarget = target.remoteTarget
        store.resumePending = false
        store.pausedOutsideTarget = false
        store.previousSignature = null
        store.processRecoveryRequired = false
        supervisor.reset()
        pauseIssuedForLoss = false
    }

    /** Call on every Accessibility package event, including non-WhatsApp packages. */
    fun onAccessibilityPackage(context: Context, observedPackage: String?) {
        val owner = RuntimeOperationCoordinator.current() ?: return
        val store = RuntimeRecoveryStore(context)
        val resolved = UnifiedRuntimeTargetStore.resolve(context)
        val expected = store.expectedPackage ?: resolved.selectedWhatsAppPackage ?: return
        if (store.operation != owner) store.operation = owner
        if (store.expectedPackage.isNullOrBlank()) store.expectedPackage = expected
        store.remoteTarget = resolved.remoteTarget
        val generation = eventGeneration.incrementAndGet()
        val visible = observedPackage == expected

        if (store.resumePending) {
            val ready = supervisor.observeResumeFrame(
                targetVisible = visible,
                signature = null,
                eventGeneration = generation
            )
            if (ready) completeVerifiedResume(context, owner, null)
            return
        }

        val paused = supervisor.observeTarget(visible, SystemClock.elapsedRealtime())
        if (paused && !pauseIssuedForLoss) pauseForTargetLoss(context, owner, "ACCESSIBILITY_TARGET_LOST")
    }

    /** Shizuku/remote snapshot path. observedPackage must come from the actual UI tree root. */
    fun onShizukuSnapshot(context: Context, observedPackage: String?, signature: Int) {
        val owner = RuntimeOperationCoordinator.current() ?: return
        val generation = eventGeneration.incrementAndGet()
        val store = RuntimeRecoveryStore(context)
        val expected = store.expectedPackage ?: UnifiedRuntimeTargetStore.selectedPackage(context) ?: return
        if (store.operation != owner) store.operation = owner
        if (store.expectedPackage.isNullOrBlank()) store.expectedPackage = expected
        val visible = observedPackage == expected

        if (!store.resumePending) {
            val paused = supervisor.observeTarget(visible, SystemClock.elapsedRealtime())
            store.previousSignature = signature
            if (paused && !pauseIssuedForLoss) pauseForTargetLoss(context, owner, "SHIZUKU_TARGET_LOST")
            return
        }

        val ready = supervisor.observeResumeFrame(visible, signature, generation)
        store.previousSignature = signature
        if (ready) completeVerifiedResume(context, owner, signature)
    }

    fun requestSmartResume(context: Context, operation: RuntimeOperation) {
        val store = RuntimeRecoveryStore(context)
        if (store.processRecoveryRequired) {
            // Android recreated the process: never resume UI input blindly. Bring the exact target
            // forward, keep the checkpoint frozen, and require the app's persisted-domain recovery
            // path to reconstruct the operation before any future input authorization.
            RuntimeControlRouter.returnToTargetDirect(context)
            return
        }
        if (!store.pausedOutsideTarget) {
            RuntimeControlRouter.resumeDirect(context, operation)
            return
        }
        store.operation = operation
        store.resumePending = true
        supervisor.requestResume(
            previousSignature = store.previousSignature,
            requireSignatureChange = store.remoteTarget,
            baselineGeneration = eventGeneration.get()
        )
        // Resume is intentionally deferred. The exact target must be returned to and verified first.
        val accepted = RuntimeControlRouter.returnToTargetDirect(context)
        if (!accepted) {
            store.resumePending = false
            // remain paused; controls stay available for explicit retry/stop
        }
    }

    fun onOperationStopped(context: Context) {
        RuntimeRecoveryStore(context).clear()
        supervisor.reset()
        pauseIssuedForLoss = false
        FloatingControlService.stop(context)
    }

    fun restorePendingState(context: Context) {
        val store = RuntimeRecoveryStore(context)
        if (!store.pausedOutsideTarget) return
        val operation = store.operation ?: return
        // Restore only ownership metadata, not automation execution. This is RECOVERY_REQUIRED,
        // not an automatic restart of clicks after process death.
        RuntimeOperationCoordinator.restoreForRecovery(operation)
        store.processRecoveryRequired = true
        supervisor.requestResume(
            store.previousSignature,
            store.remoteTarget,
            baselineGeneration = eventGeneration.get()
        )
        store.operation = operation
        FloatingControlService.start(context)
    }

    private fun pauseForTargetLoss(context: Context, owner: RuntimeOperation, reason: String) {
        pauseIssuedForLoss = true
        val store = RuntimeRecoveryStore(context)
        store.pausedOutsideTarget = true
        store.resumePending = false
        runCatching { SmartRuntimeKernel.manager.markTargetLost(reason) }
        RuntimeControlRouter.pauseDirect(context, owner)
        FloatingControlService.start(context)
    }

    private fun completeVerifiedResume(context: Context, owner: RuntimeOperation, signature: Int?) {
        val store = RuntimeRecoveryStore(context)
        store.resumePending = false
        store.pausedOutsideTarget = false
        pauseIssuedForLoss = false
        runCatching { SmartRuntimeKernel.manager.markFreshTargetVerified(signature) }
        RuntimeControlRouter.resumeDirect(context, owner)
    }
}
