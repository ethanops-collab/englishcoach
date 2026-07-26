package com.englishcoach.app.core.i18n

import androidx.annotation.StringRes
import com.englishcoach.app.core.model.LessonType

/**
 * Maps each [LessonType] to its localized title/mission string resources. Type-safe
 * alternative to looking resources up by the `lesson_title_<type>` name convention at
 * runtime - the compiler catches a missing mapping immediately.
 */
object LessonCopy {
    @StringRes
    fun titleRes(type: LessonType): Int = when (type) {
        LessonType.DAILY_CONVERSATION -> R.string.lesson_title_daily_conversation
        LessonType.RESTAURANT -> R.string.lesson_title_restaurant
        LessonType.AIRPORT -> R.string.lesson_title_airport
        LessonType.SHOPPING -> R.string.lesson_title_shopping
        LessonType.HOTEL -> R.string.lesson_title_hotel
        LessonType.JOB_INTERVIEW -> R.string.lesson_title_job_interview
        LessonType.BUSINESS_ENGLISH -> R.string.lesson_title_business_english
        LessonType.TRAVEL -> R.string.lesson_title_travel
        LessonType.DOCTOR -> R.string.lesson_title_doctor
        LessonType.PHONE_CALLS -> R.string.lesson_title_phone_calls
        LessonType.SMALL_TALK -> R.string.lesson_title_small_talk
        LessonType.EMERGENCY -> R.string.lesson_title_emergency
        LessonType.DATING -> R.string.lesson_title_dating
    }

    @StringRes
    fun missionRes(type: LessonType): Int = when (type) {
        LessonType.DAILY_CONVERSATION -> R.string.lesson_mission_daily_conversation
        LessonType.RESTAURANT -> R.string.lesson_mission_restaurant
        LessonType.AIRPORT -> R.string.lesson_mission_airport
        LessonType.SHOPPING -> R.string.lesson_mission_shopping
        LessonType.HOTEL -> R.string.lesson_mission_hotel
        LessonType.JOB_INTERVIEW -> R.string.lesson_mission_job_interview
        LessonType.BUSINESS_ENGLISH -> R.string.lesson_mission_business_english
        LessonType.TRAVEL -> R.string.lesson_mission_travel
        LessonType.DOCTOR -> R.string.lesson_mission_doctor
        LessonType.PHONE_CALLS -> R.string.lesson_mission_phone_calls
        LessonType.SMALL_TALK -> R.string.lesson_mission_small_talk
        LessonType.EMERGENCY -> R.string.lesson_mission_emergency
        LessonType.DATING -> R.string.lesson_mission_dating
    }
}
