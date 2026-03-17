import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
}

group = "com.fibelatti.buildlogic"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_21
        freeCompilerArgs.set(listOf("-Xcontext-parameters"))
    }
}

dependencies {
    compileOnly(libs.android.gradle.plugin)
    compileOnly(libs.compose.gradle.plugin)
    compileOnly(libs.spotless.gradle.plugin)
}

tasks {
    validatePlugins {
        enableStricterValidation = true
        failOnWarning = true
    }
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = libs.plugins.fibelatti.android.application.get().pluginId
            implementationClass = "AndroidApplicationConventionPlugin"
        }

        register("androidLibrary") {
            id = libs.plugins.fibelatti.android.library.get().pluginId
            implementationClass = "AndroidLibraryConventionPlugin"
        }

        register("androidBase") {
            id = libs.plugins.fibelatti.android.base.get().pluginId
            implementationClass = "AndroidBaseConventionPlugin"
        }

        register("androidCompose") {
            id = libs.plugins.fibelatti.android.compose.get().pluginId
            implementationClass = "AndroidComposeConventionPlugin"
        }

        register("kotlinLibrary") {
            id = libs.plugins.fibelatti.kotlin.library.get().pluginId
            implementationClass = "KotlinLibraryConventionPlugin"
        }

        register("manifestPermissionValidation") {
            id = libs.plugins.fibelatti.manifest.permission.validation.get().pluginId
            implementationClass = "ManifestPermissionValidationPlugin"
        }

        register("spotless") {
            id = libs.plugins.fibelatti.spotless.get().pluginId
            implementationClass = "SpotlessPlugin"
        }
    }
}
