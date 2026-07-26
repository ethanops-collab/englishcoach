package com.englishcoach.app.core.model

/**
 * The fixed catalog of coaching scenarios from the product spec. Adding a new type here
 * requires a matching entry in every language pack's lesson copy.
 */
enum class LessonType {
    DAILY_CONVERSATION,
    RESTAURANT,
    AIRPORT,
    SHOPPING,
    HOTEL,
    JOB_INTERVIEW,
    BUSINESS_ENGLISH,
    TRAVEL,
    DOCTOR,
    PHONE_CALLS,
    SMALL_TALK,
    EMERGENCY,
    DATING,
}

enum class LessonDifficulty {
    BEGINNER,
    INTERMEDIATE,
    ADVANCED,
}

data class Lesson(
    val id: String,
    val type: LessonType,
    val difficulty: LessonDifficulty,
    val titleKey: String,
    val missionKey: String,
    val estimatedMinutes: Int,
    val targetPhraseCount: Int,
)
