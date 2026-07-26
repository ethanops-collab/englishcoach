package com.englishcoach.app.feature.lesson.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.englishcoach.app.core.designsystem.component.MissionCard
import com.englishcoach.app.core.i18n.LessonCopy
import com.englishcoach.app.core.i18n.R as I18nR
import com.englishcoach.app.core.model.Lesson

@Composable
fun LessonListRoute(
    onSelectLesson: (lessonId: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LessonListViewModel = hiltViewModel(),
) {
    val lessons by viewModel.lessons.collectAsStateWithLifecycle()
    LessonListScreen(lessons = lessons, onSelectLesson = onSelectLesson, modifier = modifier)
}

@Composable
fun LessonListScreen(
    lessons: List<Lesson>,
    onSelectLesson: (lessonId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(text = stringResource(I18nR.string.lesson_list_title), style = MaterialTheme.typography.headlineMedium)
        }
        items(lessons, key = { it.id }) { lesson ->
            MissionCard(
                title = stringResource(LessonCopy.titleRes(lesson.type)),
                subtitle = stringResource(LessonCopy.missionRes(lesson.type)),
                ctaLabel = stringResource(I18nR.string.lesson_start_cta),
                onClick = { onSelectLesson(lesson.id) },
            )
        }
    }
}
