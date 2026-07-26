package com.englishcoach.app.feature.lesson

object LessonListDestination {
    const val ROUTE = "lesson_list"
}

object LessonRuntimeDestination {
    const val LESSON_ID_ARG = "lessonId"
    const val ROUTE_PATTERN = "lesson_runtime/{$LESSON_ID_ARG}"

    fun route(lessonId: String) = "lesson_runtime/$lessonId"
}
