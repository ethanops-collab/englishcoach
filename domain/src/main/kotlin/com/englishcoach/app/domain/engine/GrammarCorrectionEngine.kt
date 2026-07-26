package com.englishcoach.app.domain.engine

import com.englishcoach.app.core.model.GrammarCorrection
import javax.inject.Inject

data class ParsedCoachTurn(val correction: GrammarCorrection?, val coachReply: String)

/**
 * Parses the fixed `CORRECTED:`/`EXPLANATION:`/`REPLY:` (or `NO_ERROR:`/`REPLY:`) contract
 * defined in [com.englishcoach.app.domain.prompt.CoachPromptTemplates.correctionContract].
 * Implements step 1-2 of CLAUDE.md's Correction Style: show the corrected sentence, then a
 * brief explanation - never just continue chatting past a mistake.
 */
class GrammarCorrectionEngine @Inject constructor() {

    fun parse(originalUserText: String, llmResponseText: String): ParsedCoachTurn {
        val lines = llmResponseText.lines().map { it.trim() }.filter { it.isNotEmpty() }
        val correctedLine = lines.firstOrNull { it.startsWith("CORRECTED:") }
        val explanationLine = lines.firstOrNull { it.startsWith("EXPLANATION:") }
        val replyLine = lines.firstOrNull { it.startsWith("REPLY:") }
        val reply = replyLine?.removePrefix("REPLY:")?.trim().orEmpty()

        val correction = correctedLine?.let {
            GrammarCorrection(
                originalText = originalUserText,
                correctedText = it.removePrefix("CORRECTED:").trim(),
                explanation = explanationLine?.removePrefix("EXPLANATION:")?.trim().orEmpty(),
            )
        }

        return ParsedCoachTurn(correction = correction, coachReply = reply)
    }
}
