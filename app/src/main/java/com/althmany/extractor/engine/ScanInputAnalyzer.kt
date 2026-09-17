package com.althmany.extractor.engine

/**
 * Pure scan-input analyzer used by the UI before items are persisted.
 * It never opens WhatsApp and never changes the queue.
 */
data class ScanInputAnalysis(
    val detected: Int,
    val uniqueValid: Int,
    val duplicates: Int,
    val invalidCandidates: Int,
    val preview: List<String>
)

object ScanInputAnalyzer {
    private val candidateRegex = Regex(
        """(?i)(?:https?://)?(?:chat\.whatsapp\.com|whatsapp\.com/channel)/[A-Za-z0-9_-]+"""
    )

    fun analyze(raw: String, previewLimit: Int = 50): ScanInputAnalysis {
        if (raw.isBlank()) {
            return ScanInputAnalysis(
                detected = 0,
                uniqueValid = 0,
                duplicates = 0,
                invalidCandidates = 0,
                preview = emptyList()
            )
        }

        val candidates = candidateRegex.findAll(raw)
            .map { it.value.trim().trimEnd('.', ',', ';', ')', ']', '}', '"', '\'') }
            .filter(String::isNotBlank)
            .toList()

        val parsed = InviteLinkParser.extract(raw)
            .map { it.normalizedUrl }

        val unique = LinkedHashSet(parsed)
        val duplicates = (parsed.size - unique.size).coerceAtLeast(0)

        // Candidate-like WhatsApp invite strings that the canonical parser rejected.
        val parsedLower = unique.mapTo(hashSetOf()) { it.lowercase() }
        val invalid = candidates.count { candidate ->
            val normalizedCandidate = if (candidate.startsWith("http", ignoreCase = true)) {
                candidate
            } else {
                "https://$candidate"
            }
            normalizedCandidate.lowercase() !in parsedLower
        }

        return ScanInputAnalysis(
            detected = candidates.size,
            uniqueValid = unique.size,
            duplicates = duplicates,
            invalidCandidates = invalid,
            preview = unique.take(previewLimit)
        )
    }
}
