package com.althmany.extractor.accessibility

import android.graphics.Rect
import android.view.accessibility.AccessibilityNodeInfo

/** Shared Accessibility UI driver used by extraction, scan and publish regardless of which
 * AL-thmany accessibility service Android has connected. */
interface AccessibilityUiDriver {
    fun currentRoot(): AccessibilityNodeInfo?
    fun performBack(): Boolean
    fun tapBounds(bounds: Rect?, durationMs: Long = 72L): Boolean
    fun swipeTowardOlderMessages(durationMs: Long): Boolean
    fun swipeTowardNewerMessages(durationMs: Long): Boolean
    fun swipeChatListForward(durationMs: Long): Boolean
    fun swipeChatListBackward(durationMs: Long): Boolean
}
