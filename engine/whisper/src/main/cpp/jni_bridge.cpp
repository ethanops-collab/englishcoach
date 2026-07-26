#include <jni.h>
#include <android/log.h>
#include <string>
#include <vector>

#include "whisper.h"

#define LOG_TAG "WhisperJNI"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

extern "C" JNIEXPORT jlong JNICALL
Java_com_englishcoach_app_engine_whisper_WhisperNative_nativeInit(
        JNIEnv *env, jobject /*thiz*/, jstring modelPath) {
    const char *path = env->GetStringUTFChars(modelPath, nullptr);

    struct whisper_context_params cparams = whisper_context_default_params();
    cparams.use_gpu = false;

    struct whisper_context *ctx = whisper_init_from_file_with_params(path, cparams);

    env->ReleaseStringUTFChars(modelPath, path);

    if (ctx == nullptr) {
        LOGE("whisper_init_from_file_with_params failed");
        return 0;
    }
    return reinterpret_cast<jlong>(ctx);
}

namespace {

// Non-printable ASCII separators - safe to use since they never appear in transcribed text.
constexpr char FIELD_SEP = '\x1F';  // between word / startMs / endMs / confidence
constexpr char WORD_SEP = '\x1E';   // between words
constexpr char SECTION_SEP = '\x1D'; // between the full text and the word-timing block

// Whisper's BPE-like tokenizer marks the start of a new word with a leading space in the
// token's text (continuation pieces of the same word have no leading space). Special/control
// tokens (e.g. "[_BEG_]") are rendered wrapped in brackets and are skipped entirely.
std::string encode_words(struct whisper_context *ctx) {
    std::string out;
    const int n_segments = whisper_full_n_segments(ctx);

    for (int s = 0; s < n_segments; ++s) {
        const int n_tokens = whisper_full_n_tokens(ctx, s);

        std::string currentWord;
        int64_t wordStartCs = -1;
        int64_t wordEndCs = -1;
        float probSum = 0.0f;
        int probCount = 0;

        auto flush = [&]() {
            if (!currentWord.empty()) {
                if (!out.empty()) out += WORD_SEP;
                const float avgProb = probCount > 0 ? probSum / static_cast<float>(probCount) : 0.0f;
                out += currentWord;
                out += FIELD_SEP;
                out += std::to_string(wordStartCs * 10); // centiseconds -> ms
                out += FIELD_SEP;
                out += std::to_string(wordEndCs * 10);
                out += FIELD_SEP;
                out += std::to_string(avgProb);
            }
            currentWord.clear();
            wordStartCs = -1;
            wordEndCs = -1;
            probSum = 0.0f;
            probCount = 0;
        };

        for (int t = 0; t < n_tokens; ++t) {
            const char *tokenText = whisper_full_get_token_text(ctx, s, t);
            if (tokenText == nullptr || tokenText[0] == '\0') continue;
            if (tokenText[0] == '[') continue; // special/control token, e.g. "[_BEG_]"

            std::string piece(tokenText);
            const bool startsNewWord = piece[0] == ' ';
            if (startsNewWord && !currentWord.empty()) {
                flush();
            }

            const whisper_token_data td = whisper_full_get_token_data(ctx, s, t);
            if (wordStartCs < 0) wordStartCs = td.t0;
            wordEndCs = td.t1;
            probSum += td.p;
            probCount++;

            // Trim a single leading space when starting a new word accumulation.
            currentWord += startsNewWord ? piece.substr(1) : piece;
        }
        flush();
    }
    return out;
}

} // namespace

extern "C" JNIEXPORT jstring JNICALL
Java_com_englishcoach_app_engine_whisper_WhisperNative_nativeTranscribe(
        JNIEnv *env, jobject /*thiz*/, jlong ctxPtr, jfloatArray samples, jint nThreads) {
    auto *ctx = reinterpret_cast<struct whisper_context *>(ctxPtr);
    if (ctx == nullptr) {
        return env->NewStringUTF("");
    }

    jsize n = env->GetArrayLength(samples);
    std::vector<float> pcmf32(n);
    env->GetFloatArrayRegion(samples, 0, n, pcmf32.data());

    struct whisper_full_params wparams = whisper_full_default_params(WHISPER_SAMPLING_GREEDY);
    wparams.n_threads        = nThreads > 0 ? nThreads : 4;
    wparams.language         = "en";
    wparams.translate        = false;
    wparams.no_context       = true;
    wparams.single_segment   = false;
    wparams.token_timestamps = true;
    wparams.print_progress   = false;
    wparams.print_realtime   = false;
    wparams.print_special    = false;
    wparams.print_timestamps = false;

    int result = whisper_full(ctx, wparams, pcmf32.data(), n);
    if (result != 0) {
        LOGE("whisper_full failed with code %d", result);
        return env->NewStringUTF("");
    }

    std::string text;
    const int n_segments = whisper_full_n_segments(ctx);
    for (int i = 0; i < n_segments; ++i) {
        const char *segment = whisper_full_get_segment_text(ctx, i);
        if (segment != nullptr) {
            text += segment;
        }
    }

    std::string combined = text;
    combined += SECTION_SEP;
    combined += encode_words(ctx);

    return env->NewStringUTF(combined.c_str());
}

extern "C" JNIEXPORT void JNICALL
Java_com_englishcoach_app_engine_whisper_WhisperNative_nativeRelease(
        JNIEnv * /*env*/, jobject /*thiz*/, jlong ctxPtr) {
    auto *ctx = reinterpret_cast<struct whisper_context *>(ctxPtr);
    if (ctx != nullptr) {
        whisper_free(ctx);
    }
}
