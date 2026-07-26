package com.englishcoach.app.domain.engine

import com.englishcoach.app.core.model.ConversationTurn
import com.englishcoach.app.core.model.Lesson
import com.englishcoach.app.core.model.MistakeRecord
import com.englishcoach.app.core.model.MistakeType
import com.englishcoach.app.core.model.PronunciationScore
import com.englishcoach.app.core.model.Speaker
import com.englishcoach.app.domain.prompt.CoachPromptTemplates
import com.englishcoach.app.domain.repository.MistakeRepository
import com.englishcoach.app.engine.llm.LlmCoach
import com.englishcoach.app.engine.llm.LlmMessage
import com.englishcoach.app.engine.llm.LlmRequest
import com.englishcoach.app.engine.llm.LlmRole
import com.englishcoach.app.engine.pronunciation.PronunciationScorer
import com.englishcoach.app.engine.pronunciation.RecognizedWord
import com.englishcoach.app.engine.speech.SpeechRecognizer
import com.englishcoach.app.engine.tts.SynthesizedAudio
import com.englishcoach.app.engine.tts.TextToSpeechEngine
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID
import javax.inject.Inject

/**
 * "Listening" (mic actively capturing) is deliberately not a phase here - it's local UI
 * state ([com.englishcoach.app.feature.lesson.runtime.LessonViewModel]'s `isRecording`)
 * because this manager only reacts once a full utterance has been captured.
 */
enum class SessionPhase { IDLE, TRANSCRIBING, ANALYZING, CORRECTING, SPEAKING, COMPLETE }

data class SessionUiState(
    val lesson: Lesson? = null,
    val phase: SessionPhase = SessionPhase.IDLE,
    val turns: List<ConversationTurn> = emptyList(),
    val lastPronunciationScore: PronunciationScore? = null,
)

/**
 * Drives the CLAUDE.md Learning Flow state machine end to end:
 * Idle -> Listening -> Transcribing -> Analyzing -> Correcting -> Speaking -> Idle, calling
 * SpeechRecognizer -> (LlmCoach + GrammarCorrectionEngine) -> PronunciationScorer ->
 * TextToSpeechEngine in sequence. Actual mic capture (AudioRecord) and playback (AudioTrack)
 * are Android-specific and live in :feature:lesson; this class only orchestrates the engines.
 */
class ConversationSessionManager @Inject constructor(
    private val speechRecognizer: SpeechRecognizer,
    private val llmCoach: LlmCoach,
    private val ttsEngine: TextToSpeechEngine,
    private val pronunciationScorer: PronunciationScorer,
    private val grammarCorrectionEngine: GrammarCorrectionEngine,
    private val mistakeRepository: MistakeRepository,
) {
    private val _uiState = MutableStateFlow(SessionUiState())
    val uiState: StateFlow<SessionUiState> = _uiState

    private val _playbackEvents = MutableSharedFlow<SynthesizedAudio>(extraBufferCapacity = 1)
    val playbackEvents: SharedFlow<SynthesizedAudio> = _playbackEvents

    private var nativeLanguageTag: String = "en"
    private var pendingCorrection: MistakeRecord? = null

    suspend fun start(lesson: Lesson, nativeLanguageTag: String) {
        this.nativeLanguageTag = nativeLanguageTag
        _uiState.value = SessionUiState(lesson = lesson)

        val opening = llmCoach.complete(
            LlmRequest(
                systemPrompt = CoachPromptTemplates.personaSystemPrompt(lesson.type, lesson.missionKey),
                messages = listOf(
                    LlmMessage(
                        LlmRole.USER,
                        CoachPromptTemplates.demonstrationPrompt(lesson.type, lesson.targetPhraseCount),
                    ),
                ),
            ),
        )
        speak(opening.text)
    }

    /** Called by the UI once it has captured and stopped recording one user turn. */
    suspend fun onUserAudioCaptured(audioPcm16: ShortArray) {
        val lesson = _uiState.value.lesson ?: return

        _uiState.update { it.copy(phase = SessionPhase.TRANSCRIBING) }
        val transcription = speechRecognizer.transcribe(audioPcm16)
        addTurn(Speaker.USER, transcription.text)

        _uiState.update { it.copy(phase = SessionPhase.ANALYZING) }
        val llmResponse = llmCoach.complete(
            LlmRequest(
                systemPrompt = CoachPromptTemplates.personaSystemPrompt(lesson.type, lesson.missionKey) +
                    "\n" + CoachPromptTemplates.correctionContract,
                messages = listOf(LlmMessage(LlmRole.USER, transcription.text)),
            ),
        )
        val parsed = grammarCorrectionEngine.parse(transcription.text, llmResponse.text)

        val referenceText = parsed.correction?.correctedText ?: transcription.text
        val pronunciation = pronunciationScorer.score(
            referenceText = referenceText,
            recognizedWords = transcription.words.map { RecognizedWord(it.word, it.startMs, it.endMs, it.confidence) },
            nativeLanguageTag = nativeLanguageTag,
        )
        _uiState.update { it.copy(lastPronunciationScore = pronunciation) }

        if (parsed.correction != null) {
            pendingCorrection = MistakeRecord(
                id = UUID.randomUUID().toString(),
                type = MistakeType.GRAMMAR,
                lessonId = lesson.id,
                originalText = parsed.correction.originalText,
                correctedText = parsed.correction.correctedText,
                explanation = parsed.correction.explanation,
                createdAtEpochMs = System.currentTimeMillis(),
                nextReviewAtEpochMs = ReviewScheduler.nextReviewAtEpochMs(System.currentTimeMillis(), 1),
                intervalDays = 1,
                timesReviewed = 0,
            )
            mistakeRepository.recordMistake(pendingCorrection!!)
            addTurn(Speaker.COACH, parsed.correction.correctedText, correction = parsed.correction)
            _uiState.update { it.copy(phase = SessionPhase.CORRECTING) }
        } else {
            speak(parsed.coachReply)
        }
    }

    /** UI calls this after the user re-records the corrected sentence (step 3-4 of Correction Style). */
    suspend fun acknowledgeCorrectionAndContinue(repeatedAudioPcm16: ShortArray) {
        val lesson = _uiState.value.lesson ?: return
        _uiState.update { it.copy(phase = SessionPhase.TRANSCRIBING) }
        val repeated = speechRecognizer.transcribe(repeatedAudioPcm16)
        val correctedReference = pendingCorrection?.correctedText ?: repeated.text
        val pronunciation = pronunciationScorer.score(
            referenceText = correctedReference,
            recognizedWords = repeated.words.map { RecognizedWord(it.word, it.startMs, it.endMs, it.confidence) },
            nativeLanguageTag = nativeLanguageTag,
        )
        _uiState.update { it.copy(lastPronunciationScore = pronunciation) }
        pendingCorrection = null

        val continuation = llmCoach.complete(
            LlmRequest(
                systemPrompt = CoachPromptTemplates.personaSystemPrompt(lesson.type, lesson.missionKey),
                messages = listOf(LlmMessage(LlmRole.USER, correctedReference)),
            ),
        )
        speak(continuation.text)
    }

    fun finish() {
        _uiState.update { it.copy(phase = SessionPhase.COMPLETE) }
    }

    private suspend fun speak(text: String) {
        _uiState.update { it.copy(phase = SessionPhase.SPEAKING) }
        val audio = ttsEngine.synthesize(text)
        addTurn(Speaker.COACH, text)
        _playbackEvents.tryEmit(audio)
        _uiState.update { it.copy(phase = SessionPhase.IDLE) }
    }

    private fun addTurn(speaker: Speaker, text: String, correction: com.englishcoach.app.core.model.GrammarCorrection? = null) {
        val turn = ConversationTurn(
            id = UUID.randomUUID().toString(),
            speaker = speaker,
            text = text,
            timestampMs = System.currentTimeMillis(),
            correction = correction,
        )
        _uiState.update { it.copy(turns = it.turns + turn) }
    }
}
