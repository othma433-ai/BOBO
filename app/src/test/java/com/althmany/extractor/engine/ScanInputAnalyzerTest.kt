package com.althmany.extractor.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ScanInputAnalyzerTest {
    @Test
    fun mixedText_extractsUniqueWhatsAppInvites() {
        val text = """
            note https://chat.whatsapp.com/AbCdEf123
            duplicate: https://chat.whatsapp.com/AbCdEf123
            https://chat.whatsapp.com/ZyX987AB
        """.trimIndent()

        val result = ScanInputAnalyzer.analyze(text)

        assertEquals(2, result.uniqueValid)
        assertTrue(result.preview.any { it.contains("AbCdEf123") })
        assertTrue(result.preview.any { it.contains("ZyX987AB") })
    }

    @Test
    fun emptyInput_isZero() {
        val result = ScanInputAnalyzer.analyze("   ")

        assertEquals(0, result.detected)
        assertEquals(0, result.uniqueValid)
        assertEquals(0, result.duplicates)
        assertEquals(0, result.invalidCandidates)
        assertTrue(result.preview.isEmpty())
    }

    @Test
    fun preview_isBounded() {
        val text = (1..80).joinToString("\n") {
            "https://chat.whatsapp.com/TestInvite${it}ABC"
        }

        val result = ScanInputAnalyzer.analyze(text, previewLimit = 50)

        assertEquals(50, result.preview.size)
    }
}
