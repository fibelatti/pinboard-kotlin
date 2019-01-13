object Versions {
    internal const val gradlePluginVersion = "3.2.1"
    internal const val kotlinVersion = "1.3.11"
    internal const val dexCountPlugin = "0.8.2"
    internal const val jacocoVersion = "0.8.1"

    const val minSdkVersion = 21
    const val targetSdkVersion = 28
    const val compileSdkVersion = 28

    const val buildToolsVersion = "28.0.3"

    internal const val appCompatVersion = "1.0.2"
    internal const val materialDesignVersion = "1.0.0"
    internal const val supportAnnotationVersion = "1.0.0"
    internal const val constraintLayoutVersion = "2.0.0-alpha1"

    internal const val archComponentsVersion = "2.0.0"

    internal const val coroutinesCoreVersion = "1.0.0"
    internal const val coroutinesAndroidVersion = "1.0.0"

    internal const val daggerVersion = "2.17"

    internal const val moshiVersion = "1.8.0"

    // region Testing
    internal const val jUnitVersion = "4.12"
    internal const val testRunnerVersion = "1.1.0"
    internal const val mockitoVersion = "2.23.0"
    internal const val mockitoAndroidVersion = "2.18.3"
    internal const val junit5Version = "5.3.1"
    // endregion
}

object Classpaths {
    val gradlePlugin = "com.android.tools.build:gradle:${Versions.gradlePluginVersion}"
    val kotlinPlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlinVersion}"
    val jacocoPlugin = "org.jacoco:org.jacoco.core:${Versions.jacocoVersion}"
    val dexCountPlugin = "com.getkeepsafe.dexcount:dexcount-gradle-plugin:${Versions.dexCountPlugin}"
}

object KotlinDependencies {
    val kotlin = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Versions.kotlinVersion}"
    val coroutinesCore = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutinesCoreVersion}"
    val coroutinesAndroid = "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.coroutinesAndroidVersion}"
}

object SupportLibraryDependencies {
    val supportLibrary = "androidx.appcompat:appcompat:${Versions.appCompatVersion}"
    val materialDesign = "com.google.android.material:material:${Versions.materialDesignVersion}"
    val supportAnnotations = "androidx.annotation:annotation:${Versions.supportAnnotationVersion}"
    val constraintLayout = "androidx.constraintlayout:constraintlayout:${Versions.constraintLayoutVersion}"
}

object ArchitectureComponentDependencies {
    val archComponents = "androidx.lifecycle:lifecycle-extensions:${Versions.archComponentsVersion}"
    val archComponentsCompiler = "androidx.lifecycle:lifecycle-compiler:${Versions.archComponentsVersion}"
}

object DIDependencies {
    val dagger = "com.google.dagger:dagger:${Versions.daggerVersion}"
    val daggerCompiler = "com.google.dagger:dagger-compiler:${Versions.daggerVersion}"
}

object ThirdPartyDependencies {
    val moshiKotlin = "com.squareup.moshi:moshi-kotlin:${Versions.moshiVersion}"
}

object TestDependencies {
    val junit = "junit:junit:${Versions.jUnitVersion}"
    val junit5 = "org.junit.jupiter:junit-jupiter-api:${Versions.junit5Version}"
    val junit5Engine = "org.junit.jupiter:junit-jupiter-engine:${Versions.junit5Version}"
    val junit5Params = "org.junit.jupiter:junit-jupiter-params:${Versions.junit5Version}"
    val junitVintage = "org.junit.vintage:junit-vintage-engine:${Versions.junit5Version}"
    val testRunner = "com.android.support.test:runner:${Versions.testRunnerVersion}"
    val kotlinTest = "org.jetbrains.kotlin:kotlin-test-junit:${Versions.kotlinVersion}"
    val mockitoCore = "org.mockito:mockito-core:${Versions.mockitoVersion}"
    val mockitoAndroid = "org.mockito:mockito-android:${Versions.mockitoAndroidVersion}"
    val archComponentsTest = "android.arch.core:core-testing:${Versions.archComponentsVersion}"
}
