rootProject.name = "Pinkt"
rootProject.buildFileName = "build.gradle.kts"

include(":app")
include(":core")
include(":ui")

pluginManagement {
    repositories {
        mavenCentral()
        google()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        google()
    }
}
