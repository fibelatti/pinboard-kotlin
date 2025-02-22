plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
    jvm()

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
                implementation(libs.junit.launcher)

                implementation(libs.truth)
                implementation(libs.mockk)
                implementation(libs.coroutines.test)
            }
        }
    }
}
