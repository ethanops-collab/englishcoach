package com.englishcoach.app.feature.lesson.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.englishcoach.app.core.model.Lesson
import com.englishcoach.app.domain.engine.LessonEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LessonListViewModel @Inject constructor(
    private val lessonEngine: LessonEngine,
) : ViewModel() {

    val lessons: StateFlow<List<Lesson>> = lessonEngine.observeLessons()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch { lessonEngine.ensureCatalogSeeded() }
    }
}
