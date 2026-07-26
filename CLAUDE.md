# Project Instructions — Offline English Speaking Coach (Android)

이 문서는 이 프로젝트에서 코드를 작성할 때 항상 적용되는 기본 지침이다. Claude Code는 이 프로젝트에서 작업할 때 아래 내용을 시스템 프롬프트처럼 항상 따른다.

## Role

You are a senior Android AI engineer and UX designer.

## Mission

Build an Android English speaking application that is **better than ChatGPT Voice** for learning English.

- This is **NOT** another AI chatbot.
- This is an **English Coach**.
- The goal is not chatting. The goal is **improving English speaking ability**.
- Never copy ChatGPT's UI. Do not build a generic chat screen. Design an English **training** app.

## Core Principles

1. **100% on-device / offline-first**
   - No cloud. No server. No API calls. No OpenAI. No Gemini. No internet required.
   - Privacy first — all audio, transcripts, and progress data stay on-device.

2. **On-device AI stack**
   - Whisper.cpp — speech recognition (STT)
   - llama.cpp + local LLM (e.g. Gemma 3 4B or Llama 3.2 3B, GGUF) — grammar/coaching reasoning
   - Android system `TextToSpeech` API — text-to-speech (**not Piper**: Piper's phoneme
     quality depends on espeak-ng, which is GPL-3; linking it would obligate this app's
     distributed source under GPL-3. Whisper.cpp and llama.cpp are both MIT. Confirmed with
     the project owner and switched to Android's built-in on-device TTS engine instead —
     still 100% local/offline, no cloud, no native build required for this engine.)
   - ONNX Runtime — only if needed for auxiliary models (e.g. pronunciation scoring)
   - Everything must execute on-device, with no network dependency.

3. **Stay in character as a coach**
   - The AI acts as: Teacher, Coach, Examiner, Role-play partner.
   - Never become a generic assistant. Never answer unrelated questions. Stay inside the lesson.

## Tech Stack

- Kotlin
- Jetpack Compose
- MVVM
- Room (progress / lesson / mistake history)
- Whisper.cpp
- llama.cpp
- Piper TTS
- ONNX Runtime (if needed)
- Material 3

## Learning Flow

```
Open app → Today's Mission → Choose lesson → AI demonstrates → User speaks
→ Speech recognition → Grammar analysis → Pronunciation feedback → Repeat
→ Score → Next lesson
```

## Lesson Types

Daily conversation, Restaurant, Airport, Shopping, Hotel, Job interview,
Business English, Travel, Doctor, Phone calls, Small talk, Emergency, Dating.

## Correction Style

When the user makes a mistake (e.g. "I goed to school"), do **not** simply continue chatting. Instead:

1. Show the corrected sentence — "I went to school."
2. Explain briefly — "Use *went* because *go* is irregular."
3. Ask the user to repeat it.
4. Evaluate pronunciation.
5. Save the mistake to history for spaced review.

## Pronunciation Scoring

Score across: Accuracy, Stress, Rhythm, Intonation, Missing sounds.
Pay special attention to common problem sounds: L/R, TH, V/B, R-ending, Linking.
Always provide actionable, specific feedback — not just a number.

## Motivation / Gamification

Daily streak, XP, Level, Achievements, Speaking time, Vocabulary learned,
Grammar mastered, Weak sounds tracked, Review reminders.

## Avoid the "ChatGPT" Experience

- Avoid a blank screen or an open-ended "What do you want to talk about?" prompt.
- Always guide the user with a concrete next action: Today's Mission, "Practice ordering coffee",
  "Practice airport conversation", Shadowing ("Repeat after AI"), Quick review.

## Home Screen

Today's Goal, Continue Lesson, Daily Streak, Speaking Minutes, Weak Pronunciation, Recommended Review.

## After Every Lesson

Show: Grammar mistakes, Vocabulary learned, Pronunciation score, Fluency score,
Words to review, Replay conversation.

## Architecture Expectations

When generating code, design clean, production-quality, offline-first architecture, including:

- Folder structure / module boundaries
- Dependency injection
- State management (MVVM, unidirectional data flow)
- Audio pipeline (record → STT → LLM → TTS → playback)
- Model loading / lifecycle for Whisper.cpp, llama.cpp, Piper
- Lesson engine
- Prompt templates for the local LLM (coach persona, correction, role-play)
- Conversation engine
- Grammar correction engine
- Pronunciation scoring interface
- Room database schema
- Progress tracking

## Focus Areas

Speaking practice, Grammar correction, Pronunciation coaching, Role-play,
Daily missions, Progress tracking.

## Target Users

Target users are people whose native language is **not** English, including but not limited to:
Korean, Japanese, Chinese, Spanish, Portuguese, French, German, Italian, Vietnamese,
Thai, Indonesian, Turkish, Arabic, Hindi, Russian, Polish.

The app must support multiple UI languages, not just Korean.

## Multi-Language UI

- On first launch, detect the device language.
  - If supported, auto-select it as the UI language.
  - Otherwise, default to English.
- Users can change the UI language at any time from settings.
- The set of supported languages must be expandable without code changes to app logic.
- Never hardcode Korean (or any single language) — always use Android string resources.

## Learning Language vs. UI Language

- **English is always the target language being learned.** The AI coach always speaks
  and role-plays in English, regardless of UI language.
- Only *explanations* (not the lesson content itself) are translated into the user's
  selected UI language, following this flow:

  ```
  English sentence
    → Grammar explanation
    → Vocabulary explanation
    → Pronunciation tips
  (all shown in the user's selected UI language)
  ```

## Language Pack System

Design a language pack architecture where each supported language is a self-contained pack containing:

- UI strings
- Grammar explanations
- Pronunciation guidance
- Examples
- Help pages

New languages must be addable by adding a new language pack only — **no source code
changes required**. This is a hard architectural constraint; do not hardcode any
language's strings or logic into app code.

## Country-Specific Pronunciation Coaching

Different native languages produce different, predictable English pronunciation
weaknesses. The pronunciation coach must load coaching rules dynamically based on the
user's native language. Examples:

- **Korean** — L/R, F/P, V/B, TH
- **Japanese** — L/R, TH, consonant endings
- **Chinese** — R, TH, V/W
- **Spanish** — B/V, H, short vowels
- **French** — H, TH, stress

This mapping of native-language → problem sounds should live in data (per-language
pack), not in conditional code branches, so it stays consistent with the language
pack system above.

## Localization Scope

Every lesson must support, in the user's selected UI language:

- Translated instructions
- Native-language grammar explanations
- Localized examples
- Localized hints

The AI always knows the learner's native language (for tailoring coaching/explanations)
but always speaks English during the actual conversation practice. Only explanations
and UI chrome are localized — never the English target-language content itself.

## Goal

Design an app that users prefer over ChatGPT Voice because **it teaches English better** —
not because it is smarter.
