package com.englishcoach.app.feature.progress.review

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.englishcoach.app.core.designsystem.component.CoachCard
import com.englishcoach.app.core.designsystem.theme.Dimens
import com.englishcoach.app.core.i18n.R as I18nR
import com.englishcoach.app.core.model.MistakeRecord

@Composable
fun ReviewRoute(modifier: Modifier = Modifier, viewModel: ReviewViewModel = hiltViewModel()) {
    val dueMistakes by viewModel.dueMistakes.collectAsStateWithLifecycle()
    ReviewScreen(dueMistakes = dueMistakes, onMarkReviewed = viewModel::markReviewed, modifier = modifier)
}

@Composable
fun ReviewScreen(
    dueMistakes: List<MistakeRecord>,
    onMarkReviewed: (MistakeRecord) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(Dimens.ScreenPadding),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceM),
    ) {
        item {
            Text(text = stringResource(I18nR.string.home_recommended_review_label), style = MaterialTheme.typography.headlineMedium)
        }
        if (dueMistakes.isEmpty()) {
            item { Text(text = stringResource(I18nR.string.summary_no_mistakes)) }
        }
        items(dueMistakes, key = { it.id }) { mistake ->
            CoachCard(modifier = Modifier.fillMaxWidth()) {
                Text(text = mistake.originalText, style = MaterialTheme.typography.bodyMedium)
                Text(text = mistake.correctedText, style = MaterialTheme.typography.titleMedium)
                Text(text = mistake.explanation, style = MaterialTheme.typography.bodyMedium)
                Button(onClick = { onMarkReviewed(mistake) }, modifier = Modifier.padding(top = Dimens.SpaceS)) {
                    Text(text = stringResource(I18nR.string.correction_repeat_cta))
                }
            }
        }
    }
}
