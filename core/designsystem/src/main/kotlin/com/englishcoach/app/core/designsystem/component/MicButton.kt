package com.englishcoach.app.core.designsystem.component

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.englishcoach.app.core.designsystem.theme.EnglishCoachTheme

enum class MicState { IDLE, LISTENING, PROCESSING }

@Composable
fun MicButton(
    state: MicState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "mic-pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (state == MicState.LISTENING) 1.18f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 700),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "mic-pulse-scale",
    )

    // LISTENING and PROCESSING are normal coaching states, not errors - `error` stays
    // reserved for genuine failures (e.g. a model failing to load).
    val containerColor = when (state) {
        MicState.IDLE -> MaterialTheme.colorScheme.primary
        MicState.LISTENING -> MaterialTheme.colorScheme.tertiary
        MicState.PROCESSING -> MaterialTheme.colorScheme.secondary
    }
    val iconTint = when (state) {
        MicState.IDLE -> MaterialTheme.colorScheme.onPrimary
        MicState.LISTENING -> MaterialTheme.colorScheme.onTertiary
        MicState.PROCESSING -> MaterialTheme.colorScheme.onSecondary
    }

    Box(
        modifier = modifier
            .size(72.dp)
            .scale(if (state == MicState.LISTENING) pulseScale else 1f)
            .background(color = containerColor, shape = CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        IconButton(onClick = onClick) {
            Icon(
                imageVector = Icons.Filled.Mic,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(32.dp),
            )
        }
    }
}

@Preview
@Composable
private fun MicButtonPreview() {
    EnglishCoachTheme {
        MicButton(state = MicState.LISTENING, onClick = {})
    }
}
