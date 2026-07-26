package com.englishcoach.app.core.database.di

import android.content.Context
import androidx.room.Room
import com.englishcoach.app.core.database.AppDatabase
import com.englishcoach.app.core.database.MIGRATION_1_2
import com.englishcoach.app.core.database.dao.CharacterPreferenceDao
import com.englishcoach.app.core.database.dao.ConversationTurnDao
import com.englishcoach.app.core.database.dao.LessonAttemptDao
import com.englishcoach.app.core.database.dao.LessonDao
import com.englishcoach.app.core.database.dao.MistakeDao
import com.englishcoach.app.core.database.dao.UserProgressDao
import com.englishcoach.app.core.database.dao.VocabularyDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.DATABASE_NAME)
            .addMigrations(MIGRATION_1_2)
            .build()

    @Provides
    fun provideLessonDao(database: AppDatabase): LessonDao = database.lessonDao()

    @Provides
    fun provideLessonAttemptDao(database: AppDatabase): LessonAttemptDao = database.lessonAttemptDao()

    @Provides
    fun provideConversationTurnDao(database: AppDatabase): ConversationTurnDao = database.conversationTurnDao()

    @Provides
    fun provideMistakeDao(database: AppDatabase): MistakeDao = database.mistakeDao()

    @Provides
    fun provideVocabularyDao(database: AppDatabase): VocabularyDao = database.vocabularyDao()

    @Provides
    fun provideUserProgressDao(database: AppDatabase): UserProgressDao = database.userProgressDao()

    @Provides
    fun provideCharacterPreferenceDao(database: AppDatabase): CharacterPreferenceDao =
        database.characterPreferenceDao()
}
