import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

apply(from = "detekt.gradle")

buildscript {
    val jacocoEnabled: String? by project
    extra["jacocoEnabled"] = jacocoEnabled?.toBoolean() ?: false

    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        classpath(Classpaths.gradlePlugin)
        classpath(Classpaths.kotlinPlugin)
        classpath(Classpaths.hiltPlugin)
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
