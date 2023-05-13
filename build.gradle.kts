import com.android.build.api.dsl.CommonExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.parcelize) apply false
    alias(libs.plugins.kapt) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.spotless) apply false
    alias(libs.plugins.cache.fix) apply false
}

buildscript {
    extra["compileSdkVersion"] = 33
    extra["targetSdkVersion"] = 33
    extra["minSdkVersion"] = 23

    val jacocoEnabled: String? by project
    extra["jacocoEnabled"] = jacocoEnabled?.toBoolean() ?: false
}

subprojects {
    afterEvaluate {
        plugins.withType<com.android.build.gradle.api.AndroidBasePlugin> {
            apply(plugin = "org.gradle.android.cache-fix")
        }

        apply(plugin = "com.diffplug.spotless")
        extensions.configure<com.diffplug.gradle.spotless.SpotlessExtension> {
            kotlin {
                target("**/*.kt")
                targetExclude("**/build/**/*.kt")

                ktlint().userData(mapOf("android" to "true"))
            }
            kotlinGradle {
                target("**/*.kts")
                targetExclude("**/build/**/*.kts")

                ktlint()
            }
            format("misc") {
                target("*.gradle", "*.md", ".gitignore")

                trimTrailingWhitespace()
                indentWithSpaces()
                endWithNewline()
            }
        }

        extensions.findByType(CommonExtension::class.java)?.apply {
            compileOptions {
                sourceCompatibility(JavaVersion.VERSION_17)
                targetCompatibility(JavaVersion.VERSION_17)
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
                    devices {
                        maybeCreate<com.android.build.api.dsl.ManagedVirtualDevice>("pixel2api30").apply {
                            device = "Pixel 2"
                            apiLevel = 30
                            systemImageSource = "aosp-atd"
                        }
                    }
                }
            }
        }

        tasks.withType<KotlinCompile>().configureEach {
            kotlinOptions.apply {
                jvmTarget = "17"
                freeCompilerArgs = buildList {
                    addAll(freeCompilerArgs)

                    add("-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi")

                    if (project.findProperty("composeCompilerReports") == "true") {
                        val composeCompilerPath = "${project.buildDir.absolutePath}/compose_compiler"
                        add("-P")
                        add("plugin:androidx.compose.compiler.plugins.kotlin:reportsDestination=$composeCompilerPath")
                        add("-P")
                        add("plugin:androidx.compose.compiler.plugins.kotlin:metricsDestination=$composeCompilerPath")
                    }
                }
            }
        }

        tasks.findByName("preBuild")?.dependsOn("spotlessCheck")
    }
}
