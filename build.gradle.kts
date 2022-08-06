import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

apply(from = "detekt.gradle")

buildscript {
    extra["compileSdkVersion"] = 32
    extra["targetSdkVersion"] = 32
    extra["minSdkVersion"] = 21

    val jacocoEnabled: String? by project
    extra["jacocoEnabled"] = jacocoEnabled?.toBoolean() ?: false

    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        classpath(libs.android.gradle.plugin)
        classpath(libs.kotlin.gradle.plugin)
        classpath(libs.hilt.gradle.plugin)
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

subprojects {
    afterEvaluate {
        tasks.withType<KotlinCompile>().all {
            kotlinOptions.jvmTarget = "11"
        }

        tasks.findByName("preBuild")?.dependsOn(":detekt")
    }
}
