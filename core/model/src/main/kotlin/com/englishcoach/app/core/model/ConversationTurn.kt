package com.englishcoach.app.core.model

enum class Speaker { USER, COACH }

data class ConversationTurn(
    val id: String,
    val speaker: Speaker,
    val text: String,
    val timestampMs: Long,
    val pronunciationScore: PronunciationScore? = null,
    val correction: GrammarCorrection? = null,
)

/**
 * [explanation] is plain display text, not a resource key: it is generated per-mistake by
 * the on-device LLM (in the learner's UI language, per CLAUDE.md's localization rules), so
 * it can't be a static string-resource lookup the way lesson copy is.
 */
data class GrammarCorrection(
    val originalText: String,
    val correctedText: String,
    val explanation: String,
)
