package com.englishcoach.app.core.common.di

import javax.inject.Qualifier

/**
 * Qualifier for injecting [kotlinx.coroutines.CoroutineDispatcher]s. Bound in :app so this
 * module never has to depend on a DI framework directly.
 */
@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class Dispatcher(val appDispatcher: AppDispatcher)

enum class AppDispatcher { DEFAULT, IO, MAIN, MAIN_IMMEDIATE }
