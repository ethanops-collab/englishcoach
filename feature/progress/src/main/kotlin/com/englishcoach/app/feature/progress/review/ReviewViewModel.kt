package com.englishcoach.app.feature.progress.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.englishcoach.app.core.model.MistakeRecord
import com.englishcoach.app.domain.engine.ReviewScheduler
import com.englishcoach.app.domain.repository.MistakeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReviewViewModel @Inject constructor(
    private val mistakeRepository: MistakeRepository,
) : ViewModel() {

    val dueMistakes: StateFlow<List<MistakeRecord>> = mistakeRepository
        .observeDueMistakes(System.currentTimeMillis())
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Marks an item reviewed and pushes it further out on the spaced-repetition schedule. */
    fun markReviewed(mistake: MistakeRecord) {
        viewModelScope.launch {
            val nextInterval = ReviewScheduler.nextIntervalDays(mistake.intervalDays)
            val now = System.currentTimeMillis()
            mistakeRepository.markReviewed(
                mistakeId = mistake.id,
                nextReviewAtEpochMs = ReviewScheduler.nextReviewAtEpochMs(now, nextInterval),
                newIntervalDays = nextInterval,
                timesReviewed = mistake.timesReviewed + 1,
            )
        }
    }
}
