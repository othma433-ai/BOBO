package com.althmany.extractor.runtime

import com.althmany.extractor.profile.RuntimeBackendKind
import com.althmany.extractor.profile.RuntimeBackendPreference

enum class RuntimeHealthState { HEALTHY, DEGRADED, RECOVERING, BLOCKED }
enum class RuntimePerformanceMode { FAST, BALANCED, SAFE, RECOVERY }
enum class CircuitState { CLOSED, OPEN, HALF_OPEN }

enum class RuntimeFailureKind {
    SHIZUKU_BINDER_LOST,
    SHIZUKU_PERMISSION_LOST,
    SHIZUKU_USERSERVICE_LOST,
    SHIZUKU_SNAPSHOT_EMPTY,
    SHIZUKU_DUMP_KILLED,
    SHIZUKU_TARGET_NOT_FOREGROUND,
    SHIZUKU_STALE_TREE,
    SHIZUKU_PERSISTENT_UI_UNAVAILABLE,
    ACCESSIBILITY_ROOT_LOST,
    ACCESSIBILITY_HEARTBEAT_STALE,
    TARGET_LOST,
    NETWORK_UNAVAILABLE
}

data class BackendCapability(
    val connected: Boolean,
    val heartbeatFresh: Boolean,
    val targetVisible: Boolean,
    val profileSafe: Boolean
) {
    val healthy: Boolean get() = connected && heartbeatFresh && targetVisible && profileSafe
}

data class SmartBackendInput(
    val preference: RuntimeBackendPreference,
    val remoteTarget: Boolean,
    val accessibility: BackendCapability,
    val shizuku: BackendCapability
)

data class RuntimeBackendDecision(
    val requestedBackend: RuntimeBackendPreference,
    val effectiveBackend: RuntimeBackendKind,
    val blocked: Boolean,
    val reasonCode: String
)

data class RuntimeTelemetry(
    val backendReady: Boolean,
    val targetVerified: Boolean,
    val heartbeatFresh: Boolean,
    val networkAvailable: Boolean,
    val snapshotLatencyMs: Long?,
    val staleUiCount: Int,
    val consecutiveBackendFailures: Int,
    val recovering: Boolean,
    val permissionLost: Boolean
)

data class RuntimeHealthSnapshot(
    val state: RuntimeHealthState,
    val reasonCode: String,
    val score: Int
)

data class CircuitDecision(
    val state: CircuitState,
    val allowed: Boolean,
    val retryAfterMs: Long
)

data class PerformanceTelemetry(
    val health: RuntimeHealthState,
    val snapshotLatencyMs: Long?,
    val staleUiCount: Int,
    val failureCount: Int
)

data class RuntimePacing(
    val mode: RuntimePerformanceMode,
    val snapshotPollMs: Long,
    val settleMs: Long,
    val maxSnapshotAttempts: Int,
    val notificationThrottleMs: Long
)
