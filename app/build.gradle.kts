plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-parcelize")
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
}

val jacocoEnabled: Boolean by project
if (jacocoEnabled) {
    println("Applying coverage-report.gradle")
    apply {
        from("coverage-report.gradle")
    }
}

android {
    buildFeatures {
        viewBinding = true
    }

    compileSdkVersion(Versions.compileSdkVersion)

    defaultConfig {
        applicationId = AppInfo.applicationId
        versionCode = AppInfo.versionCode
        versionName = AppInfo.versionName
        minSdkVersion(Versions.minSdkVersion)
        targetSdkVersion(Versions.targetSdkVersion)

        resConfigs("en")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true

        javaCompileOptions {
            annotationProcessorOptions {
                arguments["room.schemaLocation"] = "$projectDir/schemas"
                arguments["room.incremental"] = "true"
            }
        }
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
            isTestCoverageEnabled = jacocoEnabled
        }

        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            setProguardFiles(
                listOf(getDefaultProguardFile("proguard-android-optimize.txt"), File("proguard-rules.pro"))
            )
        }
    }

    flavorDimensions("api")
    productFlavors {
        create("pinboardapi") {
            dimension("api")
        }

        create("noapi") {
            dimension("api")
            applicationIdSuffix(".noapi")
        }
    }

    androidComponents {
        onVariants { variant ->
            val appName = StringBuilder().apply {
                append(AppInfo.appName)
                if (variant.name.contains("noapi", ignoreCase = true)) append(" NoApi")
                if (variant.name.contains("debug", ignoreCase = true)) append(" Dev")
            }

            variant.addResValue("app_name", "string", "$appName", null)
        }
    }

    sourceSets {
        forEach { sourceSet -> getByName(sourceSet.name).java.srcDirs("src/${sourceSet.name}/kotlin") }

        getByName("test").java.srcDirs("src/sharedTest/kotlin")
        getByName("testPinboardapi").java.srcDirs("src/test/kotlin", "src/sharedTest/kotlin")

        getByName("androidTest") {
            java.srcDirs("src/sharedTest/kotlin")
            assets.srcDirs(files("$projectDir/schemas"))
        }
    }

    compileOptions {
        sourceCompatibility(JavaVersion.VERSION_1_8)
        targetCompatibility(JavaVersion.VERSION_1_8)
    }

    kotlinOptions {
        freeCompilerArgs = freeCompilerArgs + "-Xuse-experimental=kotlinx.coroutines.ExperimentalCoroutinesApi"
    }

    packagingOptions {
        exclude("META-INF/LICENSE.md")
        exclude("META-INF/LICENSE-notice.md")
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
    }
}

dependencies {
    implementation(Dependencies.coreLib)
    implementation(Dependencies.coreLibArch)

    // Kotlin
    implementation(Dependencies.kotlin)
    implementation(Dependencies.kotlinReflect)
    implementation(Dependencies.coroutinesCore)
    implementation(Dependencies.coroutinesAndroid)

    // Android
    implementation(Dependencies.supportLibrary)
    implementation(Dependencies.androidCore)
    implementation(Dependencies.activity)
    implementation(Dependencies.fragments)
    implementation(Dependencies.materialDesign)
    implementation(Dependencies.constraintLayout)
    implementation(Dependencies.swipeRefreshLayout)

    implementation(Dependencies.lifecycleJava8)

    implementation(Dependencies.archComponents)
    kapt(Dependencies.archComponentsCompiler)

    implementation(Dependencies.room)
    kapt(Dependencies.roomCompiler)

    implementation(Dependencies.workManager)

    implementation(Dependencies.customTabs)

    // Misc
    implementation(Dependencies.hilt)
    kapt(Dependencies.hiltCompiler)

    implementation(Dependencies.moshi)
    kapt(Dependencies.moshiCodeGen)

    implementation(Dependencies.okhttp)
    implementation(Dependencies.retrofit)
    implementation(Dependencies.retrofitConverter)
    implementation(Dependencies.httpLoggingInterceptor)

    implementation(Dependencies.jsoup)

    implementation(Dependencies.playCore)

    debugImplementation(Dependencies.leakCanary)

    // Test
    testCompileOnly(TestDependencies.junit)
    testRuntimeOnly(TestDependencies.junit5Engine)
    testRuntimeOnly(TestDependencies.junitVintage)
    testImplementation(TestDependencies.junit5)
    testImplementation(TestDependencies.junit5Params)

    testImplementation(TestDependencies.coreLibTest)

    testImplementation(TestDependencies.googleTruth)
    testImplementation(TestDependencies.mockk)
    testImplementation(TestDependencies.coroutinesTest)
    testImplementation(TestDependencies.archComponentsTest)

    androidTestImplementation(TestDependencies.googleTruth)
    androidTestImplementation(Dependencies.supportAnnotations)
    androidTestImplementation(TestDependencies.testRunner)
    androidTestImplementation(TestDependencies.coreLibTest)
    androidTestImplementation(TestDependencies.kotlinTest)
    androidTestImplementation(TestDependencies.archComponentsTest)
    androidTestImplementation(TestDependencies.roomTest)
}

kapt {
    correctErrorTypes = true
}
