package com.althmany.extractor.runtime.overlay

import android.content.Context
import android.content.Intent
import com.althmany.extractor.engine.ExtractionController
import com.althmany.extractor.engine.PublishController
import com.althmany.extractor.engine.RuntimeOperation
import com.althmany.extractor.engine.ScanController
import com.althmany.extractor.profile.UnifiedRuntimeTargetStore
import com.althmany.extractor.runtime.RuntimeCommand
import com.althmany.extractor.runtime.RuntimeCommandType
import com.althmany.extractor.runtime.SmartRuntimeKernel
import com.althmany.extractor.runtime.recovery.TargetRecoveryCoordinator
import com.althmany.extractor.shizuku.ShizukuBridge
import com.althmany.groupmanager.receiver.AutomationActionReceiver
import com.althmany.groupmanager.util.QuickJoinNotification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * One control router for overlay/notification/app UI. Public methods submit through the command bus.
 * Direct methods are execution hooks used only after a command was accepted or after fresh-target
 * verification succeeds.
 */
object RuntimeControlRouter {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun pause(context: Context, operation: RuntimeOperation): Boolean =
        SmartRuntimeKernel.submit(context, operation, RuntimeCommandType.PAUSE, "RUNTIME_CONTROLS")

    fun resume(context: Context, operation: RuntimeOperation): Boolean =
        SmartRuntimeKernel.submit(context, operation, RuntimeCommandType.RESUME, "RUNTIME_CONTROLS")

    fun stop(context: Context, operation: RuntimeOperation): Boolean =
        SmartRuntimeKernel.submit(context, operation, RuntimeCommandType.STOP, "RUNTIME_CONTROLS")

    fun returnToTarget(context: Context, operation: RuntimeOperation): Boolean =
        SmartRuntimeKernel.submit(context, operation, RuntimeCommandType.RETURN_TO_TARGET, "RUNTIME_CONTROLS")

    fun dispatchAccepted(context: Context, command: RuntimeCommand) {
        when (command.type) {
            RuntimeCommandType.PAUSE -> pauseDirect(context, command.operation)
            RuntimeCommandType.RESUME -> TargetRecoveryCoordinator.requestSmartResume(context, command.operation)
            RuntimeCommandType.STOP -> stopDirect(context, command.operation)
            RuntimeCommandType.RETURN_TO_TARGET -> returnToTargetDirect(context)
        }
    }

    fun pauseDirect(context: Context, operation: RuntimeOperation) {
        when (operation) {
            RuntimeOperation.EXTRACTION -> ExtractionController.pause()
            RuntimeOperation.SCAN -> ScanController.pause()
            RuntimeOperation.PUBLISH -> PublishController.pause()
            RuntimeOperation.SENDER -> senderToggle(context)
        }
    }

    fun resumeDirect(context: Context, operation: RuntimeOperation) {
        when (operation) {
            RuntimeOperation.EXTRACTION -> ExtractionController.resume()
            RuntimeOperation.SCAN -> ScanController.resume()
            RuntimeOperation.PUBLISH -> PublishController.resume()
            RuntimeOperation.SENDER -> senderToggle(context)
        }
    }

    fun stopDirect(context: Context, operation: RuntimeOperation) {
        when (operation) {
            RuntimeOperation.EXTRACTION -> ExtractionController.stop()
            RuntimeOperation.SCAN -> ScanController.stop()
            RuntimeOperation.PUBLISH -> PublishController.stop()
            RuntimeOperation.SENDER -> context.sendBroadcast(
                Intent(context, AutomationActionReceiver::class.java)
                    .setAction(QuickJoinNotification.ACTION_STOP_AUTOMATION)
            )
        }
        TargetRecoveryCoordinator.onOperationStopped(context)
    }

    /** Returns true when a launch request was accepted. Verification is performed separately. */
    fun returnToTargetDirect(context: Context): Boolean {
        val appContext = context.applicationContext
        val target = UnifiedRuntimeTargetStore.resolve(appContext)
        val pkg = target.selectedWhatsAppPackage ?: return false

        if (target.remoteTarget) {
            val status = runCatching { ShizukuBridge.status() }.getOrNull() ?: return false
            if (!status.ready) return false
            scope.launch {
                ShizukuBridge.launchPackage(appContext, pkg, target.targetAndroidUserId)
            }
            return true
        }

        val launch = appContext.packageManager.getLaunchIntentForPackage(pkg) ?: return false
        launch.setPackage(pkg)
        launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        appContext.startActivity(launch)
        return true
    }

    private fun senderToggle(context: Context) {
        context.sendBroadcast(
            Intent(context, AutomationActionReceiver::class.java)
                .setAction(QuickJoinNotification.ACTION_TOGGLE_PAUSE_AUTOMATION)
        )
    }
}
