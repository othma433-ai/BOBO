package com.althmany.extractor.engine

import com.althmany.extractor.data.InviteKind
import com.althmany.extractor.data.ScanStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PreciseInviteScanClassifierTest {

    @Test
    fun expiredInvite_isSeparatedFromGenericInvalid() {
        val result = InviteScanClassifier.classify(
            listOf("This invite link has expired")
        )

        assertEquals(ScanStatus.EXPIRED, result.status)
        assertEquals("INVITE_EXPIRED", result.signalCode)
        assertTrue(result.definitive)
    }

    @Test
    fun revokedInvite_remainsInvalidNotExpired() {
        val result = InviteScanClassifier.classify(
            listOf("This invite link has been revoked")
        )

        assertEquals(ScanStatus.INVALID, result.status)
        assertEquals("INVITE_INVALID", result.signalCode)
    }

    @Test
    fun requestToJoin_group_isApproval() {
        val result = InviteScanClassifier.classify(
            listOf("Study Group", "Request to join group", "245 members")
        )

        assertEquals(ScanStatus.APPROVAL, result.status)
        assertEquals(InviteKind.GROUP, result.inviteKind)
        assertEquals("245 members", result.memberCountText)
    }

    @Test
    fun directCommunity_keepsCommunityKindAndDirectStatus() {
        val result = InviteScanClassifier.classify(
            listOf("University Community", "Join community")
        )

        assertEquals(ScanStatus.DIRECT, result.status)
        assertEquals(InviteKind.COMMUNITY, result.inviteKind)
    }

    @Test
    fun pendingRequest_hasPriorityOverApprovalButtonText() {
        val result = InviteScanClassifier.classify(
            listOf("Request to join", "Cancel request", "Your request is pending")
        )

        assertEquals(ScanStatus.REQUEST_PENDING, result.status)
        assertEquals("REQUEST_PENDING", result.signalCode)
    }

    @Test
    fun fullGroup_isTerminalBeforeGenericJoinText() {
        val result = InviteScanClassifier.classify(
            listOf("This group is full", "Join group")
        )

        assertEquals(ScanStatus.FULL, result.status)
        assertEquals("GROUP_FULL", result.signalCode)
    }

    @Test
    fun plusAvatarBadge_doesNotBecomeMemberCount() {
        val result = InviteScanClassifier.classify(
            listOf("Study Group", "+12", "Join group")
        )

        assertEquals("+12", result.visibleMemberIndicator)
        assertEquals(null, result.memberCountText)
    }
}
