package com.englishcoach.app.domain.repository

import com.englishcoach.app.core.model.VocabularyItem
import kotlinx.coroutines.flow.Flow

interface VocabularyRepository {
    suspend fun recordVocabulary(item: VocabularyItem)
    fun observeVocabulary(): Flow<List<VocabularyItem>>
    fun observeForLesson(lessonId: String): Flow<List<VocabularyItem>>
}
