rootProject.name = "Pinkt"
rootProject.buildFileName = "build.gradle.kts"

include(":app")

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            // Classpath
            library("android-gradle-plugin", "com.android.tools.build:gradle:7.1.3")
            library("kotlin-gradle-plugin", "org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.10")
            library("hilt-gradle-plugin", "com.google.dagger:hilt-android-gradle-plugin:2.40.5")

            // Project dependencies
            library("kotlin", "org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.6.10")
            library("kotlin-reflect", "org.jetbrains.kotlin:kotlin-reflect:1.6.10")

            library("coroutines-core", "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")
            library("coroutines-android", "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.0")

            library("appcompat", "androidx.appcompat:appcompat:1.4.1")
            library("core-ktx", "androidx.core:core-ktx:1.7.0")
            library("activity-ktx", "androidx.activity:activity-ktx:1.4.0")
            library("fragment-ktx", "androidx.fragment:fragment-ktx:1.4.1")
            library("annotations", "androidx.annotation:annotation:1.3.0")
            library("material", "com.google.android.material:material:1.5.0")
            library("constraint-layout", "androidx.constraintlayout:constraintlayout:2.1.3")
            library("swipe-refresh-layout", "androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
            library("fibelatti-core", "com.fibelatti.core:core:2.0.0-alpha4")
            library("fibelatti-arch-components", "com.fibelatti.core:arch-components:2.0.0-alpha4")
            library("lifecycle-java8", "androidx.lifecycle:lifecycle-common-java8:2.4.1")
            library("lifecycle-runtime-ktx", "androidx.lifecycle:lifecycle-runtime-ktx:2.4.1")
            library("room-runtime", "androidx.room:room-runtime:2.4.0")
            library("room-compiler", "androidx.room:room-compiler:2.4.0")
            library("work-runtime-ktx", "androidx.work:work-runtime-ktx:2.7.1")
            library("browser", "androidx.browser:browser:1.4.0")
            library("hilt-android", "com.google.dagger:hilt-android:2.40.5")
            library("hilt-compiler", "com.google.dagger:hilt-compiler:2.40.5")

            library("moshi", "com.squareup.moshi:moshi-kotlin:1.13.0")
            library("moshi-codegen", "com.squareup.moshi:moshi-kotlin-codegen:1.13.0")

            library("okhttp", "com.squareup.okhttp3:okhttp:4.9.3")
            library("logging-interceptor", "com.squareup.okhttp3:logging-interceptor:4.9.3")

            library("retrofit", "com.squareup.retrofit2:retrofit:2.9.0")
            library("converter-moshi", "com.squareup.retrofit2:converter-moshi:2.9.0")

            library("jsoup", "org.jsoup:jsoup:1.14.3")
            library("play-core", "com.google.android.play:core:1.10.3")
            library("play-core-ktx", "com.google.android.play:core-ktx:1.8.1")
            library("leakcanary", "com.squareup.leakcanary:leakcanary-android:2.8.1")

            // Test
            library("fibelatti-core-test", "com.fibelatti.core:core-test:2.0.0-alpha4")

            library("junit", "junit:junit:4.13.2")

            library("junit5-api", "org.junit.jupiter:junit-jupiter-api:5.8.2")
            library("junit5-engine", "org.junit.jupiter:junit-jupiter-engine:5.8.2")
            library("junit5-params", "org.junit.jupiter:junit-jupiter-params:5.8.2")
            library("junit5-vintage", "org.junit.vintage:junit-vintage-engine:5.8.2")

            library("runner", "androidx.test:runner:1.4.0")
            library("truth", "com.google.truth:truth:1.1.3")
            library("mockk", "io.mockk:mockk:1.12.3")
            library("kotlin-test-junit", "org.jetbrains.kotlin:kotlin-test-junit:1.6.10")
            library("coroutines-test", "org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.0")
            library("arch-core-testing", "androidx.arch.core:core-testing:2.1.0")
            library("room-testing", "androidx.room:room-testing:2.4.2")
        }
    }
}
