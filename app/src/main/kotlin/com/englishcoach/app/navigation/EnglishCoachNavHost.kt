package com.englishcoach.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.englishcoach.app.feature.home.HomeDestination
import com.englishcoach.app.feature.home.HomeRoute
import com.englishcoach.app.feature.home.OnboardingDestination
import com.englishcoach.app.feature.home.onboarding.OnboardingRoute
import com.englishcoach.app.feature.lesson.LessonListDestination
import com.englishcoach.app.feature.lesson.LessonRuntimeDestination
import com.englishcoach.app.feature.lesson.list.LessonListRoute
import com.englishcoach.app.feature.lesson.runtime.LessonRuntimeRoute
import com.englishcoach.app.feature.progress.ReviewDestination
import com.englishcoach.app.feature.progress.SummaryDestination
import com.englishcoach.app.feature.progress.review.ReviewRoute
import com.englishcoach.app.feature.progress.summary.SummaryRoute

private const val STARTUP_ROUTE = "startup"

@Composable
fun EnglishCoachNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = STARTUP_ROUTE) {
        composable(STARTUP_ROUTE) {
            StartupRoute(
                onNeedsOnboarding = {
                    navController.navigate(OnboardingDestination.ROUTE) {
                        popUpTo(STARTUP_ROUTE) { inclusive = true }
                    }
                },
                onReady = {
                    navController.navigate(HomeDestination.ROUTE) {
                        popUpTo(STARTUP_ROUTE) { inclusive = true }
                    }
                },
            )
        }

        composable(OnboardingDestination.ROUTE) {
            OnboardingRoute(
                onDone = {
                    navController.navigate(HomeDestination.ROUTE) {
                        popUpTo(OnboardingDestination.ROUTE) { inclusive = true }
                    }
                },
            )
        }

        composable(HomeDestination.ROUTE) {
            HomeRoute(
                onStartLesson = { lessonId -> navController.navigate(LessonRuntimeDestination.route(lessonId)) },
                onOpenLessonList = { navController.navigate(LessonListDestination.ROUTE) },
                onOpenReview = { navController.navigate(ReviewDestination.ROUTE) },
            )
        }

        composable(LessonListDestination.ROUTE) {
            LessonListRoute(
                onSelectLesson = { lessonId -> navController.navigate(LessonRuntimeDestination.route(lessonId)) },
            )
        }

        composable(
            route = LessonRuntimeDestination.ROUTE_PATTERN,
            arguments = listOf(navArgument(LessonRuntimeDestination.LESSON_ID_ARG) { type = NavType.StringType }),
        ) { backStackEntry ->
            val lessonId = backStackEntry.arguments?.getString(LessonRuntimeDestination.LESSON_ID_ARG)
            if (lessonId != null) {
                LessonRuntimeRoute(
                    lessonId = lessonId,
                    onFinished = { attempt ->
                        navController.navigate(SummaryDestination.route(attempt.lessonId)) {
                            popUpTo(HomeDestination.ROUTE)
                        }
                    },
                )
            }
        }

        composable(
            route = SummaryDestination.ROUTE_PATTERN,
            arguments = listOf(navArgument(SummaryDestination.LESSON_ID_ARG) { type = NavType.StringType }),
        ) { backStackEntry ->
            val lessonId = backStackEntry.arguments?.getString(SummaryDestination.LESSON_ID_ARG)
            if (lessonId != null) {
                SummaryRoute(
                    lessonId = lessonId,
                    onDone = { navController.popBackStack(HomeDestination.ROUTE, inclusive = false) },
                    onReplay = {
                        // Replaying full audio playback of a past conversation is a follow-up:
                        // it needs the past turns' text re-synthesized (or their audio cached).
                    },
                )
            }
        }

        composable(ReviewDestination.ROUTE) {
            ReviewRoute()
        }
    }
}
