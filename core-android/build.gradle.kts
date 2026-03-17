plugins {
    alias(libs.plugins.fibelatti.android.library)
}

android {
    namespace = "com.fibelatti.core.android"
}

dependencies {
    implementation(libs.appcompat)
}
