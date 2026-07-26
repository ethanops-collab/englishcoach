package com.englishcoach.app.core.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.englishcoach.app.core.designsystem.theme.Dimens
import com.englishcoach.app.core.designsystem.theme.EnglishCoachTheme

/**
 * The always-present "what should I do right now" card. This is the anti-blank-screen
 * primitive: Home always shows one of these instead of an open-ended prompt.
 */
@Composable
fun MissionCard(
    title: String,
    subtitle: String,
    ctaLabel: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
    ) {
        Column(modifier = Modifier.padding(Dimens.CardPadding)) {
            Text(text = title, style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(Dimens.SpaceXS))
            Text(text = subtitle, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(Dimens.SpaceL))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(onClick = onClick) {
                    Icon(imageVector = Icons.Filled.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = ctaLabel)
                }
            }
        }
    }
}

@Preview
@Composable
private fun MissionCardPreview() {
    EnglishCoachTheme {
        MissionCard(
            title = "Today's Mission",
            subtitle = "Practice ordering coffee at a cafe",
            ctaLabel = "Start",
            onClick = {},
        )
    }
}
