package com.englishcoach.app.core.i18n.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.languagePreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "language_preferences",
)

@Module
@InstallIn(SingletonComponent::class)
object I18nModule {
    @Provides
    @Singleton
    fun provideLanguagePreferencesDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        context.languagePreferencesDataStore
}
