package com.englishcoach.app.di

import com.englishcoach.app.engine.llama.LlamaLlmCoach
import com.englishcoach.app.engine.llm.LlmCoach
import com.englishcoach.app.engine.pronunciation.PronunciationScorer
import com.englishcoach.app.engine.pronunciation.WhisperConfidencePronunciationScorer
import com.englishcoach.app.engine.speech.SpeechRecognizer
import com.englishcoach.app.engine.systemtts.SystemTextToSpeechEngine
import com.englishcoach.app.engine.tts.TextToSpeechEngine
import com.englishcoach.app.engine.whisper.WhisperSpeechRecognizer
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * The single seam between the domain-level engine interfaces and their implementations.
 * All four engines are now bound to real implementations: whisper.cpp (STT), llama.cpp
 * (LLM), Android system TTS, and a whisper-confidence-driven pronunciation scorer. Swapping
 * any of those in later is a one-line change per binding here, nowhere else.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class EngineBindingsModule {

    @Binds
    @Singleton
    abstract fun bindSpeechRecognizer(impl: WhisperSpeechRecognizer): SpeechRecognizer

    @Binds
    @Singleton
    abstract fun bindLlmCoach(impl: LlamaLlmCoach): LlmCoach

    @Binds
    @Singleton
    abstract fun bindTextToSpeechEngine(impl: SystemTextToSpeechEngine): TextToSpeechEngine

    @Binds
    @Singleton
    abstract fun bindPronunciationScorer(impl: WhisperConfidencePronunciationScorer): PronunciationScorer
}
