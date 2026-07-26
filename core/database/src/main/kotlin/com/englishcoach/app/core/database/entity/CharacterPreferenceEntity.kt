package com.englishcoach.app.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "character_preferences")
data class CharacterPreferenceEntity(
    @PrimaryKey val lessonType: String,
    val avatarImagePath: String?,
    val displayName: String?,
)
