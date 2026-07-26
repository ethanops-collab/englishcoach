plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    api(project(":core:model"))
    api(project(":core:common"))
    implementation(project(":engine:speech"))
    implementation(project(":engine:llm"))
    implementation(project(":engine:tts"))
    implementation(project(":engine:pronunciation"))
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.javax.inject)

    testImplementation(libs.junit)
}
