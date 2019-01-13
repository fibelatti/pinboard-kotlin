import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

apply {
    from("ktlint.gradle")
    from("detekt.gradle")
}

buildscript {
    repositories {
        google()
        jcenter()
        mavenCentral()
    }

    dependencies {
        classpath(Classpaths.gradlePlugin)
        classpath(Classpaths.kotlinPlugin)
        classpath(Classpaths.jacocoPlugin)
        classpath(Classpaths.dexCountPlugin)
    }
}

allprojects {
    repositories {
        google()
        jcenter()
        mavenCentral()
        flatDir {
            dirs("libs")
        }
    }
}

tasks.getByName("clean") {
    delete(rootProject.buildDir)
}

gradle.projectsEvaluated {
    subprojects {
        tasks.withType<KotlinCompile>().all {
            kotlinOptions.jvmTarget = "1.8"
        }

        tasks.getByName("preBuild").dependsOn(":detekt", ":ktlint")
    }
}
