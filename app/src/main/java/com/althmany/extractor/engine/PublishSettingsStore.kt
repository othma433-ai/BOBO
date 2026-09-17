package com.althmany.extractor.engine

import android.content.Context
import com.althmany.extractor.profile.UnifiedRuntimeRepository
import com.althmany.extractor.data.PublishContentMode

class PublishSettingsStore(private val context: Context) {
    private val prefs = context.getSharedPreferences("publish_settings", Context.MODE_PRIVATE)

    fun speed(): PublishSpeedProfile = when (UnifiedRuntimeRepository.speed(context)) {
        UnifiedRuntimeSettingsStore.Speed.HYPER -> PublishSpeedProfile.INSTANT
        UnifiedRuntimeSettingsStore.Speed.ADAPTIVE -> PublishSpeedProfile.ADAPTIVE
        UnifiedRuntimeSettingsStore.Speed.SAFE -> PublishSpeedProfile.SAFE
    }

    fun setSpeed(value: PublishSpeedProfile) {
        UnifiedRuntimeRepository.setSpeed(context, when (value) {
            PublishSpeedProfile.INSTANT, PublishSpeedProfile.TURBO, PublishSpeedProfile.FAST -> UnifiedRuntimeSettingsStore.Speed.HYPER
            PublishSpeedProfile.SAFE -> UnifiedRuntimeSettingsStore.Speed.SAFE
            PublishSpeedProfile.ADAPTIVE -> UnifiedRuntimeSettingsStore.Speed.ADAPTIVE
        })
    }

    fun maxAttempts(): Int = 1
    fun setMaxAttempts(value: Int) { /* single-flight by design */ }

    fun navigationMode(): PublishNavigationMode = runCatching {
        PublishNavigationMode.valueOf(
            prefs.getString(
                "navigation_mode",
                PublishNavigationMode.AUTO.name
            )!!
        )
    }.getOrDefault(PublishNavigationMode.AUTO)

    fun setNavigationMode(value: PublishNavigationMode) {
        prefs.edit()
            .putString("navigation_mode", value.name)
            .apply()
    }

    fun lastMessage(): String = prefs.getString("last_message", "").orEmpty()
    fun setLastMessage(value: String) { prefs.edit().putString("last_message", value.take(16_000)).apply() }

    fun contentMode(): PublishContentMode = runCatching {
        PublishContentMode.valueOf(prefs.getString("content_mode", PublishContentMode.SINGLE_TEXT.name)!!)
    }.getOrDefault(PublishContentMode.SINGLE_TEXT)
    fun setContentMode(value: PublishContentMode) { prefs.edit().putString("content_mode", value.name).apply() }

    fun attachmentUri(): String? = prefs.getString("attachment_uri", null)
    fun attachmentMime(): String? = prefs.getString("attachment_mime", null)
    fun setAttachment(uri: String?, mime: String?) {
        prefs.edit().apply {
            if (uri == null) remove("attachment_uri") else putString("attachment_uri", uri)
            if (mime == null) remove("attachment_mime") else putString("attachment_mime", mime)
        }.apply()
    }
}
