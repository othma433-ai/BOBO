package com.althmany.extractor.engine

import com.althmany.extractor.data.ExtractionMode
import com.althmany.extractor.data.GroupSelectionPreset

enum class SmartExtractionDirection { TOWARD_OLDER, TOWARD_NEWER }

object SmartExtractionPolicy {
    fun directionFor(mode: ExtractionMode): SmartExtractionDirection = when (mode) {
        ExtractionMode.NEW_ONLY -> SmartExtractionDirection.TOWARD_NEWER
        else -> SmartExtractionDirection.TOWARD_OLDER
    }

    fun modeForPreset(preset: GroupSelectionPreset): ExtractionMode? = when (preset) {
        GroupSelectionPreset.ALL -> ExtractionMode.ALL_CHATS
        GroupSelectionPreset.UNREAD -> ExtractionMode.NEW_ONLY
        else -> null
    }

    fun fastEndDecisionBudgetMs(verificationWindowMs: Long, microProbeWindowMs: Long): Long =
        (verificationWindowMs.coerceIn(80L, 700L) + microProbeWindowMs.coerceIn(50L, 250L))
            .coerceAtMost(950L)
}
