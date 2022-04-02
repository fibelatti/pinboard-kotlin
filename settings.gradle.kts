rootProject.name = "Pinkt"
rootProject.buildFileName = "build.gradle.kts"

include(":app")

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            version("kotlin", "1.6.10")
            version("coroutines", "1.6.0")
            version("hilt", "2.40.5")
            version("room", "2.4.0")
            version("lifecycle", "2.4.1")
            version("fibelatti", "2.0.0-alpha4")

            // Classpath
            library("android-gradle-plugin", "com.android.tools.build:gradle:7.1.2")
            library("kotlin-gradle-plugin", "org.jetbrains.kotlin", "kotlin-gradle-plugin").versionRef("kotlin")
            library("hilt-gradle-plugin", "com.google.dagger", "hilt-android-gradle-plugin").versionRef("hilt")

            // Project dependencies
            library("kotlin", "org.jetbrains.kotlin", "kotlin-stdlib-jdk8").versionRef("kotlin")
            library("kotlin-reflect", "org.jetbrains.kotlin", "kotlin-reflect").versionRef("kotlin")

            library("coroutines-core", "org.jetbrains.kotlinx", "kotlinx-coroutines-core").versionRef("coroutines")
            library("coroutines-android", "org.jetbrains.kotlinx", "kotlinx-coroutines-android").versionRef("coroutines")

            library("appcompat", "androidx.appcompat:appcompat:1.4.1")
            library("core-ktx", "androidx.core:core-ktx:1.7.0")
            library("activity-ktx", "androidx.activity:activity-ktx:1.4.0")
            library("fragment-ktx", "androidx.fragment:fragment-ktx:1.4.1")
            library("annotations", "androidx.annotation:annotation:1.3.0")
            library("material", "com.google.android.material:material:1.5.0")
            library("constraint-layout", "androidx.constraintlayout:constraintlayout:2.1.3")
            library("swipe-refresh-layout", "androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
            library("fibelatti-core", "com.fibelatti.core", "core").versionRef("fibelatti")
            library("fibelatti-arch-components", "com.fibelatti.core", "arch-components").versionRef("fibelatti")
            library("lifecycle-java8", "androidx.lifecycle", "lifecycle-common-java8").versionRef("lifecycle")
            library("lifecycle-runtime-ktx", "androidx.lifecycle", "lifecycle-runtime-ktx").versionRef("lifecycle")
            library("room-runtime", "androidx.room", "room-runtime").versionRef("room")
            library("room-compiler", "androidx.room", "room-compiler").versionRef("room")
            library("work-runtime-ktx", "androidx.work:work-runtime-ktx:2.7.1")
            library("browser", "androidx.browser:browser:1.4.0")
            library("hilt-android", "com.google.dagger", "hilt-android").versionRef("hilt")
            library("hilt-compiler", "com.google.dagger", "hilt-compiler").versionRef("hilt")

            version("moshi", "1.13.0")
            library("moshi", "com.squareup.moshi", "moshi-kotlin").versionRef("moshi")
            library("moshi-codegen", "com.squareup.moshi", "moshi-kotlin-codegen").versionRef("moshi")

            version("okhttp", "4.9.3")
            library("okhttp", "com.squareup.okhttp3", "okhttp").versionRef("okhttp")
            library("logging-interceptor", "com.squareup.okhttp3", "logging-interceptor").versionRef("okhttp")

            version("retrofit", "2.9.0")
            library("retrofit", "com.squareup.retrofit2", "retrofit").versionRef("retrofit")
            library("converter-moshi", "com.squareup.retrofit2", "converter-moshi").versionRef("retrofit")

            library("jsoup", "org.jsoup:jsoup:1.14.3")
            library("play-core", "com.google.android.play:core:1.10.3")
            library("play-core-ktx", "com.google.android.play:core-ktx:1.8.1")
            library("leakcanary", "com.squareup.leakcanary:leakcanary-android:2.8.1")

            // Test
            library("fibelatti-core-test", "com.fibelatti.core", "core-test").versionRef("fibelatti")

            library("junit", "junit:junit:4.13.2")

            version("junit5", "5.8.2")
            library("junit5-api", "org.junit.jupiter", "junit-jupiter-api").versionRef("junit5")
            library("junit5-engine", "org.junit.jupiter", "junit-jupiter-engine").versionRef("junit5")
            library("junit5-params", "org.junit.jupiter", "junit-jupiter-params").versionRef("junit5")
            library("junit5-vintage", "org.junit.vintage", "junit-vintage-engine").versionRef("junit5")

            library("runner", "androidx.test:runner:1.4.0")
            library("truth", "com.google.truth:truth:1.1.3")
            library("mockk", "io.mockk:mockk:1.12.3")
            library("kotlin-test-junit", "org.jetbrains.kotlin", "kotlin-test-junit").versionRef("kotlin")
            library("coroutines-test", "org.jetbrains.kotlinx", "kotlinx-coroutines-test").versionRef("coroutines")
            library("arch-core-testing", "androidx.arch.core:core-testing:2.1.0")
            library("room-testing", "androidx.room", "room-testing").versionRef("room")
        }
    }
}
