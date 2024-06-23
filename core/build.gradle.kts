plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
}

android {
    val compileSdkVersion: Int by project
    val minSdkVersion: Int by project

    namespace = "com.fibelatti.core"

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
                implementation(libs.kotlin)
                implementation(libs.coroutines.core)
                implementation(libs.ktor.io)
            }
        }

        commonTest {
            dependencies {
                compileOnly(libs.junit)
                runtimeOnly(libs.junit5.engine)
                runtimeOnly(libs.junit5.vintage)
                implementation(libs.junit5.api)
                implementation(libs.junit5.params)

                implementation(libs.truth)
                implementation(libs.mockk)
                implementation(libs.coroutines.test)
            }
        }
    }
}
