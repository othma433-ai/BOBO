package com.althmany.extractor.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ScanPersistenceContractTest {

    @Test
    fun schemaVersionIsBumpedForVisibleMemberIndicator() {
        assertEquals(12, ScanPersistenceContract.schemaVersion)
    }

    @Test
    fun projectionContainsVisibleMemberIndicatorBeforeInviteKind() {
        val columns = ScanPersistenceContract.scanProjection
        val visible = columns.indexOf("visible_member_indicator")
        val kind = columns.indexOf("invite_kind")

        assertTrue(visible >= 0)
        assertTrue(kind > visible)
    }

    @Test
    fun projectionHasExpectedColumnCount() {
        assertEquals(18, ScanPersistenceContract.scanProjection.size)
    }
}
