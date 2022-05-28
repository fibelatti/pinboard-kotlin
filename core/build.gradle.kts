plugins {
    id("com.android.library")
    id("kotlin-android")
}

android {
    val compileSdkVersion: Int by project
    val targetSdkVersion: Int by project
    val minSdkVersion: Int by project

    compileSdk = compileSdkVersion

    defaultConfig {
        targetSdk = targetSdkVersion
        minSdk = minSdkVersion
    }

    compileOptions {
        sourceCompatibility(JavaVersion.VERSION_11)
        targetCompatibility(JavaVersion.VERSION_11)
    }

    kotlinOptions {
        freeCompilerArgs = freeCompilerArgs + "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi"
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
    implementation(libs.kotlin)
    implementation(libs.coroutines.core)

    implementation(libs.core.ktx)
    implementation(libs.appcompat)
    implementation(libs.material)

    testCompileOnly(libs.junit)
    testRuntimeOnly(libs.junit5.engine)
    testRuntimeOnly(libs.junit5.vintage)
    testImplementation(libs.junit5.api)
    testImplementation(libs.junit5.params)

    testImplementation(libs.truth)
    testImplementation(libs.mockk)
    testImplementation(libs.coroutines.test)
}
