plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.android.library)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
}

android {
    val compileSdkVersion: Int by project
    val minSdkVersion: Int by project

    namespace = "com.fibelatti.bookmarking"

    compileSdk = compileSdkVersion

    defaultConfig {
        minSdk = minSdkVersion
    }

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
}

kotlin {
    explicitApi()
    jvmToolchain(17)

    androidTarget()

    sourceSets {
        androidMain {
            dependencies {
                implementation(libs.ktor.client.okhttp)

                implementation(project.dependencies.platform(libs.koin.bom))
                implementation(libs.koin.android)
            }
        }

        commonMain {
            dependencies {
                implementation(projects.core)

                implementation(libs.kotlin)
                implementation(libs.kotlin.serialization)
                implementation(libs.kotlin.datetime)

                implementation(libs.coroutines.core)

                implementation(libs.bundles.ktor.common)
                implementation(libs.urlencoder)

                implementation(libs.room.runtime)
                implementation(libs.sqlite.bundled)
                implementation(libs.multiplatform.settings)

                implementation(project.dependencies.platform(libs.koin.bom))
                implementation(libs.koin.core)
                compileOnly(libs.koin.annotations)
            }
        }

        commonTest {
            dependencies {
                implementation(projects.core)
                implementation(projects.bookmarkingTest)

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

dependencies {
    add("kspAndroid", libs.koin.ksp.compiler)
    add("kspAndroid", libs.room.compiler)
}

ksp {
    arg("room.incremental", "true")
    arg("room.generateKotlin", "true")
}

room {
    schemaDirectory("$projectDir/schemas")
}
