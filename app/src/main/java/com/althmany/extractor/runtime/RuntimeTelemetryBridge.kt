package com.althmany.extractor.runtime

import com.althmany.extractor.engine.RuntimeOperation
import com.althmany.extractor.engine.RuntimeOperationCoordinator
import com.althmany.extractor.engine.performance.AdaptiveRuntimePerformanceHub
import com.althmany.extractor.engine.performance.RuntimePerformanceOwner

/** Central telemetry adapter shared by Accessibility and Shizuku runtime adapters. */
object RuntimeTelemetryBridge {
    private fun owner(): RuntimePerformanceOwner? = when (RuntimeOperationCoordinator.current()) {
        RuntimeOperation.EXTRACTION -> RuntimePerformanceOwner.EXTRACTION
        RuntimeOperation.SCAN -> RuntimePerformanceOwner.SCAN
        RuntimeOperation.PUBLISH -> RuntimePerformanceOwner.PUBLISH
        RuntimeOperation.SENDER -> RuntimePerformanceOwner.SENDER
        null -> null
    }

    fun recordTarget(expectedPackage: String?, observedPackage: String?) {
        val owner = owner() ?: return
        val visible = !expectedPackage.isNullOrBlank() && observedPackage == expectedPackage
        AdaptiveRuntimePerformanceHub.recordTargetVisible(owner, visible)
    }

    fun recordSnapshot(
        expectedPackage: String?,
        observedPackage: String?,
        signature: String?,
        latencyMs: Long
    ) {
        val owner = owner() ?: return
        AdaptiveRuntimePerformanceHub.recordSnapshotLatency(owner, latencyMs)
        AdaptiveRuntimePerformanceHub.recordTargetVisible(
            owner,
            !expectedPackage.isNullOrBlank() && observedPackage == expectedPackage
        )
        AdaptiveRuntimePerformanceHub.recordSignature(owner, signature)
    }

    fun recordBackendHealthy(healthy: Boolean) {
        owner()?.let { AdaptiveRuntimePerformanceHub.recordBackendHealthy(it, healthy) }
    }

    fun recordAction(success: Boolean) {
        owner()?.let { AdaptiveRuntimePerformanceHub.recordAction(it, success) }
    }

    fun markProgress() {
        owner()?.let { AdaptiveRuntimePerformanceHub.markProgress(it) }
    }
}
