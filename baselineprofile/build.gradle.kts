@file:Suppress("UnstableApiUsage")

plugins {
    alias(libs.plugins.android.test)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.baselineprofile)
}

android {
    val targetSdkVersion: Int by project

    namespace = "com.fibelatti.pinboard.baselineprofile"

    defaultConfig {
        targetSdk = targetSdkVersion
        minSdk = 28

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    targetProjectPath = ":app"
}

baselineProfile {
    managedDevices += "pixel8api34"
    useConnectedDevices = false
}

dependencies {
    implementation(libs.androidx.junit)
    implementation(libs.espresso.core)
    implementation(libs.uiautomator)
    implementation(libs.benchmark.macro.junit4)
}

androidComponents {
    onVariants { variant ->
        variant.instrumentationRunnerArguments.put(
            "targetAppId",
            variant.testedApks.map { variant.artifacts.getBuiltArtifactsLoader().load(it)?.applicationId.orEmpty() },
        )
    }
}
