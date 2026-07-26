package com.englishcoach.app.core.model

enum class MasteryLevel { NEW, LEARNING, FAMILIAR, MASTERED }

data class VocabularyItem(
    val id: String,
    val word: String,
    val exampleSentence: String,
    val lessonId: String,
    val learnedAtEpochMs: Long,
    val masteryLevel: MasteryLevel,
)
