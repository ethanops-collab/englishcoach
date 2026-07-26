package com.englishcoach.app.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.englishcoach.app.domain.engine.LessonEngine
import com.englishcoach.app.domain.repository.LessonRepository
import com.englishcoach.app.domain.repository.MistakeRepository
import com.englishcoach.app.domain.repository.ProgressRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val lessonEngine: LessonEngine,
    private val lessonRepository: LessonRepository,
    private val progressRepository: ProgressRepository,
    private val mistakeRepository: MistakeRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    init {
        viewModelScope.launch {
            lessonEngine.ensureCatalogSeeded()
            val mission = lessonEngine.todaysMission()
            val weakSounds = lessonRepository.lastAttempt(mission.id)?.pronunciationScore?.missingSounds.orEmpty()
            _uiState.update { it.copy(todaysMission = mission, weakSounds = weakSounds) }
        }
        viewModelScope.launch {
            combine(
                progressRepository.observeProgress(),
                mistakeRepository.observeDueMistakes(System.currentTimeMillis()),
            ) { progress, dueMistakes -> progress to dueMistakes.size }
                .collect { (progress, dueCount) ->
                    _uiState.update { it.copy(isLoading = false, progress = progress, dueReviewCount = dueCount) }
                }
        }
    }
}
