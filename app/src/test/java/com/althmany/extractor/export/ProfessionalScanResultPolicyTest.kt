package com.althmany.extractor.export

import com.althmany.extractor.data.InviteKind
import com.althmany.extractor.data.ScanRecord
import com.althmany.extractor.data.ScanStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProfessionalScanResultPolicyTest {

    private fun record(
        id: Long,
        status: ScanStatus,
        members: String? = null,
        visibleIndicator: String? = null,
        confidence: Int = 90,
        durationMs: Long? = 100L
    ) = ScanRecord(
        id = id,
        url = "https://chat.whatsapp.com/$id",
        normalizedUrl = "https://chat.whatsapp.com/$id",
        inviteCode = id.toString(),
        sourceGroup = null,
        status = status,
        groupName = "Group $id",
        detail = null,
        attempts = 1,
        addedAt = 1L,
        scannedAt = 2L,
        confidence = confidence,
        memberCountText = members,
        visibleMemberIndicator = visibleIndicator,
        inviteKind = InviteKind.GROUP,
        signalCode = status.name,
        durationMs = durationMs,
        targetPackage = "com.whatsapp"
    )

    @Test
    fun statusOrder_keepsActionableBeforeFailuresAndUnknown() {
        val order = ScanResultOrganizer.statusOrder
        assertTrue(order.indexOf(ScanStatus.DIRECT) < order.indexOf(ScanStatus.EXPIRED))
        assertTrue(order.indexOf(ScanStatus.APPROVAL) < order.indexOf(ScanStatus.INVALID))
        assertTrue(order.indexOf(ScanStatus.INVALID) < order.indexOf(ScanStatus.UNKNOWN))
    }

    @Test
    fun expired_hasDedicatedSheet() {
        assertEquals("Expired", ScanResultOrganizer.sheetName(ScanStatus.EXPIRED))
    }

    @Test
    fun explicitMembersSortDescendingWithinResults() {
        val items = listOf(
            record(1, ScanStatus.DIRECT, "12 members"),
            record(2, ScanStatus.DIRECT, "245 members"),
            record(3, ScanStatus.DIRECT, null)
        )

        val sorted = ScanResultOrganizer.sorted(items)

        assertEquals(245, MemberCountParser.parse(sorted[0].memberCountText))
        assertEquals(12, MemberCountParser.parse(sorted[1].memberCountText))
        assertEquals(null, MemberCountParser.parse(sorted[2].memberCountText))
    }

    @Test
    fun plusBadgeIsExportedSeparatelyFromExplicitMemberCount() {
        val row = ScanExportRow.from(record(
            id = 1,
            status = ScanStatus.DIRECT,
            members = null,
            visibleIndicator = "+12"
        ))

        assertEquals(null, row.memberCount)
        assertEquals("", row.memberCountText)
        assertEquals("+12", row.visibleMemberIndicator)
    }

    @Test
    fun exportRowPreservesAuditFields() {
        val row = ScanExportRow.from(record(
            id = 7,
            status = ScanStatus.APPROVAL,
            members = "1,024 members",
            confidence = 97,
            durationMs = 143L
        ))

        assertEquals(1024, row.memberCount)
        assertEquals(97, row.confidence)
        assertEquals(143L, row.durationMs)
        assertEquals("APPROVAL", row.status)
        assertEquals("GROUP", row.inviteKind)
        assertEquals("com.whatsapp", row.targetPackage)
    }
}
