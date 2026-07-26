package com.englishcoach.app.feature.home

import com.englishcoach.app.core.model.Lesson
import com.englishcoach.app.core.model.UserProgress

data class HomeUiState(
    val isLoading: Boolean = true,
    val todaysMission: Lesson? = null,
    val progress: UserProgress = UserProgress.initial(),
    val weakSounds: List<String> = emptyList(),
    val dueReviewCount: Int = 0,
)
