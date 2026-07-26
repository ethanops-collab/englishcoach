package com.englishcoach.app.core.model

/**
 * A user's optional cosmetic customization of the role-play partner for a given
 * [LessonType] - an avatar photo and/or display name. Purely a visual/UI preference:
 * it never affects the coach persona, prompt, or conversation content.
 */
data class CharacterPreference(
    val lessonType: LessonType,
    val avatarImagePath: String? = null,
    val displayName: String? = null,
)
