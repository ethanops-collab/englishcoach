package com.englishcoach.app.feature.progress.summary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.englishcoach.app.core.designsystem.component.CoachCard
import com.englishcoach.app.core.designsystem.component.ScoreRing
import com.englishcoach.app.core.designsystem.theme.Dimens
import com.englishcoach.app.core.i18n.R as I18nR
import com.englishcoach.app.core.model.LessonAttempt

@Composable
fun SummaryRoute(
    lessonId: String,
    onDone: () -> Unit,
    onReplay: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SummaryViewModel = hiltViewModel(),
) {
    LaunchedEffect(lessonId) { viewModel.load(lessonId) }
    val attempt by viewModel.attempt.collectAsStateWithLifecycle()
    val vocabulary by viewModel.vocabularyLearned.collectAsStateWithLifecycle()

    attempt?.let {
        SummaryScreen(
            attempt = it,
            vocabularyCount = vocabulary.size,
            onDone = onDone,
            onReplay = onReplay,
            modifier = modifier,
        )
    }
}

@Composable
fun SummaryScreen(
    attempt: LessonAttempt,
    vocabularyCount: Int,
    onDone: () -> Unit,
    onReplay: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val mistakes = attempt.turns.mapNotNull { it.correction }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(Dimens.ScreenPadding),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceL),
    ) {
        item {
            Text(text = stringResource(I18nR.string.summary_title), style = MaterialTheme.typography.headlineMedium)
        }

        item {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Row2 {
                    attempt.pronunciationScore?.let { score ->
                        ScoreLabelColumn(
                            label = stringResource(I18nR.string.summary_pronunciation_score_label),
                        ) { ScoreRing(score = score.overall) }
                    }
                    attempt.fluencyScore?.let { fluency ->
                        ScoreLabelColumn(label = stringResource(I18nR.string.summary_fluency_score_label)) {
                            ScoreRing(score = fluency)
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "${stringResource(I18nR.string.summary_grammar_mistakes_label)} (${mistakes.size})",
                style = MaterialTheme.typography.titleMedium,
            )
        }
        if (mistakes.isEmpty()) {
            item { Text(text = stringResource(I18nR.string.summary_no_mistakes)) }
        } else {
            items(mistakes) { correction ->
                CoachCard(modifier = Modifier.fillMaxWidth()) {
                    Text(text = correction.correctedText, style = MaterialTheme.typography.bodyLarge)
                    Text(text = correction.explanation, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        item {
            Text(
                text = "${stringResource(I18nR.string.summary_vocabulary_learned_label)} ($vocabularyCount)",
                style = MaterialTheme.typography.titleMedium,
            )
        }

        if (attempt.wordsToReview.isNotEmpty()) {
            item {
                Text(text = stringResource(I18nR.string.summary_words_to_review_label), style = MaterialTheme.typography.titleMedium)
            }
            item { Text(text = attempt.wordsToReview.joinToString(" • ")) }
        }

        item {
            OutlinedButton(onClick = onReplay, modifier = Modifier.fillMaxWidth()) {
                Text(text = stringResource(I18nR.string.summary_replay_conversation_cta))
            }
        }
        item {
            Button(onClick = onDone, modifier = Modifier.fillMaxWidth()) {
                Text(text = stringResource(I18nR.string.summary_done_cta))
            }
        }
    }
}

@Composable
private fun Row2(content: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit) {
    androidx.compose.foundation.layout.Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceXL), content = content)
}

@Composable
private fun ScoreLabelColumn(label: String, ring: @Composable () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        ring()
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
    }
}
