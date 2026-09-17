package com.althmany.extractor.runtime.recovery

fun main() {
    val supervisor = TargetLossSupervisor(
        TargetLossPolicy(graceMs = 100L, confirmationsRequired = 2),
        SmartResumeGate(confirmationsRequired = 2)
    )
    check(!supervisor.observeTarget(true, 0L))
    check(!supervisor.observeTarget(false, 10L))
    check(supervisor.observeTarget(false, 150L))
    check(supervisor.pausedOutsideTarget)

    supervisor.requestResume(previousSignature = 5, requireSignatureChange = true)
    check(!supervisor.observeResumeFrame(true, 5, null))
    check(supervisor.observeResumeFrame(true, 6, null))
    check(!supervisor.pausedOutsideTarget)
    check(!supervisor.resumePending)
    println("TARGET_LOSS_SUPERVISOR_PASS")
}
