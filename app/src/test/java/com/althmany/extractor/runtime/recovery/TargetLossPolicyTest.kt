package com.althmany.extractor.runtime.recovery

fun main() {
    val p = TargetLossPolicy(graceMs = 450L, confirmationsRequired = 2)
    var s = TargetLossState()
    s = p.observe(s, targetVisible = true, nowMs = 1000L)
    check(s.mode == TargetPresenceMode.VISIBLE)
    s = p.observe(s, targetVisible = false, nowMs = 1100L)
    check(s.mode == TargetPresenceMode.SUSPECTED_LOST)
    s = p.observe(s, targetVisible = false, nowMs = 1600L)
    check(s.mode == TargetPresenceMode.LOST)
    check(s.pauseRequired)
    println("TARGET_LOSS_POLICY_PASS")
}
