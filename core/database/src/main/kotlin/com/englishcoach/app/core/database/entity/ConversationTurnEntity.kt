package com.englishcoach.app.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "conversation_turns", indices = [Index("attemptId")])
data class ConversationTurnEntity(
    @PrimaryKey val id: String,
    val attemptId: String,
    val speaker: String,
    val text: String,
    val timestampMs: Long,
    val correctionOriginalText: String?,
    val correctionCorrectedText: String?,
    val correctionExplanation: String?,
)
