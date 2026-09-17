package com.althmany.extractor.engine

import com.althmany.extractor.data.ScanStats

enum class ScanEngineStatus { IDLE, PREPARING, WAITING_NETWORK, OPENING, CLASSIFYING, RECOVERING, RETRYING, PAUSED, COMPLETED, STOPPED, ERROR }

enum class ScanSpeedProfile(val labelAr: String, val previewTimeoutMs: Long, val eventWaitMs: Long, val settleDelayMs: Long) {
    HYPER("فائق", 1_600L, 30L, 6L),
    ADAPTIVE("ذكي", 2_800L, 55L, 12L),
    SAFE("دقيق", 5_000L, 100L, 24L)
}

enum class ScanActionMode(val labelAr: String) {
    SCAN_ONLY("فحص فقط"),
    /** Legacy UI compatibility only. ScanController always coerces to SCAN_ONLY. */
    JOIN_ONLY("انضمام فقط"),
    /** Legacy UI compatibility only. ScanController always coerces to SCAN_ONLY. */
    SCAN_AND_JOIN("فحص + انضمام")
}

enum class ScanScope(val labelAr: String) {
    PENDING_ONLY("الجديد فقط"),
    UNCERTAIN_ONLY("غير المؤكد والأخطاء فقط"),
    RECHECK_ALL("إعادة فحص الكل")
}

data class ScanUiState(
    val status: ScanEngineStatus = ScanEngineStatus.IDLE,
    val serviceConnected: Boolean = false,
    val running: Boolean = false,
    val paused: Boolean = false,
    val currentUrl: String? = null,
    val currentIndex: Int = 0,
    val total: Int = 0,
    val currentAttempt: Int = 0,
    val currentConfidence: Int = 0,
    val speed: ScanSpeedProfile = ScanSpeedProfile.ADAPTIVE,
    val scope: ScanScope = ScanScope.PENDING_ONLY,
    val maxAttempts: Int = 1,
    val actionMode: ScanActionMode = ScanActionMode.SCAN_ONLY,
    val requestToJoinEnabled: Boolean = false,
    val message: String = "جاهز للفحص",
    val stats: ScanStats = ScanStats()
)
