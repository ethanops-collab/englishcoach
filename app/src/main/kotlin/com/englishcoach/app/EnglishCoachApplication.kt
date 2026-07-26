package com.englishcoach.app

import android.app.Application
import com.englishcoach.app.core.i18n.LanguagePreferencesRepository
import com.englishcoach.app.core.i18n.LocaleController
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class EnglishCoachApplication : Application() {

    @Inject lateinit var languagePreferencesRepository: LanguagePreferencesRepository

    @Inject lateinit var localeController: LocaleController

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        // Re-apply any explicit language override the user made in a previous session.
        // If they've never overridden it (null), Android's normal resource-qualifier
        // fallback already does the right thing (device language if translated, else
        // English) with zero extra code.
        applicationScope.launch {
            val chosenTag = languagePreferencesRepository.chosenLanguageTag.first()
            if (chosenTag != null) {
                localeController.applyLocale(chosenTag)
            }
        }
    }
}
