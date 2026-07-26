package com.englishcoach.app.core.i18n

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val UI_LANGUAGE_TAG_KEY = stringPreferencesKey("ui_language_bcp47_tag")

/**
 * Persists the user's explicit UI language override. `null` means "follow the device
 * language", which is the state right after first launch until the user changes it in
 * settings.
 */
class LanguagePreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    val chosenLanguageTag: Flow<String?> = dataStore.data.map { it[UI_LANGUAGE_TAG_KEY] }

    suspend fun setChosenLanguageTag(bcp47Tag: String?) {
        dataStore.edit { prefs ->
            if (bcp47Tag == null) prefs.remove(UI_LANGUAGE_TAG_KEY) else prefs[UI_LANGUAGE_TAG_KEY] = bcp47Tag
        }
    }
}
