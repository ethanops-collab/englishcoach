package com.englishcoach.app.core.database.mapper

import com.englishcoach.app.core.database.entity.CharacterPreferenceEntity
import com.englishcoach.app.core.database.entity.ConversationTurnEntity
import com.englishcoach.app.core.database.entity.LessonAttemptEntity
import com.englishcoach.app.core.database.entity.LessonEntity
import com.englishcoach.app.core.database.entity.MistakeRecordEntity
import com.englishcoach.app.core.database.entity.UserProgressEntity
import com.englishcoach.app.core.database.entity.VocabularyItemEntity
import com.englishcoach.app.core.model.CharacterPreference
import com.englishcoach.app.core.model.ConversationTurn
import com.englishcoach.app.core.model.GrammarCorrection
import com.englishcoach.app.core.model.Lesson
import com.englishcoach.app.core.model.LessonAttempt
import com.englishcoach.app.core.model.LessonDifficulty
import com.englishcoach.app.core.model.LessonType
import com.englishcoach.app.core.model.MasteryLevel
import com.englishcoach.app.core.model.MistakeRecord
import com.englishcoach.app.core.model.MistakeType
import com.englishcoach.app.core.model.PronunciationScore
import com.englishcoach.app.core.model.Speaker
import com.englishcoach.app.core.model.UserProgress
import com.englishcoach.app.core.model.VocabularyItem
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val json = Json { ignoreUnknownKeys = true }

fun LessonEntity.toDomain() = Lesson(
    id = id,
    type = LessonType.valueOf(type),
    difficulty = LessonDifficulty.valueOf(difficulty),
    titleKey = titleKey,
    missionKey = missionKey,
    estimatedMinutes = estimatedMinutes,
    targetPhraseCount = targetPhraseCount,
)

fun Lesson.toEntity() = LessonEntity(
    id = id,
    type = type.name,
    difficulty = difficulty.name,
    titleKey = titleKey,
    missionKey = missionKey,
    estimatedMinutes = estimatedMinutes,
    targetPhraseCount = targetPhraseCount,
)

fun ConversationTurnEntity.toDomain() = ConversationTurn(
    id = id,
    speaker = Speaker.valueOf(speaker),
    text = text,
    timestampMs = timestampMs,
    correction = if (correctionCorrectedText != null) {
        GrammarCorrection(
            originalText = correctionOriginalText.orEmpty(),
            correctedText = correctionCorrectedText,
            explanation = correctionExplanation.orEmpty(),
        )
    } else {
        null
    },
)

fun ConversationTurn.toEntity(attemptId: String) = ConversationTurnEntity(
    id = id,
    attemptId = attemptId,
    speaker = speaker.name,
    text = text,
    timestampMs = timestampMs,
    correctionOriginalText = correction?.originalText,
    correctionCorrectedText = correction?.correctedText,
    correctionExplanation = correction?.explanation,
)

fun LessonAttemptEntity.toDomain(turns: List<ConversationTurn>) = LessonAttempt(
    id = id,
    lessonId = lessonId,
    startedAtEpochMs = startedAtEpochMs,
    completedAtEpochMs = completedAtEpochMs,
    turns = turns,
    fluencyScore = fluencyScore,
    pronunciationScore = pronunciationOverall?.let {
        PronunciationScore(
            overall = it,
            accuracy = pronunciationAccuracy ?: 0f,
            stress = pronunciationStress ?: 0f,
            rhythm = pronunciationRhythm ?: 0f,
            intonation = pronunciationIntonation ?: 0f,
            missingSounds = pronunciationMissingSoundsCsv?.split(",")?.filter(String::isNotBlank).orEmpty(),
            problemSoundScores = pronunciationProblemSoundScoresJson
                ?.let { raw -> json.decodeFromString<Map<String, Float>>(raw) }
                .orEmpty(),
        )
    },
    xpEarned = xpEarned,
    wordsToReview = wordsToReviewCsv.split(",").filter(String::isNotBlank),
)

fun LessonAttempt.toEntity() = LessonAttemptEntity(
    id = id,
    lessonId = lessonId,
    startedAtEpochMs = startedAtEpochMs,
    completedAtEpochMs = completedAtEpochMs,
    fluencyScore = fluencyScore,
    pronunciationOverall = pronunciationScore?.overall,
    pronunciationAccuracy = pronunciationScore?.accuracy,
    pronunciationStress = pronunciationScore?.stress,
    pronunciationRhythm = pronunciationScore?.rhythm,
    pronunciationIntonation = pronunciationScore?.intonation,
    pronunciationMissingSoundsCsv = pronunciationScore?.missingSounds?.joinToString(","),
    pronunciationProblemSoundScoresJson = pronunciationScore?.problemSoundScores?.let { json.encodeToString(it) },
    xpEarned = xpEarned,
    wordsToReviewCsv = wordsToReview.joinToString(","),
)

fun MistakeRecordEntity.toDomain() = MistakeRecord(
    id = id,
    type = MistakeType.valueOf(type),
    lessonId = lessonId,
    originalText = originalText,
    correctedText = correctedText,
    explanation = explanation,
    createdAtEpochMs = createdAtEpochMs,
    nextReviewAtEpochMs = nextReviewAtEpochMs,
    intervalDays = intervalDays,
    timesReviewed = timesReviewed,
)

fun MistakeRecord.toEntity() = MistakeRecordEntity(
    id = id,
    type = type.name,
    lessonId = lessonId,
    originalText = originalText,
    correctedText = correctedText,
    explanation = explanation,
    createdAtEpochMs = createdAtEpochMs,
    nextReviewAtEpochMs = nextReviewAtEpochMs,
    intervalDays = intervalDays,
    timesReviewed = timesReviewed,
)

fun VocabularyItemEntity.toDomain() = VocabularyItem(
    id = id,
    word = word,
    exampleSentence = exampleSentence,
    lessonId = lessonId,
    learnedAtEpochMs = learnedAtEpochMs,
    masteryLevel = MasteryLevel.valueOf(masteryLevel),
)

fun VocabularyItem.toEntity() = VocabularyItemEntity(
    id = id,
    word = word,
    exampleSentence = exampleSentence,
    lessonId = lessonId,
    learnedAtEpochMs = learnedAtEpochMs,
    masteryLevel = masteryLevel.name,
)

fun UserProgressEntity.toDomain() = UserProgress(
    streakDays = streakDays,
    xp = xp,
    level = level,
    totalSpeakingMinutes = totalSpeakingMinutes,
    lastActiveEpochDay = lastActiveEpochDay,
)

fun UserProgress.toEntity() = UserProgressEntity(
    streakDays = streakDays,
    xp = xp,
    level = level,
    totalSpeakingMinutes = totalSpeakingMinutes,
    lastActiveEpochDay = lastActiveEpochDay,
)

fun CharacterPreferenceEntity.toDomain() = CharacterPreference(
    lessonType = LessonType.valueOf(lessonType),
    avatarImagePath = avatarImagePath,
    displayName = displayName,
)

fun CharacterPreference.toEntity() = CharacterPreferenceEntity(
    lessonType = lessonType.name,
    avatarImagePath = avatarImagePath,
    displayName = displayName,
)
