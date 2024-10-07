plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.room)
    alias(libs.plugins.about.libraries)
}

val jacocoEnabled: Boolean by project
if (jacocoEnabled) {
    println("Applying coverage-report.gradle")
    apply {
        from("coverage-report.gradle")
    }
}

object AppInfo {
    const val APP_NAME = "Pinkt"
    const val APPLICATION_ID = "com.fibelatti.pinboard"

    private const val VERSION_MAJOR = 3
    private const val VERSION_MINOR = 2
    private const val VERSION_PATCH = 0
    private const val VERSION_BUILD = 0

    val versionCode: Int =
        (VERSION_MAJOR * 1_000_000 + VERSION_MINOR * 10_000 + VERSION_PATCH * 100 + VERSION_BUILD)
            .also { println("versionCode: $it") }

    @Suppress("KotlinConstantConditions")
    val versionName: String =
        StringBuilder("$VERSION_MAJOR.$VERSION_MINOR")
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

        resourceConfigurations.add("en")

        testInstrumentationRunner = "com.fibelatti.pinboard.tooling.HiltTestRunner"
        testInstrumentationRunnerArguments["clearPackageData"] = "true"

        vectorDrawables.useSupportLibrary = true
    }

    signingConfigs {
        getByName("debug") {
            storeFile = File("$rootDir/keystore/debug.keystore")
            keyAlias = "androiddebugkey"
            keyPassword = "android"
            storePassword = "android"
        }
    }

    buildTypes {
        getByName("debug") {
            applicationIdSuffix = ".debug"
            isMinifyEnabled = false
            enableUnitTestCoverage = jacocoEnabled
            enableAndroidTestCoverage = jacocoEnabled
        }

        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            isCrunchPngs = false
            setProguardFiles(
                listOf(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    File("proguard-rules.pro"),
                ),
            )
        }
    }

    flavorDimensions.add("api")
    productFlavors {
        create("pinboardapi") {
            dimension = "api"
        }

        create("noapi") {
            dimension = "api"
            applicationIdSuffix = ".noapi"
        }
    }

    sourceSets {
        forEach { sourceSet -> getByName(sourceSet.name).java.srcDirs("src/${sourceSet.name}/kotlin") }

        getByName("test").java.srcDirs("src/sharedTest/kotlin")

        getByName("androidTest") {
            java.srcDirs("src/sharedTest/kotlin")
            assets.srcDirs(files("$projectDir/schemas"))
        }
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
    }
}

androidComponents {
    onVariants { variant ->
        val appName =
            StringBuilder().apply {
                append(AppInfo.APP_NAME)
                if (variant.name.contains("noapi", ignoreCase = true)) append(" NoApi")
                if (variant.name.contains("debug", ignoreCase = true)) append(" Dev")
            }.toString()

        variant.resValues.put(
            variant.makeResValueKey("string", "app_name"),
            com.android.build.api.variant.ResValue(appName, null),
        )
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

    implementation(libs.okhttp)
    implementation(libs.retrofit)
    implementation(libs.converter.serialization)
    implementation(libs.logging.interceptor)

    implementation(libs.jsoup)

    implementation(libs.about.libraries)

    debugImplementation(libs.leakcanary)

    // Test
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
}
