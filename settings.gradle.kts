@file:Suppress("UnstableApiUsage")

rootProject.name = "Pinkt"
rootProject.buildFileName = "build.gradle.kts"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(":app")
include(":bookmarking")
include(":bookmarking-test")
include(":core")
include(":core-android")
include(":ui")

pluginManagement {
    repositories {
        mavenCentral {
            content {
                excludeGroupByRegex("com\\.android.*")
            }
        }
        google()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral {
            content {
                excludeGroupByRegex("androidx.*")
                excludeGroupByRegex("com\\.android.*")
            }
        }
        google()
    }
}
