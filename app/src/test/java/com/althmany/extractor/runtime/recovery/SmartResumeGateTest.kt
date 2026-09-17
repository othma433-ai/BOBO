package com.althmany.extractor.runtime.recovery

fun main() {
    val gate = SmartResumeGate(confirmationsRequired = 2)
    var state = ResumeVerificationState(previousSignature = 77)
    state = gate.observe(state, targetVisible = true, signature = 77, eventGeneration = 10)
    check(!state.ready)
    state = gate.observe(state, targetVisible = true, signature = 88, eventGeneration = 11)
    check(state.ready)

    val remoteGate = SmartResumeGate(confirmationsRequired = 2)
    var remote = ResumeVerificationState(previousSignature = 50, requireSignatureChange = true)
    remote = remoteGate.observe(remote, targetVisible = true, signature = 50, eventGeneration = null)
    check(!remote.ready)
    remote = remoteGate.observe(remote, targetVisible = true, signature = 51, eventGeneration = null)
    check(remote.ready)
    println("SMART_RESUME_GATE_PASS")
}
