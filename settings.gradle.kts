rootProject.name = "Pinkt"
rootProject.buildFileName = "build.gradle.kts"

include(":app")
include(":core")
include(":ui")

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
