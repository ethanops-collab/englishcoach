package com.englishcoach.app.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lessons")
data class LessonEntity(
    @PrimaryKey val id: String,
    val type: String,
    val difficulty: String,
    val titleKey: String,
    val missionKey: String,
    val estimatedMinutes: Int,
    val targetPhraseCount: Int,
)
