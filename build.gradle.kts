import com.android.build.api.dsl.CommonExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

apply(from = "detekt.gradle")

buildscript {
    extra["compileSdkVersion"] = 33
    extra["targetSdkVersion"] = 33
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
        extensions.findByType(CommonExtension::class.java)?.apply {
            compileOptions {
                sourceCompatibility(JavaVersion.VERSION_11)
                targetCompatibility(JavaVersion.VERSION_11)
            }

            testOptions {
                animationsDisabled = true

                unitTests {
                    isReturnDefaultValues = true
                    isIncludeAndroidResources = true

                    all {
                        it.useJUnitPlatform()
                    }
                }
            }
        }

        tasks.withType<KotlinCompile>().all {
            kotlinOptions.apply {
                jvmTarget = "11"
                freeCompilerArgs = freeCompilerArgs + "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi"
            }
        }

        tasks.findByName("preBuild")?.dependsOn(":detekt")
    }
}
