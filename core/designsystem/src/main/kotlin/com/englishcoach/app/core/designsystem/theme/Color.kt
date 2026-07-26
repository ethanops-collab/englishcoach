package com.englishcoach.app.core.designsystem.theme

import androidx.compose.ui.graphics.Color

// Coach-brand palette: deliberately distinct from generic chat-app blues/greens so the app
// never reads as "just another chatbot". Each brand hue carries a light/dark "container" pair
// (90/30 tones) so every M3 container role (primaryContainer, secondaryContainer, ...) stays
// on-brand instead of falling back to Material's default baseline purple.
val CoachTeal10 = Color(0xFF00201C)
val CoachTeal30 = Color(0xFF00504A)
val CoachTeal40 = Color(0xFF00695C)
val CoachTeal80 = Color(0xFF4DB6AC)
val CoachTeal90 = Color(0xFFB2DFDB)

val CoachAmber10 = Color(0xFF2A1800)
val CoachAmber30 = Color(0xFF7A4A00)
val CoachAmber40 = Color(0xFFB26A00)
val CoachAmber80 = Color(0xFFFFC46B)
val CoachAmber90 = Color(0xFFFFDFB0)

val CoachCoral10 = Color(0xFF3A0A00)
val CoachCoral30 = Color(0xFF8A2E0A)
val CoachCoral40 = Color(0xFFC2410C)
val CoachCoral80 = Color(0xFFFFAB80)
val CoachCoral90 = Color(0xFFFFDBC7)

// Reserved for genuine error states only (e.g. a failed model load) - never used for
// corrections or the mic's "listening" state, both of which are normal coaching flow.
val Error40 = Color(0xFFBA1A1A)
val Error80 = Color(0xFFFFB4AB)
val OnErrorLight = Color(0xFFFFFFFF)
val OnErrorDark = Color(0xFF690005)
val ErrorContainerLight = Color(0xFFFFDAD4)
val OnErrorContainerLight = Color(0xFF410002)
val ErrorContainerDark = Color(0xFF93000A)
val OnErrorContainerDark = Color(0xFFFFDAD4)

// Warm neutral surface tones - replaces Material's default cool-gray surfaceVariant with a
// cozy sand/taupe tone that matches the brand hues above.
val SurfaceVariantWarmLight = Color(0xFFEFE3D6)
val OnSurfaceVariantWarmLight = Color(0xFF4E4438)
val SurfaceVariantWarmDark = Color(0xFF3B342C)
val OnSurfaceVariantWarmDark = Color(0xFFD8CBBC)

val Neutral10 = Color(0xFF1A1C1B)
val Neutral20 = Color(0xFF2F312F)
val Neutral95 = Color(0xFFF3F5F2)
val Neutral99 = Color(0xFFFBFDF9)
