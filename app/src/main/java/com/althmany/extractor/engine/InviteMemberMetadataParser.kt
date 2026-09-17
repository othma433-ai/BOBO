package com.althmany.extractor.engine

data class InviteMemberMetadata(
    val explicitMemberCountText: String?,
    val visibleMemberIndicator: String?
)

/**
 * Keeps the WhatsApp "+NN" avatar badge separate from an explicit total member count.
 * "+12" is never promoted to memberCount because WhatsApp may use it as an overflow indicator.
 */
object InviteMemberMetadataParser {
    private val explicitCountRegexes = listOf(
        Regex("""(?i)\b[0-9٠-٩۰-۹][0-9٠-٩۰-۹,.٬، ]{0,10}\s*(?:مشارك(?:ًا|ا)?|مشاركون|عضو|أعضاء)\b"""),
        Regex("""(?i)\b[0-9][0-9,. ]{0,10}\s*(?:participants?|members?)\b""")
    )

    private val visibleIndicatorRegex = Regex("""^\+\s*[0-9٠-٩۰-۹]{1,6}$""")

    fun parse(texts: Collection<String>): InviteMemberMetadata {
        val cleaned = texts.asSequence()
            .map { it.trim().replace(Regex("""\s+"""), " ") }
            .filter { it.isNotBlank() }
            .distinct()
            .toList()

        val explicit = cleaned.firstNotNullOfOrNull { text ->
            explicitCountRegexes.firstNotNullOfOrNull { regex ->
                regex.find(text)?.value
            }
        }

        val visible = cleaned.firstOrNull { visibleIndicatorRegex.matches(it) }

        return InviteMemberMetadata(
            explicitMemberCountText = explicit,
            visibleMemberIndicator = visible
        )
    }
}
