package com.englishcoach.app.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.englishcoach.app.core.i18n.LanguagePreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * First screen shown: decides whether the user still needs the language-selection
 * onboarding step (never picked a language before) or can go straight to Home.
 */
@HiltViewModel
class StartupViewModel @Inject constructor(
    private val languagePreferencesRepository: LanguagePreferencesRepository,
) : ViewModel() {
    fun resolve(onNeedsOnboarding: () -> Unit, onReady: () -> Unit) {
        viewModelScope.launch {
            val chosenTag = languagePreferencesRepository.chosenLanguageTag.first()
            if (chosenTag == null) onNeedsOnboarding() else onReady()
        }
    }
}

@Composable
fun StartupRoute(
    onNeedsOnboarding: () -> Unit,
    onReady: () -> Unit,
    viewModel: StartupViewModel = hiltViewModel(),
) {
    LaunchedEffect(Unit) { viewModel.resolve(onNeedsOnboarding, onReady) }
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}
