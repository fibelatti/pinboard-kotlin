@file:Suppress("UnstableApiUsage")

import com.android.build.api.dsl.CommonExtension
import com.diffplug.gradle.spotless.SpotlessExtension
import com.diffplug.gradle.spotless.SpotlessExtensionPredeclare
import com.diffplug.gradle.spotless.SpotlessPlugin
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.spotless) apply false
    alias(libs.plugins.cache.fix) apply false
    alias(libs.plugins.about.libraries) apply false
    alias(libs.plugins.licensee) apply false
}

buildscript {
    extra["compileSdkVersion"] = 36
    extra["targetSdkVersion"] = 36
    extra["minSdkVersion"] = 26
}

private val javaVersion = JavaVersion.VERSION_21

allprojects {
    apply<SpotlessPlugin>()

    val disabledRules = listOf(
        "ktlint_standard_blank-line-before-declaration",
        "ktlint_standard_function-expression-body",
        "ktlint_standard_class-signature",
        "ktlint_standard_function-signature",
    ).associateWith { "disabled" }

    val configuredRules = mapOf(
        "ktlint_function_naming_ignore_when_annotated_with" to "Composable",
        "ktlint_ignore_back_ticked_identifier" to true,
    )

    val allRules = disabledRules + configuredRules

    val configureSpotless: SpotlessExtension.() -> Unit = {
        kotlin {
            target("**/*.kt")
            targetExclude("**/build/**/*.kt")

            ktlint().editorConfigOverride(allRules)
        }
        kotlinGradle {
            target("**/*.kts")
            targetExclude("**/build/**/*.kts")

            ktlint().editorConfigOverride(allRules)
        }
        format("xml") {
            target("**/*.xml")
            targetExclude("**/build/**/*.xml")
        }
        format("misc") {
            target("*.gradle", "*.md", ".gitignore")

            trimTrailingWhitespace()
            leadingTabsToSpaces()
            endWithNewline()
        }
    }

    if (project === rootProject) {
        extensions.getByType<SpotlessExtension>().predeclareDeps()
        extensions.configure<SpotlessExtensionPredeclare>(configureSpotless)
    } else {
        extensions.configure(configureSpotless)
    }
}

subprojects {
    afterEvaluate {
        plugins.withType<com.android.build.gradle.api.AndroidBasePlugin> {
            apply(plugin = libs.plugins.cache.fix.get().pluginId)
        }

        extensions.findByType<KotlinProjectExtension>()?.apply {
            if (!plugins.hasPlugin("com.android.application")) {
                explicitApi()
            }
        }

        extensions.findByType(CommonExtension::class.java)?.apply {
            val compileSdkVersion: Int by project
            val minSdkVersion: Int by project

            compileSdk = compileSdkVersion

            defaultConfig {
                minSdk = minSdkVersion
            }

            compileOptions {
                isCoreLibraryDesugaringEnabled = true
                sourceCompatibility(javaVersion)
                targetCompatibility(javaVersion)
            }

            testOptions {
                animationsDisabled = true

                unitTests {
                    isReturnDefaultValues = true
                    isIncludeAndroidResources = true

                    all {
                        it.useJUnitPlatform()
                    }
                }

                managedDevices {
                    allDevices {
                        maybeCreate<com.android.build.api.dsl.ManagedVirtualDevice>("pixel8api34").apply {
                            device = "Pixel 8"
                            apiLevel = 34
                            systemImageSource = "google"
                        }
                    }
                }
            }
        }

        extensions.findByType(ComposeCompilerGradlePluginExtension::class.java)?.apply {
            stabilityConfigurationFiles.add(rootProject.layout.projectDirectory.file("compose_compiler_config.conf"))

            if (project.findProperty("composeCompilerReports") == "true") {
                val destinationDir = project.layout.buildDirectory.dir("compose_compiler")
                reportsDestination = destinationDir
                metricsDestination = destinationDir
            }
        }

        tasks.withType<JavaCompile>().configureEach {
            sourceCompatibility = javaVersion.toString()
            targetCompatibility = javaVersion.toString()
        }

        tasks.withType<KotlinCompile>().configureEach {
            compilerOptions {
                jvmTarget.set(JvmTarget.fromTarget(javaVersion.toString()))
                freeCompilerArgs = buildList {
                    addAll(freeCompilerArgs.get())
                    add("-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi")
                    add("-Xannotation-default-target=param-property")
                }
            }
        }

        if (extensions.findByType(CommonExtension::class.java) != null) {
            dependencies {
                "coreLibraryDesugaring"(libs.core.library.desugaring)
            }
        }
    }
}
