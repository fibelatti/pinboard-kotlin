import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

apply(from = "detekt.gradle")

buildscript {
    extra["compileSdkVersion"] = 31
    extra["targetSdkVersion"] = 31
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
        jcenter {
            content {
                // Remove jcenter once this is migrated
                includeGroup("com.fibelatti.core")
            }
        }
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
