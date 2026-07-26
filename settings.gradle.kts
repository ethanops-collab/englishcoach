pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "English Coach"

include(":app")

include(":core:common")
include(":core:model")
include(":core:designsystem")
include(":core:database")
include(":core:i18n")

include(":engine:speech")
include(":engine:whisper")
include(":engine:llm")
include(":engine:llama")
include(":engine:tts")
include(":engine:systemtts")
include(":engine:pronunciation")

include(":domain")

include(":feature:home")
include(":feature:lesson")
include(":feature:progress")
