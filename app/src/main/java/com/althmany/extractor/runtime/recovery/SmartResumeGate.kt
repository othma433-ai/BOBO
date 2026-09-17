package com.althmany.extractor.runtime.recovery

data class ResumeVerificationState(
    val previousSignature: Int? = null,
    val requireSignatureChange: Boolean = false,
    val visibleConfirmations: Int = 0,
    val lastEventGeneration: Long? = null,
    val freshnessProved: Boolean = false,
    val ready: Boolean = false
)

/**
 * Resume is authorized only after exact-target visibility plus fresh UI evidence.
 * Accessibility may prove freshness with a newer event generation; remote/Shizuku paths can
 * require a changed snapshot signature.
 */
class SmartResumeGate(private val confirmationsRequired: Int = 2) {
    init { require(confirmationsRequired >= 1) }

    fun observe(
        previous: ResumeVerificationState,
        targetVisible: Boolean,
        signature: Int?,
        eventGeneration: Long?
    ): ResumeVerificationState {
        if (!targetVisible) {
            return previous.copy(visibleConfirmations = 0, ready = false)
        }

        val confirmations = previous.visibleConfirmations + 1
        val signatureChanged = signature != null && previous.previousSignature != null && signature != previous.previousSignature
        val eventAdvanced = eventGeneration != null && previous.lastEventGeneration != null && eventGeneration > previous.lastEventGeneration
        val firstFreshFrame = previous.lastEventGeneration == null && eventGeneration != null && !previous.requireSignatureChange
        val freshness = previous.freshnessProved || signatureChanged || eventAdvanced || firstFreshFrame
        // Remote/Shizuku recovery accepts either changed UI content OR a newer verified
        // snapshot generation. Requiring content change alone can deadlock when WhatsApp returns to
        // the exact same screen after relaunch.
        val freshnessAccepted = if (previous.requireSignatureChange) {
            previous.freshnessProved || signatureChanged || eventAdvanced
        } else freshness

        return previous.copy(
            previousSignature = signature ?: previous.previousSignature,
            visibleConfirmations = confirmations,
            lastEventGeneration = eventGeneration ?: previous.lastEventGeneration,
            freshnessProved = freshnessAccepted,
            ready = confirmations >= confirmationsRequired && freshnessAccepted
        )
    }
}
