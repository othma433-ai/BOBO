package com.althmany.groupmanager.domain

/**
 * Single-flight runtime contract shared by Join / Scan / Publish.
 * One task is attempted once; continuity moves to the next task instead of repeating the same one.
 */
object RuntimeUnifiedPolicy {
    const val actionAttempts = 1
    const val scanAttempts = 1
    const val publishAttempts = 1
    const val autoAdvance = true
    const val pauseOnControlledExit = false
    const val autoResumeAfterControlledExit = true
    const val terminalHandoffMs = 24L
}
