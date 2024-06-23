plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
}

android {
    val compileSdkVersion: Int by project
    val minSdkVersion: Int by project

    namespace = "com.fibelatti.bookmarking.test"

    compileSdk = compileSdkVersion

    defaultConfig {
        minSdk = minSdkVersion
    }
}

kotlin {
    explicitApi()
    jvmToolchain(17)

    androidTarget()

    sourceSets {
        commonMain {
            dependencies {
                implementation(projects.bookmarking)

                implementation(libs.urlencoder)
            }
        }
    }
}
