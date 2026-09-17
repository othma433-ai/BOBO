package com.althmany.extractor.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class InviteScanMetadataPolicyTest {
    @Test
    fun plusBadge_isVisibleIndicator_notExplicitMemberCount() {
        val metadata = InviteMemberMetadataParser.parse(listOf(
            "مجموعة الدراسة",
            "+12"
        ))

        assertNull(metadata.explicitMemberCountText)
        assertEquals("+12", metadata.visibleMemberIndicator)
    }

    @Test
    fun explicitArabicMemberCount_isStoredAsMemberCount() {
        val metadata = InviteMemberMetadataParser.parse(listOf(
            "مجموعة الدراسة",
            "245 أعضاء"
        ))

        assertEquals("245 أعضاء", metadata.explicitMemberCountText)
        assertNull(metadata.visibleMemberIndicator)
    }

    @Test
    fun explicitEnglishMemberCount_isStoredAsMemberCount() {
        val metadata = InviteMemberMetadataParser.parse(listOf(
            "Study Group",
            "1,024 members"
        ))

        assertEquals("1,024 members", metadata.explicitMemberCountText)
    }
}
