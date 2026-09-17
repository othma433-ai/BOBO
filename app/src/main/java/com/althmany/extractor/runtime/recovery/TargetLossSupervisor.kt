package com.althmany.extractor.runtime.recovery

/** Pure state machine used by Android adapters and unit tests. */
class TargetLossSupervisor(
    private val lossPolicy: TargetLossPolicy = TargetLossPolicy(),
    private val resumeGate: SmartResumeGate = SmartResumeGate()
) {
    private var lossState = TargetLossState()
    private var resumeState = ResumeVerificationState()
    var pausedOutsideTarget: Boolean = false
        private set
    var resumePending: Boolean = false
        private set

    fun observeTarget(targetVisible: Boolean, nowMs: Long): Boolean {
        lossState = lossPolicy.observe(lossState, targetVisible, nowMs)
        if (lossState.pauseRequired) pausedOutsideTarget = true
        return pausedOutsideTarget
    }

    fun requestResume(
        previousSignature: Int?,
        requireSignatureChange: Boolean,
        baselineGeneration: Long? = null
    ) {
        resumePending = true
        resumeState = ResumeVerificationState(
            previousSignature = previousSignature,
            requireSignatureChange = requireSignatureChange,
            lastEventGeneration = baselineGeneration
        )
    }

    fun observeResumeFrame(targetVisible: Boolean, signature: Int?, eventGeneration: Long?): Boolean {
        if (!resumePending) return false
        resumeState = resumeGate.observe(resumeState, targetVisible, signature, eventGeneration)
        if (!resumeState.ready) return false
        resumePending = false
        pausedOutsideTarget = false
        lossState = TargetLossState(mode = TargetPresenceMode.VISIBLE)
        return true
    }

    fun reset() {
        lossState = TargetLossState()
        resumeState = ResumeVerificationState()
        pausedOutsideTarget = false
        resumePending = false
    }
}
