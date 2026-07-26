package com.englishcoach.app.feature.lesson.runtime

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.englishcoach.app.core.i18n.LanguagePreferencesRepository
import com.englishcoach.app.core.model.CharacterPreference
import com.englishcoach.app.core.model.LessonAttempt
import com.englishcoach.app.domain.engine.CompleteLessonUseCase
import com.englishcoach.app.domain.engine.ConversationSessionManager
import com.englishcoach.app.domain.engine.SessionPhase
import com.englishcoach.app.domain.engine.SessionUiState
import com.englishcoach.app.domain.repository.CharacterPreferenceRepository
import com.englishcoach.app.domain.repository.LessonRepository
import com.englishcoach.app.feature.lesson.audio.AudioPlayback
import com.englishcoach.app.feature.lesson.audio.MicAudioCapture
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class LessonViewModel @Inject constructor(
    private val sessionManager: ConversationSessionManager,
    private val completeLessonUseCase: CompleteLessonUseCase,
    private val lessonRepository: LessonRepository,
    private val languagePreferencesRepository: LanguagePreferencesRepository,
    private val characterPreferenceRepository: CharacterPreferenceRepository,
    private val micAudioCapture: MicAudioCapture,
    private val audioPlayback: AudioPlayback,
) : ViewModel() {

    val sessionState: StateFlow<SessionUiState> = sessionManager.uiState

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording

    val characterPreference: StateFlow<CharacterPreference?> = sessionState
        .map { it.lesson?.type }
        .filterNotNull()
        .distinctUntilChanged()
        .flatMapLatest { lessonType -> characterPreferenceRepository.observe(lessonType) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private var sessionStartMs = 0L
    private var started = false

    init {
        viewModelScope.launch {
            sessionManager.playbackEvents.collect { audio -> audioPlayback.play(audio) }
        }
    }

    fun start(lessonId: String) {
        if (started) return
        started = true
        viewModelScope.launch {
            val lesson = lessonRepository.getLesson(lessonId) ?: return@launch
            val nativeTag = languagePreferencesRepository.chosenLanguageTag.first() ?: "en"
            sessionStartMs = System.currentTimeMillis()
            sessionManager.start(lesson, nativeTag)
        }
    }

    fun onMicPressed() {
        if (_isRecording.value) return
        _isRecording.value = true
        micAudioCapture.start(viewModelScope)
    }

    fun onMicReleased() {
        if (!_isRecording.value) return
        _isRecording.value = false
        viewModelScope.launch {
            val pcm = micAudioCapture.stopAndGetPcm16()
            if (sessionState.value.phase == SessionPhase.CORRECTING) {
                sessionManager.acknowledgeCorrectionAndContinue(pcm)
            } else {
                sessionManager.onUserAudioCaptured(pcm)
            }
        }
    }

    fun onAvatarPicked(avatarImagePath: String) {
        val lessonType = sessionState.value.lesson?.type ?: return
        viewModelScope.launch {
            characterPreferenceRepository.save(
                lessonType = lessonType,
                avatarImagePath = avatarImagePath,
                displayName = characterPreference.value?.displayName,
            )
        }
    }

    fun onAvatarCleared() {
        val lessonType = sessionState.value.lesson?.type ?: return
        viewModelScope.launch { characterPreferenceRepository.clear(lessonType) }
    }

    fun onNameChanged(name: String?) {
        val lessonType = sessionState.value.lesson?.type ?: return
        viewModelScope.launch {
            characterPreferenceRepository.save(
                lessonType = lessonType,
                avatarImagePath = characterPreference.value?.avatarImagePath,
                displayName = name,
            )
        }
    }

    suspend fun finishLesson(): LessonAttempt {
        sessionManager.finish()
        val speakingSeconds = ((System.currentTimeMillis() - sessionStartMs) / 1_000L).toInt()
        return completeLessonUseCase(sessionState.value, speakingSeconds)
    }
}
