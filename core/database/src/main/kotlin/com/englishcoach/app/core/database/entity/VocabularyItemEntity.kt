package com.englishcoach.app.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "vocabulary", indices = [Index("lessonId")])
data class VocabularyItemEntity(
    @PrimaryKey val id: String,
    val word: String,
    val exampleSentence: String,
    val lessonId: String,
    val learnedAtEpochMs: Long,
    val masteryLevel: String,
)
