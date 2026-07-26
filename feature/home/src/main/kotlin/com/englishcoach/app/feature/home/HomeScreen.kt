package com.englishcoach.app.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.englishcoach.app.core.designsystem.component.CoachCard
import com.englishcoach.app.core.designsystem.component.MissionCard
import com.englishcoach.app.core.designsystem.component.StreakBadge
import com.englishcoach.app.core.designsystem.theme.Dimens
import com.englishcoach.app.core.i18n.LessonCopy
import com.englishcoach.app.core.i18n.R as I18nR

@Composable
fun HomeRoute(
    onStartLesson: (lessonId: String) -> Unit,
    onOpenLessonList: () -> Unit,
    onOpenReview: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HomeScreen(
        uiState = uiState,
        onStartMission = onStartLesson,
        onOpenLessonList = onOpenLessonList,
        onOpenReview = onOpenReview,
        modifier = modifier,
    )
}

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onStartMission: (lessonId: String) -> Unit,
    onOpenLessonList: () -> Unit,
    onOpenReview: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(Dimens.ScreenPadding),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceL),
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = stringResource(I18nR.string.app_name), style = MaterialTheme.typography.headlineMedium)
                StreakBadge(streakDays = uiState.progress.streakDays)
            }
        }

        uiState.todaysMission?.let { mission ->
            item {
                MissionCard(
                    title = stringResource(I18nR.string.home_today_mission_title),
                    subtitle = stringResource(LessonCopy.missionRes(mission.type)),
                    ctaLabel = stringResource(I18nR.string.home_start_cta),
                    onClick = { onStartMission(mission.id) },
                )
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceM)) {
                StatTile(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.Timer,
                    label = stringResource(I18nR.string.home_speaking_minutes_label),
                    value = uiState.progress.totalSpeakingMinutes.toString(),
                    onClick = null,
                )
                StatTile(
                    modifier = Modifier.weight(1f),
                    icon = Icons.AutoMirrored.Filled.MenuBook,
                    label = stringResource(I18nR.string.home_recommended_review_label),
                    value = uiState.dueReviewCount.toString(),
                    onClick = onOpenReview,
                )
            }
        }

        if (uiState.weakSounds.isNotEmpty()) {
            item {
                CoachCard(modifier = Modifier.fillMaxWidth()) {
                    Row {
                        Icon(Icons.Filled.RecordVoiceOver, contentDescription = null)
                        Text(
                            text = stringResource(I18nR.string.home_weak_pronunciation_label),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(start = Dimens.SpaceS),
                        )
                    }
                    uiState.weakSounds.forEach { sound ->
                        Text(text = "• $sound", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        item {
            Text(
                text = stringResource(I18nR.string.lesson_list_title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = Dimens.SpaceS),
            )
        }
        item {
            TextButton(onClick = onOpenLessonList) {
                Text(text = stringResource(I18nR.string.home_continue_lesson_title))
            }
        }
    }
}

@Composable
private fun StatTile(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    CoachCard(modifier = modifier, onClick = onClick) {
        Icon(icon, contentDescription = null)
        Text(text = value, style = MaterialTheme.typography.titleLarge)
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
    }
}
