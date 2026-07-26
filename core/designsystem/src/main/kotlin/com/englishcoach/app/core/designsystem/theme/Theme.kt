package com.englishcoach.app.core.designsystem.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColors = lightColorScheme(
    primary = CoachTeal40,
    onPrimary = Color.White,
    primaryContainer = CoachTeal90,
    onPrimaryContainer = CoachTeal10,
    secondary = CoachAmber40,
    onSecondary = Color.White,
    secondaryContainer = CoachAmber90,
    onSecondaryContainer = CoachAmber10,
    tertiary = CoachCoral40,
    onTertiary = Color.White,
    tertiaryContainer = CoachCoral90,
    onTertiaryContainer = CoachCoral10,
    error = Error40,
    onError = OnErrorLight,
    errorContainer = ErrorContainerLight,
    onErrorContainer = OnErrorContainerLight,
    background = Neutral99,
    surface = Neutral99,
    onBackground = Neutral10,
    onSurface = Neutral10,
    surfaceVariant = SurfaceVariantWarmLight,
    onSurfaceVariant = OnSurfaceVariantWarmLight,
)

private val DarkColors = darkColorScheme(
    primary = CoachTeal80,
    onPrimary = CoachTeal10,
    primaryContainer = CoachTeal30,
    onPrimaryContainer = CoachTeal90,
    secondary = CoachAmber80,
    onSecondary = CoachAmber10,
    secondaryContainer = CoachAmber30,
    onSecondaryContainer = CoachAmber90,
    tertiary = CoachCoral80,
    onTertiary = CoachCoral10,
    tertiaryContainer = CoachCoral30,
    onTertiaryContainer = CoachCoral90,
    error = Error80,
    onError = OnErrorDark,
    errorContainer = ErrorContainerDark,
    onErrorContainer = OnErrorContainerDark,
    background = Neutral10,
    surface = Neutral20,
    onBackground = Neutral95,
    onSurface = Neutral95,
    surfaceVariant = SurfaceVariantWarmDark,
    onSurfaceVariant = OnSurfaceVariantWarmDark,
)

@Composable
fun EnglishCoachTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = CoachTypography,
        shapes = CoachShapes,
        content = content,
    )
}
