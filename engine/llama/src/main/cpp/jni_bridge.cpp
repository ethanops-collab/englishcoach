#include <jni.h>
#include <android/log.h>
#include <string>
#include <vector>

#include "llama.h"

#define LOG_TAG "LlamaJNI"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

namespace {

bool g_backend_initialized = false;

struct LlamaHandle {
    llama_model *model;
    llama_context *ctx;
};

} // namespace

extern "C" JNIEXPORT jlong JNICALL
Java_com_englishcoach_app_engine_llama_LlamaNative_nativeInit(
        JNIEnv *env, jobject /*thiz*/, jstring modelPath) {
    if (!g_backend_initialized) {
        llama_backend_init();
        g_backend_initialized = true;
    }

    const char *path = env->GetStringUTFChars(modelPath, nullptr);

    llama_model_params model_params = llama_model_default_params();
    model_params.n_gpu_layers = 0;

    llama_model *model = llama_model_load_from_file(path, model_params);
    env->ReleaseStringUTFChars(modelPath, path);

    if (model == nullptr) {
        LOGE("llama_model_load_from_file failed");
        return 0;
    }

    llama_context_params ctx_params = llama_context_default_params();
    ctx_params.n_ctx = 2048;
    ctx_params.n_batch = 512;
    ctx_params.n_ubatch = 512;
    ctx_params.n_threads = 4;
    ctx_params.n_threads_batch = 4;

    llama_context *ctx = llama_init_from_model(model, ctx_params);
    if (ctx == nullptr) {
        LOGE("llama_init_from_model failed");
        llama_model_free(model);
        return 0;
    }

    auto *handle = new LlamaHandle{model, ctx};
    return reinterpret_cast<jlong>(handle);
}

/**
 * Builds the full chat prompt fresh from systemPrompt + (roles[i], contents[i]) every call
 * (stateless - :domain's ConversationSessionManager owns conversation history and passes
 * whatever messages are needed each time), applies the model's own embedded chat template,
 * then runs a plain greedy/low-temperature sampling loop against the core llama.h API.
 */
extern "C" JNIEXPORT jstring JNICALL
Java_com_englishcoach_app_engine_llama_LlamaNative_nativeComplete(
        JNIEnv *env, jobject /*thiz*/, jlong handlePtr, jstring systemPrompt,
        jobjectArray roles, jobjectArray contents, jint maxTokens, jfloat temperature) {
    auto *handle = reinterpret_cast<LlamaHandle *>(handlePtr);
    if (handle == nullptr) {
        return env->NewStringUTF("");
    }

    llama_model *model = handle->model;
    llama_context *ctx = handle->ctx;
    const llama_vocab *vocab = llama_model_get_vocab(model);

    const char *sys = env->GetStringUTFChars(systemPrompt, nullptr);
    std::string systemContent(sys);
    env->ReleaseStringUTFChars(systemPrompt, sys);

    jsize n = env->GetArrayLength(roles);
    std::vector<std::string> roleStrings(n);
    std::vector<std::string> contentStrings(n);
    for (jsize i = 0; i < n; i++) {
        auto roleObj = (jstring) env->GetObjectArrayElement(roles, i);
        auto contentObj = (jstring) env->GetObjectArrayElement(contents, i);
        const char *r = env->GetStringUTFChars(roleObj, nullptr);
        const char *c = env->GetStringUTFChars(contentObj, nullptr);
        roleStrings[i] = r;
        contentStrings[i] = c;
        env->ReleaseStringUTFChars(roleObj, r);
        env->ReleaseStringUTFChars(contentObj, c);
        env->DeleteLocalRef(roleObj);
        env->DeleteLocalRef(contentObj);
    }

    std::vector<llama_chat_message> messages;
    messages.push_back({"system", systemContent.c_str()});
    for (jsize i = 0; i < n; i++) {
        messages.push_back({roleStrings[i].c_str(), contentStrings[i].c_str()});
    }

    const char *tmpl = llama_model_chat_template(model, /* name */ nullptr);

    std::vector<char> formatted(4096);
    int32_t needed = llama_chat_apply_template(
            tmpl, messages.data(), messages.size(), true, formatted.data(), (int32_t) formatted.size());
    if (needed > (int32_t) formatted.size()) {
        formatted.resize(needed);
        needed = llama_chat_apply_template(
                tmpl, messages.data(), messages.size(), true, formatted.data(), (int32_t) formatted.size());
    }
    if (needed < 0) {
        LOGE("llama_chat_apply_template failed");
        return env->NewStringUTF("");
    }
    std::string prompt(formatted.data(), needed);

    // Fresh completion each call - clear any KV cache state from a previous call on this context.
    llama_memory_clear(llama_get_memory(ctx), true);

    const int32_t n_prompt_tokens =
            -llama_tokenize(vocab, prompt.c_str(), (int32_t) prompt.size(), nullptr, 0, true, true);
    std::vector<llama_token> prompt_tokens(n_prompt_tokens);
    if (llama_tokenize(vocab, prompt.c_str(), (int32_t) prompt.size(), prompt_tokens.data(),
                        (int32_t) prompt_tokens.size(), true, true) < 0) {
        LOGE("failed to tokenize prompt");
        return env->NewStringUTF("");
    }

    llama_sampler *sampler = llama_sampler_chain_init(llama_sampler_chain_default_params());
    llama_sampler_chain_add(sampler, llama_sampler_init_min_p(0.05f, 1));
    llama_sampler_chain_add(sampler, llama_sampler_init_temp(temperature));
    llama_sampler_chain_add(sampler, llama_sampler_init_dist(LLAMA_DEFAULT_SEED));

    std::string response;
    llama_batch batch = llama_batch_get_one(prompt_tokens.data(), (int32_t) prompt_tokens.size());
    int32_t generated = 0;
    while (generated < maxTokens) {
        const int32_t n_ctx = (int32_t) llama_n_ctx(ctx);
        const int32_t n_ctx_used = (int32_t) llama_memory_seq_pos_max(llama_get_memory(ctx), 0) + 1;
        if (n_ctx_used + batch.n_tokens > n_ctx) {
            LOGE("context size exceeded");
            break;
        }

        if (llama_decode(ctx, batch) != 0) {
            LOGE("llama_decode failed");
            break;
        }

        llama_token new_token = llama_sampler_sample(sampler, ctx, -1);
        if (llama_vocab_is_eog(vocab, new_token)) {
            break;
        }

        char buf[256];
        int32_t len = llama_token_to_piece(vocab, new_token, buf, sizeof(buf), 0, true);
        if (len < 0) {
            break;
        }
        response.append(buf, len);

        batch = llama_batch_get_one(&new_token, 1);
        generated++;
    }

    llama_sampler_free(sampler);
    return env->NewStringUTF(response.c_str());
}

extern "C" JNIEXPORT void JNICALL
Java_com_englishcoach_app_engine_llama_LlamaNative_nativeRelease(
        JNIEnv * /*env*/, jobject /*thiz*/, jlong handlePtr) {
    auto *handle = reinterpret_cast<LlamaHandle *>(handlePtr);
    if (handle != nullptr) {
        if (handle->ctx != nullptr) {
            llama_free(handle->ctx);
        }
        if (handle->model != nullptr) {
            llama_model_free(handle->model);
        }
        delete handle;
    }
}
