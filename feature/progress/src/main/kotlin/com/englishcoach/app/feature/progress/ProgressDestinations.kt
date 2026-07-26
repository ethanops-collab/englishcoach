package com.englishcoach.app.feature.progress

object SummaryDestination {
    const val LESSON_ID_ARG = "lessonId"
    const val ROUTE_PATTERN = "summary/{$LESSON_ID_ARG}"

    fun route(lessonId: String) = "summary/$lessonId"
}

object ReviewDestination {
    const val ROUTE = "review"
}
