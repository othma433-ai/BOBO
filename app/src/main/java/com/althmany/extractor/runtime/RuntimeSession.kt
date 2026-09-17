package com.althmany.extractor.runtime

import com.althmany.extractor.engine.RuntimeOperation
import com.althmany.extractor.profile.RuntimeBackendKind
import com.althmany.extractor.profile.RuntimeBackendPreference

enum class RuntimeSessionState {
    IDLE,
    RESOLVING_TARGET,
    CHECKING_BACKEND,
    READY,
    RUNNING,
    PAUSED,
    PAUSED_OUTSIDE_TARGET,
    RECOVERING,
    STOPPED,
    COMPLETED,
    ERROR
}

data class RuntimeCheckpoint(
    val itemId: String,
    val checkpointRef: String? = null,
    val snapshotSignature: Int? = null,
    val updatedAtMs: Long = 0L
)

data class RuntimeSession(
    val sessionId: Long,
    val operation: RuntimeOperation,
    val targetPackage: String,
    val targetAndroidUserId: Int,
    val remoteTarget: Boolean,
    val requestedBackend: RuntimeBackendPreference,
    val effectiveBackend: RuntimeBackendKind,
    val state: RuntimeSessionState,
    val checkpoint: RuntimeCheckpoint? = null,
    val health: RuntimeHealthSnapshot = RuntimeHealthSnapshot(RuntimeHealthState.HEALTHY, "SESSION_STARTED", 100),
    val pacing: RuntimePacing = RuntimePacing(RuntimePerformanceMode.BALANCED, 55L, 80L, 2, 500L),
    val failureCounts: Map<RuntimeFailureKind, Int> = emptyMap(),
    val lastSnapshotSignature: Int? = null,
    val lastRecoveryReason: String? = null,
    val inputAuthorized: Boolean = false,
    val generation: Long = 0L
) {
    val terminal: Boolean
        get() = state in setOf(RuntimeSessionState.STOPPED, RuntimeSessionState.COMPLETED, RuntimeSessionState.ERROR)
}

data class RuntimeHandoverPlan(
    val allowed: Boolean,
    val fromBackend: RuntimeBackendKind,
    val toBackend: RuntimeBackendKind,
    val reasonCode: String,
    val sameItem: Boolean = true,
    val requireFreshTargetVerification: Boolean = true
)
