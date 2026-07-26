package com.englishcoach.app.feature.home.onboarding

import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.englishcoach.app.core.i18n.DeviceLocaleResolver
import com.englishcoach.app.core.i18n.LanguagePreferencesRepository
import com.englishcoach.app.core.i18n.LocaleController
import com.englishcoach.app.core.i18n.SupportedLanguageProvider
import com.englishcoach.app.core.model.SupportedLanguage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    supportedLanguageProvider: SupportedLanguageProvider,
    deviceLocaleResolver: DeviceLocaleResolver,
    private val languagePreferencesRepository: LanguagePreferencesRepository,
    private val localeController: LocaleController,
) : ViewModel() {

    val options: List<SupportedLanguage> = supportedLanguageProvider.selectable()

    private val _selectedTag = MutableStateFlow(
        deviceLocaleResolver.resolveInitialLanguageTag(LocaleListCompat.getAdjustedDefault()),
    )
    val selectedTag: StateFlow<String> = _selectedTag

    fun select(bcp47Tag: String) {
        _selectedTag.value = bcp47Tag
    }

    fun confirm(onDone: () -> Unit) {
        localeController.applyLocale(_selectedTag.value)
        viewModelScope.launch {
            languagePreferencesRepository.setChosenLanguageTag(_selectedTag.value)
            onDone()
        }
    }
}
