package com.englishcoach.app.feature.lesson.runtime

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.englishcoach.app.core.designsystem.component.CoachCard
import com.englishcoach.app.core.designsystem.component.CorrectionCard
import com.englishcoach.app.core.designsystem.component.MicButton
import com.englishcoach.app.core.designsystem.component.MicState
import com.englishcoach.app.core.designsystem.component.ScoreRing
import com.englishcoach.app.core.designsystem.theme.Dimens
import com.englishcoach.app.core.i18n.LessonCopy
import com.englishcoach.app.core.i18n.R as I18nR
import com.englishcoach.app.core.model.CharacterPreference
import com.englishcoach.app.core.model.ConversationTurn
import com.englishcoach.app.core.model.LessonAttempt
import com.englishcoach.app.core.model.Speaker
import com.englishcoach.app.domain.engine.SessionPhase
import com.englishcoach.app.domain.engine.SessionUiState
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun LessonRuntimeRoute(
    lessonId: String,
    onFinished: (LessonAttempt) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LessonViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var hasMicPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED,
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasMicPermission = granted
    }
    val avatarImageStore = remember(context) { AvatarImageStore(context) }
    var showCharacterSheet by remember { mutableStateOf(false) }
    val photoPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        val lessonType = viewModel.sessionState.value.lesson?.type
        if (uri != null && lessonType != null) {
            coroutineScope.launch {
                val path = avatarImageStore.persist(uri, lessonType)
                viewModel.onAvatarPicked(path)
            }
        }
    }

    LaunchedEffect(lessonId) { viewModel.start(lessonId) }

    val sessionState by viewModel.sessionState.collectAsStateWithLifecycle()
    val isRecording by viewModel.isRecording.collectAsStateWithLifecycle()
    val characterPreference by viewModel.characterPreference.collectAsStateWithLifecycle()

    LessonRuntimeScreen(
        sessionState = sessionState,
        isRecording = isRecording,
        hasMicPermission = hasMicPermission,
        characterPreference = characterPreference,
        onRequestMicPermission = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
        onMicPressed = viewModel::onMicPressed,
        onMicReleased = viewModel::onMicReleased,
        onOpenCharacterCustomize = { showCharacterSheet = true },
        onFinish = {
            coroutineScope.launch { onFinished(viewModel.finishLesson()) }
        },
        modifier = modifier,
    )

    if (showCharacterSheet) {
        CharacterCustomizeSheet(
            avatarImagePath = characterPreference?.avatarImagePath,
            displayName = characterPreference?.displayName,
            onChoosePhoto = {
                photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            },
            onRemovePhoto = viewModel::onAvatarCleared,
            onNameChanged = viewModel::onNameChanged,
            onDismiss = { showCharacterSheet = false },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonRuntimeScreen(
    sessionState: SessionUiState,
    isRecording: Boolean,
    hasMicPermission: Boolean,
    characterPreference: CharacterPreference?,
    onRequestMicPermission: () -> Unit,
    onMicPressed: () -> Unit,
    onMicReleased: () -> Unit,
    onOpenCharacterCustomize: () -> Unit,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = sessionState.lesson?.let { stringResource(LessonCopy.titleRes(it.type)) }
                            ?: stringResource(I18nR.string.home_today_mission_title),
                    )
                },
                actions = {
                    IconButton(onClick = onOpenCharacterCustomize) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = stringResource(I18nR.string.character_customize_content_description),
                        )
                    }
                    Button(onClick = onFinish) { Text(text = stringResource(I18nR.string.lesson_finish_cta)) }
                },
            )
        },
    ) { padding ->
        if (sessionState.phase == SessionPhase.ERROR) {
            CoachUnavailable(
                message = sessionState.errorMessage,
                modifier = Modifier.fillMaxSize().padding(padding),
            )
            return@Scaffold
        }
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(Dimens.ScreenPadding),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceM),
            ) {
                items(sessionState.turns, key = { it.id }) { turn ->
                    ConversationTurnRow(turn, characterPreference)
                }
                sessionState.lastPronunciationScore?.let { score ->
                    item {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            ScoreRing(score = score.overall)
                        }
                    }
                }
            }

            Box(
                modifier = Modifier.fillMaxWidth().padding(Dimens.SpaceXL),
                contentAlignment = Alignment.Center,
            ) {
                if (!hasMicPermission) {
                    Button(onClick = onRequestMicPermission) {
                        Text(text = stringResource(I18nR.string.lesson_mic_hint_idle))
                    }
                } else {
                    val micState = when {
                        isRecording -> MicState.LISTENING
                        sessionState.phase == SessionPhase.TRANSCRIBING || sessionState.phase == SessionPhase.ANALYZING ->
                            MicState.PROCESSING
                        else -> MicState.IDLE
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        MicButton(
                            state = micState,
                            onClick = {},
                            modifier = Modifier.pointerInput(Unit) {
                                detectTapGestures(
                                    onPress = {
                                        onMicPressed()
                                        tryAwaitRelease()
                                        onMicReleased()
                                    },
                                )
                            },
                        )
                        Text(
                            text = stringResource(
                                if (isRecording) I18nR.string.lesson_mic_hint_listening else I18nR.string.lesson_mic_hint_idle,
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        }
    }
}

/** Shown instead of the conversation when an on-device engine (LLM/STT/TTS) fails to load -
 * e.g. the model file isn't present on this device yet - instead of crashing. */
@Composable
private fun CoachUnavailable(message: String?, modifier: Modifier = Modifier) {
    Box(modifier = modifier.padding(Dimens.ScreenPadding), contentAlignment = Alignment.Center) {
        CoachCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(I18nR.string.lesson_coach_unavailable_title),
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = stringResource(I18nR.string.lesson_coach_unavailable_body),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = Dimens.SpaceS),
            )
            message?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = Dimens.SpaceM),
                )
            }
        }
    }
}

@Composable
private fun ConversationTurnRow(turn: ConversationTurn, characterPreference: CharacterPreference?) {
    val correction = turn.correction
    if (correction != null) {
        CorrectionCard(
            originalText = correction.originalText,
            correctedText = correction.correctedText,
            explanation = correction.explanation,
            repeatCtaLabel = stringResource(I18nR.string.correction_repeat_cta),
            onRepeatClick = {},
        )
    } else if (turn.speaker == Speaker.COACH) {
        Row(modifier = Modifier.fillMaxWidth()) {
            CharacterAvatar(characterPreference?.avatarImagePath)
            Column(modifier = Modifier.padding(start = Dimens.SpaceS)) {
                Text(
                    text = characterPreference?.displayName?.takeIf { it.isNotBlank() }
                        ?: stringResource(I18nR.string.speaker_coach),
                    style = MaterialTheme.typography.labelLarge,
                )
                Text(text = turn.text, style = MaterialTheme.typography.bodyLarge)
            }
        }
    } else {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(text = stringResource(I18nR.string.speaker_user), style = MaterialTheme.typography.labelLarge)
            Text(text = turn.text, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

/** Small, label-sized avatar - deliberately not a large portrait, so this stays a training-app
 * detail rather than reading as a companion-chat avatar. */
@Composable
private fun CharacterAvatar(avatarImagePath: String?) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.size(36.dp),
    ) {
        if (avatarImagePath != null) {
            AsyncImage(
                model = File(avatarImagePath),
                contentDescription = null,
                modifier = Modifier.size(36.dp).clip(CircleShape),
            )
        } else {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(36.dp),
            )
        }
    }
}
