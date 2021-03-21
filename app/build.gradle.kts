import com.android.build.gradle.internal.api.ReadOnlyProductFlavor
import org.gradle.internal.extensibility.DefaultExtraPropertiesExtension

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-parcelize")
    id("kotlin-kapt")
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
                arguments(
                    mapOf(
                        "room.schemaLocation" to "$projectDir/schemas",
                        "room.incremental" to "true"
                    )
                )
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
            require(this is ExtensionAware)
            extra["appNameSuffix"] = mapOf("debug" to " Dev", "release" to "")
        }

        create("noapi") {
            dimension("api")
            applicationIdSuffix(".noapi")
            require(this is ExtensionAware)
            extra["appNameSuffix"] = mapOf("debug" to "NoApi Dev", "release" to " NoApi")
        }
    }

    afterEvaluate {
        applicationVariants.forEach { variant ->
            val flavor = variant.productFlavors[0] as ReadOnlyProductFlavor
            val extra = flavor.getProperty("ext") as DefaultExtraPropertiesExtension
            val appNameSuffix = extra.get("appNameSuffix")
            require(appNameSuffix is Map<*, *>)
            variant.resValue("string", "app_name", "${AppInfo.appName}${appNameSuffix[variant.buildType.name]}")
        }
    }

    sourceSets {
        getByName("main").java.srcDirs("src/main/kotlin")

        getByName("pinboardapi").java.srcDir("src/pinboardapi/kotlin")
        getByName("noapi").java.srcDir("src/noapi/kotlin")

        getByName("test").java.srcDirs("src/test/kotlin", "src/sharedTest/kotlin")
        getByName("testPinboardapi").java
            .srcDirs("src/test/kotlin", "src/sharedTest/kotlin", "src/testPinboardapi/kotlin")


        getByName("androidTest") {
            java.srcDirs("src/androidTest/kotlin", "src/sharedTest/kotlin")
            assets.srcDirs(files("$projectDir/schemas"))
        }
    }

    compileOptions {
        sourceCompatibility(JavaVersion.VERSION_1_8)
        targetCompatibility(JavaVersion.VERSION_1_8)
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
    implementation(Dependencies.dagger)
    kapt(Dependencies.daggerCompiler)

    implementation(Dependencies.gson)

    implementation(Dependencies.okhttp)
    implementation(Dependencies.retrofit)
    implementation(Dependencies.retrofitGsonConverter)
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

    testImplementation(Dependencies.kotlinReflect)

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
