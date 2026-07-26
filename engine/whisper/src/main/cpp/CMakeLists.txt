cmake_minimum_required(VERSION 3.22)

project(whisper_jni)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_POSITION_INDEPENDENT_CODE ON)

set(WHISPER_CPP_DIR ${CMAKE_CURRENT_SOURCE_DIR}/thirdparty/whisper_cpp)

# ggml itself decides what to build (CPU-only here; all GPU/accelerator
# backends are forced OFF via the cmake.arguments in build.gradle.kts).
add_subdirectory(${WHISPER_CPP_DIR}/ggml ${CMAKE_CURRENT_BINARY_DIR}/ggml)

add_library(whisper STATIC
    ${WHISPER_CPP_DIR}/src/whisper.cpp
)
target_include_directories(whisper PUBLIC
    ${WHISPER_CPP_DIR}/include
    ${WHISPER_CPP_DIR}/src
)
target_compile_features(whisper PUBLIC cxx_std_11)
target_compile_definitions(whisper PRIVATE WHISPER_VERSION="1.9.1")
target_link_libraries(whisper PUBLIC ggml)

find_library(LOG_LIB log)
find_library(ANDROID_LIB android)

add_library(whisper_jni SHARED
    jni_bridge.cpp
)
target_link_libraries(whisper_jni PRIVATE whisper ${LOG_LIB} ${ANDROID_LIB})

# 16 KB page size support (Android 15+): NDK r27 does not enable 16 KB ELF
# segment alignment by default (that only became automatic in r28), so the
# linker flag must be set explicitly or the .so fails the 16 KB device check.
target_link_options(whisper_jni PRIVATE "-Wl,-z,max-page-size=16384")

if (NOT CMAKE_BUILD_TYPE STREQUAL "Debug")
    target_compile_options(whisper PRIVATE -O3)
    target_compile_options(whisper_jni PRIVATE -O3)
endif()
