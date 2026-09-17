package com.althmany.extractor.runtime.recovery

import android.content.Context
import com.althmany.extractor.engine.RuntimeOperation

internal class RuntimeRecoveryStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    var pausedOutsideTarget: Boolean
        get() = prefs.getBoolean(KEY_PAUSED_OUTSIDE, false)
        set(value) { prefs.edit().putBoolean(KEY_PAUSED_OUTSIDE, value).apply() }

    var resumePending: Boolean
        get() = prefs.getBoolean(KEY_RESUME_PENDING, false)
        set(value) { prefs.edit().putBoolean(KEY_RESUME_PENDING, value).apply() }

    var operation: RuntimeOperation?
        get() = prefs.getString(KEY_OPERATION, null)?.let { runCatching { RuntimeOperation.valueOf(it) }.getOrNull() }
        set(value) {
            prefs.edit().apply {
                if (value == null) remove(KEY_OPERATION) else putString(KEY_OPERATION, value.name)
            }.apply()
        }

    var expectedPackage: String?
        get() = prefs.getString(KEY_EXPECTED_PACKAGE, null)
        set(value) {
            prefs.edit().apply {
                if (value.isNullOrBlank()) remove(KEY_EXPECTED_PACKAGE) else putString(KEY_EXPECTED_PACKAGE, value)
            }.apply()
        }

    var previousSignature: Int?
        get() = if (prefs.contains(KEY_PREVIOUS_SIGNATURE)) prefs.getInt(KEY_PREVIOUS_SIGNATURE, 0) else null
        set(value) {
            prefs.edit().apply {
                if (value == null) remove(KEY_PREVIOUS_SIGNATURE) else putInt(KEY_PREVIOUS_SIGNATURE, value)
            }.apply()
        }

    var remoteTarget: Boolean
        get() = prefs.getBoolean(KEY_REMOTE_TARGET, false)
        set(value) { prefs.edit().putBoolean(KEY_REMOTE_TARGET, value).apply() }

    var processRecoveryRequired: Boolean
        get() = prefs.getBoolean(KEY_PROCESS_RECOVERY_REQUIRED, false)
        set(value) { prefs.edit().putBoolean(KEY_PROCESS_RECOVERY_REQUIRED, value).apply() }

    fun clear() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREFS = "althmany_runtime_recovery_v4"
        private const val KEY_PAUSED_OUTSIDE = "paused_outside_target"
        private const val KEY_RESUME_PENDING = "resume_pending"
        private const val KEY_OPERATION = "operation"
        private const val KEY_EXPECTED_PACKAGE = "expected_package"
        private const val KEY_PREVIOUS_SIGNATURE = "previous_signature"
        private const val KEY_REMOTE_TARGET = "remote_target"
        private const val KEY_PROCESS_RECOVERY_REQUIRED = "process_recovery_required"
    }
}
