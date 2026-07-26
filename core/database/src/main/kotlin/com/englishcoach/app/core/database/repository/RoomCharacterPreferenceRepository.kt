package com.englishcoach.app.core.database.repository

import com.englishcoach.app.core.database.dao.CharacterPreferenceDao
import com.englishcoach.app.core.database.entity.CharacterPreferenceEntity
import com.englishcoach.app.core.database.mapper.toDomain
import com.englishcoach.app.core.model.CharacterPreference
import com.englishcoach.app.core.model.LessonType
import com.englishcoach.app.domain.repository.CharacterPreferenceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RoomCharacterPreferenceRepository @Inject constructor(
    private val characterPreferenceDao: CharacterPreferenceDao,
) : CharacterPreferenceRepository {

    override fun observe(lessonType: LessonType): Flow<CharacterPreference?> =
        characterPreferenceDao.observe(lessonType.name).map { it?.toDomain() }

    override suspend fun save(lessonType: LessonType, avatarImagePath: String?, displayName: String?) {
        characterPreferenceDao.upsert(
            CharacterPreferenceEntity(
                lessonType = lessonType.name,
                avatarImagePath = avatarImagePath,
                displayName = displayName,
            ),
        )
    }

    override suspend fun clear(lessonType: LessonType) {
        characterPreferenceDao.clear(lessonType.name)
    }
}
