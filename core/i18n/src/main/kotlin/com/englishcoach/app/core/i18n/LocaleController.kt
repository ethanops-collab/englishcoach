package com.englishcoach.app.core.i18n

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import javax.inject.Inject

/**
 * Applies the resolved UI language at the OS level via AndroidX per-app language APIs, so
 * every screen's `stringResource(...)` call picks it up automatically - no custom string
 * lookup layer needed.
 */
class LocaleController @Inject constructor() {
    fun applyLocale(bcp47Tag: String?) {
        val locales = if (bcp47Tag == null) {
            LocaleListCompat.getEmptyLocaleList()
        } else {
            LocaleListCompat.forLanguageTags(bcp47Tag)
        }
        AppCompatDelegate.setApplicationLocales(locales)
    }
}
