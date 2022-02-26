object Versions {
    const val minSdkVersion = 21
    const val targetSdkVersion = 31
    const val compileSdkVersion = 31

    internal const val kotlinVersion = "1.5.21"
    internal const val coroutinesVersion = "1.5.1"
    internal const val lifecycleVersion = "2.2.0"
    internal const val roomVersion = "2.3.0"
}

object Classpaths {
    const val gradlePlugin = "com.android.tools.build:gradle:7.1.2"
    const val kotlinPlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlinVersion}"
    const val hiltPlugin = "com.google.dagger:hilt-android-gradle-plugin:2.38.1"
}

object Dependencies {
    const val kotlin = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Versions.kotlinVersion}"
    const val kotlinReflect = "org.jetbrains.kotlin:kotlin-reflect:${Versions.kotlinVersion}"
    const val coroutinesCore = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutinesVersion}"
    const val coroutinesAndroid = "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.coroutinesVersion}"

    const val supportLibrary = "androidx.appcompat:appcompat:1.3.1"
    const val androidCore = "androidx.core:core-ktx:1.6.0"
    const val activity = "androidx.activity:activity-ktx:1.3.1"
    const val fragments = "androidx.fragment:fragment-ktx:1.3.6"
    const val supportAnnotations = "androidx.annotation:annotation:1.2.0"
    const val materialDesign = "com.google.android.material:material:1.4.0"
    const val constraintLayout = "androidx.constraintlayout:constraintlayout:2.1.0"
    const val swipeRefreshLayout = "androidx.swiperefreshlayout:swiperefreshlayout:1.1.0"

    const val coreLib = "com.fibelatti.core:core:2.0.0-alpha4"
    const val coreLibArch = "com.fibelatti.core:arch-components:2.0.0-alpha4"

    const val lifecycleJava8 = "androidx.lifecycle:lifecycle-common-java8:${Versions.lifecycleVersion}"

    const val archComponents = "androidx.lifecycle:lifecycle-extensions:${Versions.lifecycleVersion}"
    const val archComponentsCompiler = "androidx.lifecycle:lifecycle-compiler:${Versions.lifecycleVersion}"

    const val room = "androidx.room:room-runtime:${Versions.roomVersion}"
    const val roomCompiler = "androidx.room:room-compiler:${Versions.roomVersion}"

    const val workManager = "androidx.work:work-runtime-ktx:2.5.0"

    const val customTabs = "androidx.browser:browser:1.3.0"

    const val hilt = "com.google.dagger:hilt-android:2.38.1"
    const val hiltCompiler = "com.google.dagger:hilt-compiler:2.38.1"

    const val moshi = "com.squareup.moshi:moshi-kotlin:1.12.0"
    const val moshiCodeGen = "com.squareup.moshi:moshi-kotlin-codegen:1.12.0"

    private const val okHttpVersion = "4.9.0"
    const val okhttp = "com.squareup.okhttp3:okhttp:$okHttpVersion"
    const val httpLoggingInterceptor = "com.squareup.okhttp3:logging-interceptor:$okHttpVersion"

    private const val retrofitVersion = "2.9.0"
    const val retrofit = "com.squareup.retrofit2:retrofit:$retrofitVersion"
    const val retrofitConverter = "com.squareup.retrofit2:converter-moshi:$retrofitVersion"

    const val jsoup = "org.jsoup:jsoup:1.13.1"

    const val playCore = "com.google.android.play:core-ktx:1.8.1"

    const val leakCanary = "com.squareup.leakcanary:leakcanary-android:2.6"
}

object TestDependencies {

    const val coreLibTest = "com.fibelatti.core:core-test:2.0.0-alpha4"

    private const val junit5Version = "5.7.2"
    const val junit = "junit:junit:4.13"
    const val junit5 = "org.junit.jupiter:junit-jupiter-api:$junit5Version"
    const val junit5Engine = "org.junit.jupiter:junit-jupiter-engine:$junit5Version"
    const val junit5Params = "org.junit.jupiter:junit-jupiter-params:$junit5Version"
    const val junitVintage = "org.junit.vintage:junit-vintage-engine:$junit5Version"

    const val testRunner = "androidx.test:runner:1.4.0"

    const val googleTruth = "com.google.truth:truth:1.1.3"
    const val mockk = "io.mockk:mockk:1.12.0"

    const val kotlinTest = "org.jetbrains.kotlin:kotlin-test-junit:${Versions.kotlinVersion}"
    const val coroutinesTest = "org.jetbrains.kotlinx:kotlinx-coroutines-test:${Versions.coroutinesVersion}"
    const val archComponentsTest = "androidx.arch.core:core-testing:2.1.0"
    const val roomTest = "androidx.room:room-testing:${Versions.roomVersion}"
}
