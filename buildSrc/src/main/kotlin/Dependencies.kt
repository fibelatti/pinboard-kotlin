object Versions {
    const val minSdkVersion = 21
    const val targetSdkVersion = 28
    const val compileSdkVersion = 28
    const val buildToolsVersion = "28.0.3"

    internal const val kotlinVersion = "1.3.11"
    internal const val archComponentsVersion = "2.0.0"
}

object Classpaths {
    val gradlePlugin = "com.android.tools.build:gradle:3.2.1"
    val kotlinPlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlinVersion}"
    val jacocoPlugin = "org.jacoco:org.jacoco.core:0.8.1"
    val dexCountPlugin = "com.getkeepsafe.dexcount:dexcount-gradle-plugin:0.8.2"
}

object Dependencies {
    val kotlin = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Versions.kotlinVersion}"
    val coroutinesCore = "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.0.0"
    val coroutinesAndroid = "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.0.0"

    val supportLibrary = "androidx.appcompat:appcompat:1.0.2"
    val materialDesign = "com.google.android.material:material:1.0.0"
    val supportAnnotations = "androidx.annotation:annotation:1.0.0"
    val constraintLayout = "androidx.constraintlayout:constraintlayout:2.0.0-alpha1"
    val archComponents = "androidx.lifecycle:lifecycle-extensions:${Versions.archComponentsVersion}"
    val archComponentsCompiler = "androidx.lifecycle:lifecycle-compiler:${Versions.archComponentsVersion}"
    val customTabs = "androidx.browser:browser:1.0.0"

    private const val daggerVersion = "2.17"

    val dagger = "com.google.dagger:dagger:$daggerVersion"
    val daggerCompiler = "com.google.dagger:dagger-compiler:$daggerVersion"

    val moshiKotlin = "com.squareup.moshi:moshi-kotlin:1.8.0"

    private const val retrofitVersion = "2.5.0"

    val retrofit = "com.squareup.retrofit2:retrofit:$retrofitVersion"
    val retrofitMoshiConverter = "com.squareup.retrofit2:converter-moshi:$retrofitVersion"
    val httpLoggingInterceptor = "com.squareup.okhttp3:logging-interceptor:3.10.0"
}

object TestDependencies {
    private const val junit5Version = "5.3.1"

    val junit = "junit:junit:4.12"
    val junit5 = "org.junit.jupiter:junit-jupiter-api:$junit5Version"
    val junit5Engine = "org.junit.jupiter:junit-jupiter-engine:$junit5Version"
    val junit5Params = "org.junit.jupiter:junit-jupiter-params:$junit5Version"
    val junitVintage = "org.junit.vintage:junit-vintage-engine:$junit5Version"
    val testRunner = "com.android.support.test:runner:1.1.0"
    val kotlinTest = "org.jetbrains.kotlin:kotlin-test-junit:${Versions.kotlinVersion}"
    val mockitoCore = "org.mockito:mockito-core:2.23.0"
    val mockitoAndroid = "org.mockito:mockito-android:2.18.3"
    val archComponentsTest = "android.arch.core:core-testing:${Versions.archComponentsVersion}"
}
