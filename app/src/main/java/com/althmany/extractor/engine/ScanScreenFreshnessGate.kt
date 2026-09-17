package com.althmany.extractor.engine

import java.security.MessageDigest
import java.util.Locale

/**
 * Prevents Scan from classifying the previous invite card after the next URL is launched.
 *
 * A candidate is fresh when either:
 *  - its normalized UI signature differs from the pre-launch baseline, or
 *  - Accessibility/Shizuku reported a post-open UI event that confirms navigation.
 *
 * This gate is read-only and backend-neutral.
 */
object ScanScreenFreshnessGate {

    fun signature(texts: Collection<String>): String {
        val canonical = texts.asSequence()
            .map { it.trim().replace(Regex("""\s+"""), " ").lowercase(Locale.ROOT) }
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()
            .joinToString("\u001F")

        if (canonical.isBlank()) return "EMPTY"

        val digest = MessageDigest.getInstance("SHA-256")
            .digest(canonical.toByteArray(Charsets.UTF_8))
        return digest.take(12).joinToString("") { "%02x".format(it) }
    }

    fun isFresh(
        baselineSignature: String?,
        candidateSignature: String,
        observedUiEventAfterOpen: Boolean
    ): Boolean {
        if (candidateSignature == "EMPTY") return false
        if (baselineSignature.isNullOrBlank() || baselineSignature == "EMPTY") return true
        return candidateSignature != baselineSignature || observedUiEventAfterOpen
    }
}
