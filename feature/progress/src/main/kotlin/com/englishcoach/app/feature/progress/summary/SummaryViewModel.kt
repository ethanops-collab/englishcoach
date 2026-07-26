package com.englishcoach.app.feature.progress.summary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.englishcoach.app.core.model.LessonAttempt
import com.englishcoach.app.core.model.VocabularyItem
import com.englishcoach.app.domain.repository.LessonRepository
import com.englishcoach.app.domain.repository.VocabularyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SummaryViewModel @Inject constructor(
    private val lessonRepository: LessonRepository,
    private val vocabularyRepository: VocabularyRepository,
) : ViewModel() {

    private val _attempt = MutableStateFlow<LessonAttempt?>(null)
    val attempt: StateFlow<LessonAttempt?> = _attempt

    private val _vocabularyLearned = MutableStateFlow<List<VocabularyItem>>(emptyList())
    val vocabularyLearned: StateFlow<List<VocabularyItem>> = _vocabularyLearned

    fun load(lessonId: String) {
        viewModelScope.launch {
            _attempt.value = lessonRepository.lastAttempt(lessonId)
        }
        viewModelScope.launch {
            vocabularyRepository.observeForLesson(lessonId).collect { _vocabularyLearned.value = it }
        }
    }
}
