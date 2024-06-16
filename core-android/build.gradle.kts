plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    val compileSdkVersion: Int by project
    val minSdkVersion: Int by project

    namespace = "com.fibelatti.core.android"

    compileSdk = compileSdkVersion

    defaultConfig {
        minSdk = minSdkVersion
    }
}

dependencies {
    implementation(libs.kotlin)
    implementation(libs.coroutines.core)

    implementation(libs.core.ktx)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.viewbinding)

    testCompileOnly(libs.junit)
    testRuntimeOnly(libs.junit5.engine)
    testRuntimeOnly(libs.junit5.vintage)
    testImplementation(libs.junit5.api)
    testImplementation(libs.junit5.params)

    testImplementation(libs.truth)
    testImplementation(libs.mockk)
    testImplementation(libs.coroutines.test)
}
