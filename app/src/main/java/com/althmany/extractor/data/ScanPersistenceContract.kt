package com.althmany.extractor.data

/**
 * Canonical persistence projection for scan_items after the 3.5.0 scan upgrade.
 * Keep ExtractorDatabase scanSelect/cursor mapping aligned with this order.
 */
object ScanPersistenceContract {
    const val schemaVersion: Int = 12

    val scanProjection: List<String> = listOf(
        "id",
        "url",
        "normalized_url",
        "invite_code",
        "source_group",
        "status",
        "group_name",
        "detail",
        "attempts",
        "added_at",
        "scanned_at",
        "confidence",
        "member_count_text",
        "visible_member_indicator",
        "invite_kind",
        "signal_code",
        "duration_ms",
        "target_package"
    )
}
