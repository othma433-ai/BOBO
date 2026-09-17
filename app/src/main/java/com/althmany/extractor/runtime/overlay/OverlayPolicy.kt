package com.althmany.extractor.runtime.overlay

enum class OverlayMode { HIDDEN, FLOATING, NOTIFICATION_ONLY }
enum class OverlaySessionState { RUNNING, PAUSED, PAUSED_OUTSIDE_TARGET, RECOVERING, TERMINAL }

data class OverlayUiModel(
    val mode: OverlayMode,
    val showPause: Boolean,
    val showResume: Boolean,
    val showReturnToTarget: Boolean,
    val showStop: Boolean
)

object OverlayPolicy {
    fun resolve(active: Boolean, overlayPermission: Boolean, state: OverlaySessionState): OverlayUiModel {
        if (!active || state == OverlaySessionState.TERMINAL) {
            return OverlayUiModel(OverlayMode.HIDDEN, false, false, false, false)
        }
        val mode = if (overlayPermission) OverlayMode.FLOATING else OverlayMode.NOTIFICATION_ONLY
        return OverlayUiModel(
            mode = mode,
            showPause = state == OverlaySessionState.RUNNING,
            showResume = state in setOf(OverlaySessionState.PAUSED, OverlaySessionState.PAUSED_OUTSIDE_TARGET, OverlaySessionState.RECOVERING),
            showReturnToTarget = state == OverlaySessionState.PAUSED_OUTSIDE_TARGET,
            showStop = true
        )
    }
}
