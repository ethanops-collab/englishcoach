package com.englishcoach.app.core.i18n

import androidx.core.os.LocaleListCompat
import javax.inject.Inject

/**
 * First-launch language detection: walks the device's ordered locale preferences and picks
 * the first one this app has a translated language pack for, otherwise falls back to
 * English - implementing CLAUDE.md's "detect device language, else English" rule.
 */
class DeviceLocaleResolver @Inject constructor(
    private val supportedLanguageProvider: SupportedLanguageProvider,
) {
    fun resolveInitialLanguageTag(deviceLocales: LocaleListCompat): String {
        for (index in 0 until deviceLocales.size()) {
            val locale = deviceLocales[index] ?: continue
            val language = locale.language
            if (supportedLanguageProvider.isSupported(language)) return language
        }
        return "en"
    }
}
