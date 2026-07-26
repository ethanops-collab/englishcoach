package com.englishcoach.app.domain.content

import com.englishcoach.app.core.model.Lesson
import com.englishcoach.app.core.model.LessonDifficulty
import com.englishcoach.app.core.model.LessonType

/**
 * One seed [Lesson] per [LessonType] from the product spec. `titleKey`/`missionKey` follow
 * the `lesson_title_<type>` / `lesson_mission_<type>` string-resource naming convention that
 * :core:i18n's language packs implement, so a language pack never needs a lookup table -
 * just resources named to match.
 */
object LessonCatalog {
    val all: List<Lesson> = LessonType.entries.map { type ->
        val key = type.name.lowercase()
        Lesson(
            id = "lesson_$key",
            type = type,
            difficulty = LessonDifficulty.BEGINNER,
            titleKey = "lesson_title_$key",
            missionKey = "lesson_mission_$key",
            estimatedMinutes = 5,
            targetPhraseCount = 6,
        )
    }

    fun byType(type: LessonType): Lesson = all.first { it.type == type }
}
