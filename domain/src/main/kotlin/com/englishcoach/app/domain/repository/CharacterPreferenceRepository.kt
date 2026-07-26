package com.englishcoach.app.domain.repository

import com.englishcoach.app.core.model.CharacterPreference
import com.englishcoach.app.core.model.LessonType
import kotlinx.coroutines.flow.Flow

interface CharacterPreferenceRepository {
    fun observe(lessonType: LessonType): Flow<CharacterPreference?>
    suspend fun save(lessonType: LessonType, avatarImagePath: String?, displayName: String?)
    suspend fun clear(lessonType: LessonType)
}
