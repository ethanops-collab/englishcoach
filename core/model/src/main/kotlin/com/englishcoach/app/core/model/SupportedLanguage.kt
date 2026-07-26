package com.englishcoach.app.core.model

/**
 * A UI/native language the app can coach in. [bcp47Tag] drives locale resolution;
 * [nativeName] is always shown in that language's own script, never translated.
 */
data class SupportedLanguage(
    val bcp47Tag: String,
    val englishName: String,
    val nativeName: String,
)
