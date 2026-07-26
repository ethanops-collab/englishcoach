package com.englishcoach.app.feature.lesson.runtime

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.englishcoach.app.core.designsystem.theme.Dimens
import com.englishcoach.app.core.i18n.R as I18nR
import java.io.File

/**
 * Lets the user pick a static avatar photo and/or a name for the current lesson's role-play
 * partner. Cosmetic only - never affects the coach persona or conversation content.
 */
@Composable
fun CharacterCustomizeSheet(
    avatarImagePath: String?,
    displayName: String?,
    onChoosePhoto: () -> Unit,
    onRemovePhoto: () -> Unit,
    onNameChanged: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(I18nR.string.character_customize_title)) },
        text = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AvatarPreview(avatarImagePath)
                    Column(modifier = Modifier.padding(start = Dimens.SpaceM)) {
                        TextButton(onClick = onChoosePhoto) {
                            Text(text = stringResource(I18nR.string.character_choose_photo_cta))
                        }
                        if (avatarImagePath != null) {
                            TextButton(onClick = onRemovePhoto) {
                                Text(text = stringResource(I18nR.string.character_remove_photo_cta))
                            }
                        }
                    }
                }
                OutlinedTextField(
                    value = displayName.orEmpty(),
                    onValueChange = onNameChanged,
                    label = { Text(text = stringResource(I18nR.string.character_name_label)) },
                    singleLine = true,
                    modifier = Modifier.padding(top = Dimens.SpaceM),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(I18nR.string.character_done_cta))
            }
        },
    )
}

@Composable
private fun AvatarPreview(avatarImagePath: String?) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.size(64.dp),
    ) {
        if (avatarImagePath != null) {
            AsyncImage(
                model = File(avatarImagePath),
                contentDescription = null,
                modifier = Modifier.size(64.dp).clip(CircleShape),
            )
        } else {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(64.dp),
            )
        }
    }
}
