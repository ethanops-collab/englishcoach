package com.englishcoach.app.core.i18n

import com.englishcoach.app.core.model.SupportedLanguage
import javax.inject.Inject

/**
 * A language the product targets. [isFullyTranslated] is `true` only once a matching
 * `values-<tag>` resource folder actually exists — adding the rest is a pure translation
 * task (new `values-xx/strings.xml`), never a code change, per the language-pack
 * architecture requirement.
 */
data class LanguageCatalogEntry(
    val language: SupportedLanguage,
    val isFullyTranslated: Boolean,
)

/**
 * The full roadmap of target UI languages (non-native English speakers learning English:
 * Korean, Japanese, Chinese, Spanish, Portuguese, French, German, Italian, Vietnamese, Thai,
 * Indonesian, Turkish, Arabic, Hindi, Russian, Polish), plus English itself as the
 * always-available fallback.
 */
class SupportedLanguageProvider @Inject constructor() {

    val all: List<LanguageCatalogEntry> = listOf(
        entry("en", "English", "English", translated = true),
        entry("ko", "Korean", "한국어", translated = true),
        entry("ja", "Japanese", "日本語", translated = true),
        entry("es", "Spanish", "Español", translated = true),
        entry("zh", "Chinese", "中文"),
        entry("pt", "Portuguese", "Português"),
        entry("fr", "French", "Français"),
        entry("de", "German", "Deutsch"),
        entry("it", "Italian", "Italiano"),
        entry("vi", "Vietnamese", "Tiếng Việt"),
        entry("th", "Thai", "ไทย"),
        entry("id", "Indonesian", "Bahasa Indonesia"),
        entry("tr", "Turkish", "Türkçe"),
        entry("ar", "Arabic", "العربية"),
        entry("hi", "Hindi", "हिन्दी"),
        entry("ru", "Russian", "Русский"),
        entry("pl", "Polish", "Polski"),
    )

    /** What the in-app language picker should actually offer today. */
    fun selectable(): List<SupportedLanguage> = all.filter { it.isFullyTranslated }.map { it.language }

    fun isSupported(bcp47Tag: String): Boolean =
        all.any { it.isFullyTranslated && it.language.bcp47Tag.equals(bcp47Tag, ignoreCase = true) }

    private fun entry(tag: String, englishName: String, nativeName: String, translated: Boolean = false) =
        LanguageCatalogEntry(SupportedLanguage(tag, englishName, nativeName), translated)
}
