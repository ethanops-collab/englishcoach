package com.englishcoach.app.core.database.repository

import com.englishcoach.app.core.database.dao.VocabularyDao
import com.englishcoach.app.core.database.mapper.toDomain
import com.englishcoach.app.core.database.mapper.toEntity
import com.englishcoach.app.core.model.VocabularyItem
import com.englishcoach.app.domain.repository.VocabularyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RoomVocabularyRepository @Inject constructor(
    private val vocabularyDao: VocabularyDao,
) : VocabularyRepository {

    override suspend fun recordVocabulary(item: VocabularyItem) {
        vocabularyDao.insert(item.toEntity())
    }

    override fun observeVocabulary(): Flow<List<VocabularyItem>> =
        vocabularyDao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override fun observeForLesson(lessonId: String): Flow<List<VocabularyItem>> =
        vocabularyDao.observeForLesson(lessonId).map { entities -> entities.map { it.toDomain() } }
}
