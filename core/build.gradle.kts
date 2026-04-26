plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.fibelatti.kotlin.library)
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

        jvmTest {
            dependencies {
                implementation(project.dependencies.platform(libs.junit6.bom))
                compileOnly(libs.junit)
                runtimeOnly(libs.junit6.engine)
                runtimeOnly(libs.junit6.launcher)
                runtimeOnly(libs.junit6.vintage)
                implementation(libs.junit6.api)
                implementation(libs.junit6.params)

                implementation(libs.truth)
                implementation(libs.mockk)
                implementation(libs.coroutines.test)
            }
        }
    }
}
