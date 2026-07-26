package com.englishcoach.app.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.englishcoach.app.core.database.dao.CharacterPreferenceDao
import com.englishcoach.app.core.database.dao.ConversationTurnDao
import com.englishcoach.app.core.database.dao.LessonAttemptDao
import com.englishcoach.app.core.database.dao.LessonDao
import com.englishcoach.app.core.database.dao.MistakeDao
import com.englishcoach.app.core.database.dao.UserProgressDao
import com.englishcoach.app.core.database.dao.VocabularyDao
import com.englishcoach.app.core.database.entity.CharacterPreferenceEntity
import com.englishcoach.app.core.database.entity.ConversationTurnEntity
import com.englishcoach.app.core.database.entity.LessonAttemptEntity
import com.englishcoach.app.core.database.entity.LessonEntity
import com.englishcoach.app.core.database.entity.MistakeRecordEntity
import com.englishcoach.app.core.database.entity.UserProgressEntity
import com.englishcoach.app.core.database.entity.VocabularyItemEntity

@Database(
    entities = [
        LessonEntity::class,
        LessonAttemptEntity::class,
        ConversationTurnEntity::class,
        MistakeRecordEntity::class,
        VocabularyItemEntity::class,
        UserProgressEntity::class,
        CharacterPreferenceEntity::class,
    ],
    version = 2,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun lessonDao(): LessonDao
    abstract fun lessonAttemptDao(): LessonAttemptDao
    abstract fun conversationTurnDao(): ConversationTurnDao
    abstract fun mistakeDao(): MistakeDao
    abstract fun vocabularyDao(): VocabularyDao
    abstract fun userProgressDao(): UserProgressDao
    abstract fun characterPreferenceDao(): CharacterPreferenceDao

    companion object {
        const val DATABASE_NAME = "english_coach.db"
    }
}
