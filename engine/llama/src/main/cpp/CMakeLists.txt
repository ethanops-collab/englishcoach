cmake_minimum_required(VERSION 3.22)

project(llama_jni)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_POSITION_INDEPENDENT_CODE ON)

set(LLAMA_CPP_DIR ${CMAKE_CURRENT_SOURCE_DIR}/thirdparty/llama_cpp)

# LLAMA_STANDALONE evaluates to OFF when pulled in via add_subdirectory (our case), which
# makes llama.cpp's own root CMakeLists.txt skip common/, tools/, tests/, examples/, app/ -
# only the `ggml` and `llama` targets get built. No need to hand-list src/*.cpp ourselves.
add_subdirectory(${LLAMA_CPP_DIR} ${CMAKE_CURRENT_BINARY_DIR}/llama_cpp)

find_library(LOG_LIB log)
find_library(ANDROID_LIB android)

add_library(llama_jni SHARED
    jni_bridge.cpp
)
target_link_libraries(llama_jni PRIVATE llama ${LOG_LIB} ${ANDROID_LIB})

# 16 KB page size support (Android 15+): NDK r27 does not enable 16 KB ELF
# segment alignment by default (that only became automatic in r28), so the
# linker flag must be set explicitly or the .so fails the 16 KB device check.
target_link_options(llama_jni PRIVATE "-Wl,-z,max-page-size=16384")

if (NOT CMAKE_BUILD_TYPE STREQUAL "Debug")
    target_compile_options(llama_jni PRIVATE -O3)
endif()
