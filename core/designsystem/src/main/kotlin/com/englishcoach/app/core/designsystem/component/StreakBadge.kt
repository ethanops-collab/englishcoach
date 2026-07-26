package com.englishcoach.app.core.designsystem.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.englishcoach.app.core.designsystem.theme.Dimens
import com.englishcoach.app.core.designsystem.theme.EnglishCoachTheme

@Composable
fun StreakBadge(streakDays: Int, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.secondaryContainer,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Dimens.SpaceM, vertical = Dimens.SpaceS),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.LocalFireDepartment,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            Text(
                text = "$streakDays",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(start = Dimens.SpaceXS),
            )
        }
    }
}

@Preview
@Composable
private fun StreakBadgePreview() {
    EnglishCoachTheme {
        StreakBadge(streakDays = 12)
    }
}
