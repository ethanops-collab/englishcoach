package com.englishcoach.app.engine.pronunciation

/** Phoneme/prosody trouble spots the scorer weights more heavily. Pure data, no localized text. */
enum class ProblemSound {
    L_R,
    R,
    TH,
    V_B,
    F_P,
    V_W,
    H,
    R_ENDING,
    LINKING,
    CONSONANT_ENDINGS,
    SHORT_VOWELS,
    STRESS,
}

/**
 * Which [ProblemSound]s to weight for a given native language (BCP-47 primary subtag),
 * seeded from the CLAUDE.md country-specific pronunciation table. Adding a new native
 * language here requires no other code changes — this is the whole rule set.
 */
object NativeLanguageProblemSoundProfiles {
    private val profiles: Map<String, Set<ProblemSound>> = mapOf(
        "ko" to setOf(ProblemSound.L_R, ProblemSound.F_P, ProblemSound.V_B, ProblemSound.TH),
        "ja" to setOf(ProblemSound.L_R, ProblemSound.TH, ProblemSound.CONSONANT_ENDINGS),
        "zh" to setOf(ProblemSound.R, ProblemSound.TH, ProblemSound.V_W),
        "es" to setOf(ProblemSound.V_B, ProblemSound.H, ProblemSound.SHORT_VOWELS),
        "fr" to setOf(ProblemSound.H, ProblemSound.TH, ProblemSound.STRESS),
    )

    private val default: Set<ProblemSound> =
        setOf(ProblemSound.L_R, ProblemSound.TH, ProblemSound.V_B, ProblemSound.R_ENDING, ProblemSound.LINKING)

    fun forNativeLanguage(bcp47Tag: String): Set<ProblemSound> {
        val primarySubtag = bcp47Tag.substringBefore('-').lowercase()
        return profiles[primarySubtag] ?: default
    }
}
