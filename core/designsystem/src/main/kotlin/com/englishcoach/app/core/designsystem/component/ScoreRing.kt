package com.englishcoach.app.core.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.englishcoach.app.core.designsystem.theme.EnglishCoachTheme

/**
 * A 0..100 score shown as a ring, used for pronunciation/fluency scores. Always paired
 * with a text label by the caller — a bare number is not actionable feedback on its own.
 * Ring color bands the score (teal = strong, amber = getting there, coral = needs work)
 * instead of a single flat color.
 */
@Composable
fun ScoreRing(
    score: Float,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 96.dp,
) {
    val progressColor = when {
        score >= 80f -> MaterialTheme.colorScheme.primary
        score >= 50f -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.tertiary
    }
    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            progress = { (score / 100f).coerceIn(0f, 1f) },
            modifier = Modifier.size(size),
            color = progressColor,
            strokeWidth = 8.dp,
            strokeCap = StrokeCap.Round,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
        Text(
            text = score.toInt().toString(),
            style = MaterialTheme.typography.titleLarge,
        )
    }
}

@Preview
@Composable
private fun ScoreRingPreview() {
    EnglishCoachTheme {
        ScoreRing(score = 82f)
    }
}
