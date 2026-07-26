package com.englishcoach.app.core.database.di

import com.englishcoach.app.core.database.repository.RoomCharacterPreferenceRepository
import com.englishcoach.app.core.database.repository.RoomLessonRepository
import com.englishcoach.app.core.database.repository.RoomMistakeRepository
import com.englishcoach.app.core.database.repository.RoomProgressRepository
import com.englishcoach.app.core.database.repository.RoomVocabularyRepository
import com.englishcoach.app.domain.repository.CharacterPreferenceRepository
import com.englishcoach.app.domain.repository.LessonRepository
import com.englishcoach.app.domain.repository.MistakeRepository
import com.englishcoach.app.domain.repository.ProgressRepository
import com.englishcoach.app.domain.repository.VocabularyRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindLessonRepository(impl: RoomLessonRepository): LessonRepository

    @Binds
    @Singleton
    abstract fun bindProgressRepository(impl: RoomProgressRepository): ProgressRepository

    @Binds
    @Singleton
    abstract fun bindMistakeRepository(impl: RoomMistakeRepository): MistakeRepository

    @Binds
    @Singleton
    abstract fun bindVocabularyRepository(impl: RoomVocabularyRepository): VocabularyRepository

    @Binds
    @Singleton
    abstract fun bindCharacterPreferenceRepository(impl: RoomCharacterPreferenceRepository): CharacterPreferenceRepository
}
