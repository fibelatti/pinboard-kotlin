@file:Suppress("UnstableApiUsage")

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose.screenshot)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.room)
    alias(libs.plugins.about.libraries)
}

object AppInfo {

    const val APP_NAME = "Pinkt"
    const val APPLICATION_ID = "com.fibelatti.pinboard"

    private const val VERSION_MAJOR = 3
    private const val VERSION_MINOR = 6
    private const val VERSION_PATCH = 1
    private const val VERSION_BUILD = 0

    val versionCode: Int = (VERSION_MAJOR * 1_000_000 + VERSION_MINOR * 10_000 + VERSION_PATCH * 100 + VERSION_BUILD)
        .also { println("versionCode: $it") }

    @Suppress("KotlinConstantConditions")
    val versionName: String = StringBuilder("$VERSION_MAJOR.$VERSION_MINOR")
        .apply { if (VERSION_PATCH != 0) append(".$VERSION_PATCH") }
        .toString()
        .also { println("versionName: $it") }
}

android {
    val compileSdkVersion: Int by project
    val targetSdkVersion: Int by project
    val minSdkVersion: Int by project

    namespace = "com.fibelatti.pinboard"
    compileSdk = compileSdkVersion

    buildFeatures {
        buildConfig = true
        compose = true
        viewBinding = true
    }

    defaultConfig {
        applicationId = AppInfo.APPLICATION_ID
        versionCode = AppInfo.versionCode
        versionName = AppInfo.versionName
        targetSdk = targetSdkVersion
        minSdk = minSdkVersion

        base.archivesName = "$applicationId-v$versionName-$versionCode"

        testInstrumentationRunner = "com.fibelatti.pinboard.tooling.HiltTestRunner"
        testInstrumentationRunnerArguments["clearPackageData"] = "true"

        vectorDrawables.useSupportLibrary = true
    }

    signingConfigs {
        getByName("debug") {
            storeFile = File("$rootDir/keystore/debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }

        create("release") {
            storeFile = File("$rootDir/keystore/release.jks")
            storePassword = System.getenv("SIGNING_STORE_PASSWORD")
            keyAlias = System.getenv("SIGNING_KEY_ALIAS")
            keyPassword = System.getenv("SIGNING_KEY_PASSWORD")
        }
    }

    buildTypes {
        getByName("debug") {
            applicationIdSuffix = ".debug"
            isMinifyEnabled = false
        }

        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            isCrunchPngs = false

            if (System.getenv("SIGN_BUILD").toBoolean()) {
                signingConfig = signingConfigs.getByName("release")
            }

            setProguardFiles(
                listOf(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    File("proguard-rules.pro"),
                ),
            )
        }
    }

    sourceSets {
        forEach { sourceSet -> getByName(sourceSet.name).java.srcDirs("src/${sourceSet.name}/kotlin") }

        getByName("androidTest").assets.srcDirs(files("$projectDir/schemas"))
    }

    packaging {
        resources.excludes.add("META-INF/LICENSE.md")
        resources.excludes.add("META-INF/LICENSE-notice.md")
    }

    dependenciesInfo {
        // Disables dependency metadata when building APKs.
        includeInApk = false
        // Disables dependency metadata when building Android App Bundles.
        includeInBundle = false
    }

    testOptions {
        execution = "ANDROIDX_TEST_ORCHESTRATOR"

        screenshotTests {
            imageDifferenceThreshold = 0.001f // 0.1%
        }
    }

    testFixtures {
        enable = true
    }

    experimentalProperties["android.experimental.enableScreenshotTest"] = true
}

androidComponents {
    onVariants { variant ->
        val appName =
            StringBuilder().apply {
                append(AppInfo.APP_NAME)
                if (variant.name.contains("debug", ignoreCase = true)) append(" Dev")
            }.toString()

        variant.resValues.put(
            variant.makeResValueKey("string", "app_name"),
            com.android.build.api.variant.ResValue(appName, null),
        )

        variant.androidResources.localeFilters.add("en")
    }

    onVariants(selector().withBuildType("release")) { variant ->
        variant.packaging.resources.excludes.add("META-INF/*.version")
    }
}

ksp {
    arg("room.incremental", "true")
    arg("room.generateKotlin", "true")
}

room {
    schemaDirectory("$projectDir/schemas")
}

aboutLibraries {
    excludeFields = arrayOf("generated")
    registerAndroidTasks = false
}

dependencies {
    implementation(projects.core)
    implementation(projects.coreAndroid)
    implementation(projects.ui)

    // Kotlin
    implementation(libs.kotlin)
    implementation(libs.kotlin.reflect)
    implementation(libs.kotlin.serialization)
    implementation(libs.kotlin.datetime)

    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)

    // Android
    implementation(libs.appcompat)
    implementation(libs.core.ktx)
    implementation(libs.activity)
    implementation(libs.activity.compose)
    implementation(libs.fragment.ktx)
    implementation(libs.transition.ktx)
    implementation(libs.material)
    implementation(libs.constraint.layout)
    implementation(libs.constraint.layout.compose)
    implementation(libs.window)

    implementation(libs.lifecycle.java8)
    implementation(libs.lifecycle.process)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.lifecycle.runtime.compose)

    implementation(libs.room.runtime)
    ksp(libs.room.compiler)

    implementation(libs.work.runtime.ktx)

    implementation(libs.browser)

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.runtime)
    implementation(libs.compose.material)
    implementation(libs.compose.material3)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)

    // Misc
    ksp(libs.dagger.hilt.compiler)
    ksp(libs.hilt.compiler)
    implementation(libs.dagger.hilt.android)
    implementation(libs.hilt.work)
    implementation(libs.hilt.navigation.compose)

    implementation(libs.bundles.ktor.common)
    implementation(libs.ktor.client.okhttp)

    implementation(libs.jsoup)

    implementation(libs.about.libraries)

    debugImplementation(libs.leakcanary)

    // Test
    testFixturesImplementation(libs.kotlin)
    testFixturesImplementation(platform(libs.compose.bom))
    testFixturesImplementation(libs.compose.runtime)

    testCompileOnly(libs.junit)
    testRuntimeOnly(libs.junit5.engine)
    testRuntimeOnly(libs.junit5.vintage)
    testImplementation(libs.junit5.api)
    testImplementation(libs.junit5.params)

    testImplementation(libs.truth)
    testImplementation(libs.mockk)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.arch.core.testing)

    androidTestImplementation(libs.runner)
    androidTestUtil(libs.orchestrator)

    androidTestImplementation(libs.truth)
    androidTestImplementation(libs.coroutines.test)
    androidTestImplementation(libs.room.testing)

    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test.junit4)

    androidTestImplementation(libs.hilt.android.testing)
    kspAndroidTest(libs.hilt.android.compiler)

    androidTestImplementation(libs.mockwebserver)

    screenshotTestImplementation(libs.compose.ui.tooling)
}

/**
 * Prints the current version code. Used for GitHub releases.
 */
val printReleaseVersionCode by tasks.registering {
    doLast {
        println(AppInfo.versionCode)
    }
}
