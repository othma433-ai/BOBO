package com.althmany.extractor.runtime

import com.althmany.extractor.engine.RuntimeOperation
import com.althmany.extractor.profile.RuntimeBackendKind
import com.althmany.extractor.profile.RuntimeBackendPreference
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicLong

/**
 * Backend-neutral session coordinator. It owns state transitions only; domain controllers still own
 * Scan/Extraction/Join/Publish algorithms and actual UI actions.
 */
class SmartRuntimeManager(
    val commandBus: RuntimeCommandBus,
    private val nowMs: () -> Long = { System.currentTimeMillis() }
) {
    private val ids = AtomicLong(1L)
    private val governor = AdaptivePerformanceGovernor()
    private val _session = MutableStateFlow<RuntimeSession?>(null)
    val session: StateFlow<RuntimeSession?> = _session.asStateFlow()

    fun begin(
        operation: RuntimeOperation,
        targetPackage: String,
        targetAndroidUserId: Int,
        remoteTarget: Boolean,
        preference: RuntimeBackendPreference,
        accessibility: BackendCapability,
        shizuku: BackendCapability
    ): RuntimeSession {
        val decision = SmartBackendPolicy.resolve(
            SmartBackendInput(preference, remoteTarget, accessibility, shizuku)
        )
        val initialState = if (decision.blocked) RuntimeSessionState.ERROR else RuntimeSessionState.RUNNING
        val initialHealth = if (decision.blocked) {
            RuntimeHealthSnapshot(RuntimeHealthState.BLOCKED, decision.reasonCode, 0)
        } else {
            RuntimeHealthSnapshot(RuntimeHealthState.HEALTHY, decision.reasonCode, 100)
        }
        val created = RuntimeSession(
            sessionId = ids.getAndIncrement(),
            operation = operation,
            targetPackage = targetPackage,
            targetAndroidUserId = targetAndroidUserId,
            remoteTarget = remoteTarget,
            requestedBackend = preference,
            effectiveBackend = decision.effectiveBackend,
            state = initialState,
            health = initialHealth,
            inputAuthorized = !decision.blocked,
            generation = 1L
        )
        _session.value = created
        commandBus.resetForOperation(operation)
        return created
    }

    fun checkpoint(itemId: String, checkpointRef: String?, signature: Int?): RuntimeSession {
        return mutate { current ->
            current.copy(
                checkpoint = RuntimeCheckpoint(itemId, checkpointRef, signature, nowMs()),
                lastSnapshotSignature = signature,
                generation = current.generation + 1
            )
        }
    }

    fun reportTelemetry(telemetry: RuntimeTelemetry): RuntimeSession {
        return mutate { current ->
            val health = RuntimeHealthMonitor.evaluate(telemetry)
            val pacing = governor.choose(
                PerformanceTelemetry(
                    health = health.state,
                    snapshotLatencyMs = telemetry.snapshotLatencyMs,
                    staleUiCount = telemetry.staleUiCount,
                    failureCount = telemetry.consecutiveBackendFailures
                )
            )
            val nextState = when {
                current.terminal -> current.state
                health.state == RuntimeHealthState.BLOCKED -> RuntimeSessionState.ERROR
                health.state == RuntimeHealthState.RECOVERING -> RuntimeSessionState.RECOVERING
                else -> current.state
            }
            current.copy(
                health = health,
                pacing = pacing,
                state = nextState,
                inputAuthorized = current.inputAuthorized && health.state != RuntimeHealthState.BLOCKED,
                generation = current.generation + 1
            )
        }
    }

    fun recordFailure(kind: RuntimeFailureKind, reason: String = kind.name): RuntimeSession {
        return mutate { current ->
            val count = (current.failureCounts[kind] ?: 0) + 1
            current.copy(
                failureCounts = current.failureCounts + (kind to count),
                lastRecoveryReason = reason,
                generation = current.generation + 1
            )
        }
    }

    fun markTargetLost(reason: String = "TARGET_LOST"): RuntimeSession {
        return mutate { current ->
            current.copy(
                state = RuntimeSessionState.PAUSED_OUTSIDE_TARGET,
                health = RuntimeHealthSnapshot(RuntimeHealthState.RECOVERING, reason, 35),
                inputAuthorized = false,
                lastRecoveryReason = reason,
                generation = current.generation + 1
            )
        }
    }

    fun requestHandover(
        failure: RuntimeFailureKind,
        accessibility: BackendCapability,
        shizuku: BackendCapability
    ): RuntimeHandoverPlan {
        val current = requireSession()
        if (current.requestedBackend != RuntimeBackendPreference.AUTO) {
            return RuntimeHandoverPlan(
                allowed = false,
                fromBackend = current.effectiveBackend,
                toBackend = RuntimeBackendKind.NONE,
                reasonCode = "MANUAL_BACKEND_NO_CROSSOVER"
            )
        }

        val alternateDecision = when (current.effectiveBackend) {
            RuntimeBackendKind.SHIZUKU -> {
                if (accessibility.healthy) RuntimeBackendKind.ACCESSIBILITY else RuntimeBackendKind.NONE
            }
            RuntimeBackendKind.ACCESSIBILITY -> {
                if (shizuku.healthy) RuntimeBackendKind.SHIZUKU else RuntimeBackendKind.NONE
            }
            RuntimeBackendKind.NONE -> SmartBackendPolicy.resolve(
                SmartBackendInput(current.requestedBackend, current.remoteTarget, accessibility, shizuku)
            ).effectiveBackend
        }

        val safeRemoteFallback = !current.remoteTarget ||
            alternateDecision != RuntimeBackendKind.ACCESSIBILITY || accessibility.profileSafe
        val allowed = alternateDecision != RuntimeBackendKind.NONE && safeRemoteFallback
        return RuntimeHandoverPlan(
            allowed = allowed,
            fromBackend = current.effectiveBackend,
            toBackend = if (allowed) alternateDecision else RuntimeBackendKind.NONE,
            reasonCode = if (allowed) "AUTO_HANDOVER_${failure.name}" else "AUTO_NO_SAFE_ALTERNATE_${failure.name}"
        )
    }

    fun applyHandover(plan: RuntimeHandoverPlan): RuntimeSession {
        require(plan.allowed) { "Handover plan is blocked" }
        return mutate { current ->
            current.copy(
                effectiveBackend = plan.toBackend,
                state = RuntimeSessionState.RECOVERING,
                inputAuthorized = false,
                lastRecoveryReason = plan.reasonCode,
                generation = current.generation + 1
            )
        }
    }

    fun markFreshTargetVerified(signature: Int? = null): RuntimeSession {
        return mutate { current ->
            current.copy(
                state = RuntimeSessionState.RUNNING,
                health = RuntimeHealthSnapshot(RuntimeHealthState.HEALTHY, "TARGET_VERIFIED", 100),
                inputAuthorized = true,
                lastSnapshotSignature = signature ?: current.lastSnapshotSignature,
                generation = current.generation + 1
            )
        }
    }

    fun applyCommand(command: RuntimeCommand): RuntimeSession {
        val current = requireSession()
        if (command.operation != current.operation) return current
        return when (command.type) {
            RuntimeCommandType.PAUSE -> mutate { it.copy(state = RuntimeSessionState.PAUSED, inputAuthorized = false, generation = it.generation + 1) }
            RuntimeCommandType.RESUME -> mutate {
                val outside = it.state == RuntimeSessionState.PAUSED_OUTSIDE_TARGET
                it.copy(
                    state = if (outside) RuntimeSessionState.RESOLVING_TARGET else RuntimeSessionState.RUNNING,
                    inputAuthorized = !outside,
                    generation = it.generation + 1
                )
            }
            RuntimeCommandType.RETURN_TO_TARGET -> mutate {
                it.copy(state = RuntimeSessionState.RESOLVING_TARGET, inputAuthorized = false, generation = it.generation + 1)
            }
            RuntimeCommandType.STOP -> mutate {
                it.copy(state = RuntimeSessionState.STOPPED, inputAuthorized = false, generation = it.generation + 1)
            }
        }
    }

    fun markCompleted(): RuntimeSession = mutate {
        it.copy(state = RuntimeSessionState.COMPLETED, inputAuthorized = false, generation = it.generation + 1)
    }

    fun markError(reason: String): RuntimeSession = mutate {
        it.copy(
            state = RuntimeSessionState.ERROR,
            health = RuntimeHealthSnapshot(RuntimeHealthState.BLOCKED, reason, 0),
            inputAuthorized = false,
            lastRecoveryReason = reason,
            generation = it.generation + 1
        )
    }

    private fun requireSession(): RuntimeSession = requireNotNull(_session.value) { "No active runtime session" }

    private inline fun mutate(block: (RuntimeSession) -> RuntimeSession): RuntimeSession {
        val updated = block(requireSession())
        _session.value = updated
        return updated
    }
}
