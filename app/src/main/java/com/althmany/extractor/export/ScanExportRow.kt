package com.althmany.extractor.export

import com.althmany.extractor.data.ScanRecord

/**
 * Canonical export projection so CSV / TXT / JSON / XLSX expose the same scan facts.
 */
data class ScanExportRow(
    val groupName: String,
    val memberCount: Int?,
    val memberCountText: String,
    val visibleMemberIndicator: String,
    val status: String,
    val statusAr: String,
    val inviteKind: String,
    val inviteKindAr: String,
    val confidence: Int,
    val url: String,
    val inviteCode: String,
    val sourceGroup: String,
    val signalCode: String,
    val detail: String,
    val attempts: Int,
    val durationMs: Long?,
    val targetPackage: String,
    val scannedAt: Long?
) {
    companion object {
        fun from(record: ScanRecord): ScanExportRow = ScanExportRow(
            groupName = record.groupName.orEmpty(),
            memberCount = MemberCountParser.parse(record.memberCountText),
            memberCountText = record.memberCountText.orEmpty(),
            visibleMemberIndicator = record.visibleMemberIndicator.orEmpty(),
            status = record.status.name,
            statusAr = record.status.labelAr,
            inviteKind = record.inviteKind.name,
            inviteKindAr = record.inviteKind.labelAr,
            confidence = record.confidence,
            url = record.normalizedUrl,
            inviteCode = record.inviteCode,
            sourceGroup = record.sourceGroup.orEmpty(),
            signalCode = record.signalCode.orEmpty(),
            detail = record.detail.orEmpty(),
            attempts = record.attempts,
            durationMs = record.durationMs,
            targetPackage = record.targetPackage.orEmpty(),
            scannedAt = record.scannedAt
        )
    }
}
