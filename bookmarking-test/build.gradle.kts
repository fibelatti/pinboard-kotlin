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
                implementation(projects.core)
                implementation(projects.bookmarking)

                implementation(libs.room.testing)

                implementation(libs.bundles.ktor.common)
                implementation(libs.urlencoder)
                implementation(libs.mockwebserver)

                implementation(project.dependencies.platform(libs.koin.bom))
                implementation(libs.koin.core)
                implementation(libs.koin.android)
            }
        }
    }
}
