package com.englishcoach.app.engine.llm

import com.englishcoach.app.core.common.AppResult
import com.englishcoach.app.core.common.ModelState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * Deterministic stand-in for the on-device Gemma/Llama GGUF model. Recognizes the exact
 * scripted mistakes produced by [com.englishcoach.app.engine.speech.FakeSpeechRecognizer]
 * so the whole pipeline demoes coherently without a real model loaded. Response format is
 * the same fixed `CORRECTED:` / `EXPLANATION:` / `REPLY:` (or `NO_ERROR:`) contract that
 * :domain's GrammarCorrectionEngine parses from a real model's output.
 */
class FakeLlmCoach @Inject constructor() : LlmCoach {

    private val _state = MutableStateFlow(ModelState.UNLOADED)
    override val state: StateFlow<ModelState> = _state

    private val scriptedCorrections = mapOf(
        "I goed to school yesterday." to
            "CORRECTED: I went to school yesterday.\n" +
                "EXPLANATION: Use \"went\" because \"go\" is irregular.\n" +
                "REPLY: Nice, tell me more about your day at school.",
        "I want to book a room for two night." to
            "CORRECTED: I want to book a room for two nights.\n" +
                "EXPLANATION: Use the plural \"nights\" after a number greater than one.\n" +
                "REPLY: Sure, what dates would you like?",
        "She don't like spicy food." to
            "CORRECTED: She doesn't like spicy food.\n" +
                "EXPLANATION: Use \"doesn't\" with \"she\", \"he\", and \"it\".\n" +
                "REPLY: Got it, what does she like to eat instead?",
    )

    override suspend fun load(modelPath: String): AppResult<Unit> {
        _state.value = ModelState.LOADING
        delay(150)
        _state.value = ModelState.READY
        return AppResult.Success(Unit)
    }

    override fun unload() {
        _state.value = ModelState.UNLOADED
    }

    override suspend fun complete(request: LlmRequest): LlmResponse {
        delay(400)
        val lastUserMessage = request.messages.lastOrNull { it.role == LlmRole.USER }?.content.orEmpty()
        val text = scriptedCorrections[lastUserMessage]
            ?: "NO_ERROR: true\nREPLY: Great, let's keep going. Can I have a coffee, please?"
        return LlmResponse(text = text, tokensGenerated = text.length / 4)
    }
}
