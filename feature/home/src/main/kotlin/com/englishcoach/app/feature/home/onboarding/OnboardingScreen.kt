package com.englishcoach.app.feature.home.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.englishcoach.app.core.i18n.R as I18nR
import com.englishcoach.app.core.model.SupportedLanguage

@Composable
fun OnboardingRoute(
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val selectedTag by viewModel.selectedTag.collectAsStateWithLifecycle()
    OnboardingScreen(
        options = viewModel.options,
        selectedTag = selectedTag,
        onSelect = viewModel::select,
        onContinue = { viewModel.confirm(onDone) },
        modifier = modifier,
    )
}

@Composable
fun OnboardingScreen(
    options: List<SupportedLanguage>,
    selectedTag: String,
    onSelect: (String) -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize().padding(20.dp)) {
        Text(text = stringResource(I18nR.string.onboarding_choose_language_title), style = MaterialTheme.typography.headlineMedium)
        Text(
            text = stringResource(I18nR.string.onboarding_choose_language_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp, bottom = 16.dp),
        )

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            items(options, key = { it.bcp47Tag }) { language ->
                val isSelected = language.bcp47Tag == selectedTag
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(selected = isSelected, onClick = { onSelect(language.bcp47Tag) }),
                ) {
                    androidx.compose.foundation.layout.Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = isSelected, onClick = { onSelect(language.bcp47Tag) })
                        Text(text = language.nativeName, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }

        Button(onClick = onContinue, modifier = Modifier.fillMaxWidth()) {
            Text(text = stringResource(I18nR.string.onboarding_continue_cta))
        }
    }
}
