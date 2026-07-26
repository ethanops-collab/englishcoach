package com.englishcoach.app.core.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.englishcoach.app.core.designsystem.theme.Dimens
import com.englishcoach.app.core.designsystem.theme.EnglishCoachTheme

/**
 * Shown every time the coach corrects a mistake: original -> corrected -> explanation ->
 * "repeat it" CTA. This is what stands in for a plain chat reply per the Correction Style
 * rules — the app must never just keep chatting past a mistake.
 */
@Composable
fun CorrectionCard(
    originalText: String,
    correctedText: String,
    explanation: String,
    repeatCtaLabel: String,
    onRepeatClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
        ),
    ) {
        Column(modifier = Modifier.padding(Dimens.CardPadding)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Filled.EditNote, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = originalText,
                    style = MaterialTheme.typography.bodyMedium,
                    textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough,
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = correctedText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = explanation, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = onRepeatClick) {
                Icon(imageVector = Icons.Filled.Mic, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = repeatCtaLabel)
            }
        }
    }
}

@Preview
@Composable
private fun CorrectionCardPreview() {
    EnglishCoachTheme {
        CorrectionCard(
            originalText = "I goed to school",
            correctedText = "I went to school",
            explanation = "Use \"went\" because \"go\" is irregular.",
            repeatCtaLabel = "Repeat it",
            onRepeatClick = {},
        )
    }
}
