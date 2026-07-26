package com.englishcoach.app.engine.llama

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.englishcoach.app.core.common.AppResult
import com.englishcoach.app.engine.llm.LlmMessage
import com.englishcoach.app.engine.llm.LlmRequest
import com.englishcoach.app.engine.llm.LlmRole
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/**
 * Real, on-device proof that llama.cpp is actually wired up, using the production model
 * (Gemma 3 4B, per CLAUDE.md's spec) rather than the small Qwen2.5 0.5B used to first verify
 * the pipeline. Checks two things: (1) the model produces coherent, non-trivial text, and (2)
 * it can actually follow the real coach correction-contract format the app depends on
 * (`CoachPromptTemplates.correctionContract` in :domain) - a much stronger signal than plain
 * coherence, since the whole grammar-correction feature depends on the model reliably
 * emitting `CORRECTED:`/`EXPLANATION:`/`REPLY:`. Requires `gemma-3-4b-it-Q4_K_M.gguf` to
 * already be pushed to this test package's internal storage - this test does not download it.
 */
@RunWith(AndroidJUnit4::class)
class LlamaLlmCoachInstrumentedTest {

    @Test
    fun completesASimplePromptWithCoherentText() = runBlocking {
        val coach = loadCoach()

        val response = coach.complete(
            LlmRequest(
                systemPrompt = "You are a friendly English speaking coach. Reply in one short sentence.",
                messages = listOf(LlmMessage(LlmRole.USER, "Say hello and ask me how my day was.")),
                maxTokens = 64,
                temperature = 0.2f,
            ),
        )
        Log.i("LlamaTest", "Generated text: \"${response.text}\"")

        assertTrue("Expected a non-empty response", response.text.isNotBlank())
        assertTrue(
            "Expected at least a few words of coherent text, got: \"${response.text}\"",
            response.text.count { it.isLetter() } > 10,
        )
    }

    @Test
    fun followsTheRealCorrectionContractFormat() = runBlocking {
        val coach = loadCoach()

        // Mirrors CoachPromptTemplates.correctionContract exactly (domain/src/.../CoachPromptTemplates.kt)
        // so this test proves the production model can actually drive the app's grammar-correction
        // feature, not just produce coherent prose.
        val systemPrompt = """
            You are an English speaking coach running a live lesson, not a general assistant.
            Scenario: restaurant.
            Mission: Practice ordering food at a restaurant.
            Check the user's last line for grammar mistakes only (ignore pronunciation - that is
            scored separately). Respond in EXACTLY one of these two formats, nothing else:

            If there is a mistake:
            CORRECTED: <the corrected sentence>
            EXPLANATION: <one short, encouraging sentence explaining the rule>
            REPLY: <your one-sentence in-character reply that continues the scenario>

            If there is no mistake:
            NO_ERROR: true
            REPLY: <your one-sentence in-character reply that continues the scenario>
        """.trimIndent()

        val response = coach.complete(
            LlmRequest(
                systemPrompt = systemPrompt,
                messages = listOf(LlmMessage(LlmRole.USER, "I goed to the restaurant yesterday.")),
                maxTokens = 128,
                temperature = 0.2f,
            ),
        )
        Log.i("LlamaTest", "Correction-contract response:\n${response.text}")

        val followsContract = response.text.contains("CORRECTED:") || response.text.contains("NO_ERROR:")
        assertTrue(
            "Expected the model to follow the CORRECTED:/NO_ERROR: contract, got: \"${response.text}\"",
            followsContract,
        )
        assertTrue(
            "Expected a REPLY: line to continue the scenario, got: \"${response.text}\"",
            response.text.contains("REPLY:"),
        )
    }

    private suspend fun loadCoach(): LlamaLlmCoach {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val modelFile = File(context.filesDir, "models/gemma-3-4b-it-Q4_K_M.gguf")

        assertTrue(
            "Model file must be pushed to ${modelFile.absolutePath} before running this test",
            modelFile.exists(),
        )

        val coach = LlamaLlmCoach(context)
        val loadResult = coach.load(modelFile.absolutePath)
        assertTrue("Model failed to load: $loadResult", loadResult is AppResult.Success)
        return coach
    }
}
