rootProject.name = "Pinkt"
rootProject.buildFileName = "build.gradle.kts"

include(":app")
include(":core")

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            val kotlinVersion = "1.7.10"
            val coroutinesVersion = "1.6.4"
            val hiltVersion = "2.43.2"
            val lifecycleVersion = "2.5.1"
            val roomVersion = "2.4.3"
            val moshiVersion = "1.14.0"
            val okHttpVersion = "4.10.0"
            val retrofitVersion = "2.9.0"
            val junit5Version = "5.9.0"

            // Classpath
            library("android-gradle-plugin", "com.android.tools.build:gradle:7.2.2")
            library("kotlin-gradle-plugin", "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
            library("hilt-gradle-plugin", "com.google.dagger:hilt-android-gradle-plugin:$hiltVersion")

            // Project dependencies
            library("kotlin", "org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
            library("kotlin-reflect", "org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")

            library("coroutines-core", "org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
            library("coroutines-android", "org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion")

            library("appcompat", "androidx.appcompat:appcompat:1.5.0")
            library("core-ktx", "androidx.core:core-ktx:1.8.0")
            library("activity-ktx", "androidx.activity:activity-ktx:1.5.1")
            library("fragment-ktx", "androidx.fragment:fragment-ktx:1.5.2")
            library("material", "com.google.android.material:material:1.6.1")
            library("constraint-layout", "androidx.constraintlayout:constraintlayout:2.1.4")
            library("swipe-refresh-layout", "androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
            library("lifecycle-java8", "androidx.lifecycle:lifecycle-common-java8:$lifecycleVersion")
            library("lifecycle-runtime-ktx", "androidx.lifecycle:lifecycle-runtime-ktx:$lifecycleVersion")
            library("room-runtime", "androidx.room:room-ktx:$roomVersion")
            library("room-compiler", "androidx.room:room-compiler:$roomVersion")
            library("work-runtime-ktx", "androidx.work:work-runtime-ktx:2.7.1")
            library("browser", "androidx.browser:browser:1.4.0")
            library("viewbinding", "androidx.databinding:viewbinding:7.2.2")

            library("hilt-android", "com.google.dagger:hilt-android:$hiltVersion")
            library("hilt-compiler", "com.google.dagger:hilt-compiler:$hiltVersion")

            library("moshi", "com.squareup.moshi:moshi-kotlin:$moshiVersion")
            library("moshi-codegen", "com.squareup.moshi:moshi-kotlin-codegen:$moshiVersion")

            library("okhttp", "com.squareup.okhttp3:okhttp:$okHttpVersion")
            library("logging-interceptor", "com.squareup.okhttp3:logging-interceptor:$okHttpVersion")

            library("retrofit", "com.squareup.retrofit2:retrofit:$retrofitVersion")
            library("converter-moshi", "com.squareup.retrofit2:converter-moshi:$retrofitVersion")

            library("jsoup", "org.jsoup:jsoup:1.15.3")
            library("play-core", "com.google.android.play:core:1.10.3")
            library("play-core-ktx", "com.google.android.play:core-ktx:1.8.1")
            library("leakcanary", "com.squareup.leakcanary:leakcanary-android:2.9.1")

            // Test
            library("junit", "junit:junit:4.13.2")

            library("junit5-api", "org.junit.jupiter:junit-jupiter-api:$junit5Version")
            library("junit5-engine", "org.junit.jupiter:junit-jupiter-engine:$junit5Version")
            library("junit5-params", "org.junit.jupiter:junit-jupiter-params:$junit5Version")
            library("junit5-vintage", "org.junit.vintage:junit-vintage-engine:$junit5Version")

            library("runner", "androidx.test:runner:1.4.0")
            library("truth", "com.google.truth:truth:1.1.3")
            library("mockk", "io.mockk:mockk:1.12.7")
            library("coroutines-test", "org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutinesVersion")
            library("arch-core-testing", "androidx.arch.core:core-testing:2.1.0")
            library("room-testing", "androidx.room:room-testing:$roomVersion")
        }
    }
}
