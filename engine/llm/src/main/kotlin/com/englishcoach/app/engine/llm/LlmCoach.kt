package com.englishcoach.app.engine.llm

import com.englishcoach.app.core.common.ModelEngine

/**
 * Thin, persona-agnostic chat-completion contract mirroring llama.cpp's completion API.
 * All coach persona / correction / role-play prompt construction and response parsing
 * live in :domain — this interface just runs a completion.
 */
interface LlmCoach : ModelEngine {
    suspend fun complete(request: LlmRequest): LlmResponse
}

data class LlmRequest(
    val systemPrompt: String,
    val messages: List<LlmMessage>,
    val maxTokens: Int = 256,
    val temperature: Float = 0.7f,
)

data class LlmMessage(val role: LlmRole, val content: String)

enum class LlmRole { USER, ASSISTANT }

data class LlmResponse(val text: String, val tokensGenerated: Int)
