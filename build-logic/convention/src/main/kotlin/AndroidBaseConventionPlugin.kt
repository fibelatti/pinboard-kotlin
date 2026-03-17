import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.maybeCreate
import org.gradle.kotlin.dsl.provideDelegate

@Suppress("Unused")
class AndroidBaseConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            apply(plugin = versionCatalog.findPluginIdByAlias("cache-fix"))

            configureSdkCompatibility()
            configureCommonDependencies()
            configureCoreLibraryDesugaring()
            configureAndroidTestOptions()
        }
    }

    private fun Project.configureSdkCompatibility() {
        val compileSdkVersion: Int by this
        val minSdkVersion: Int by this

        extensions.getByType<CommonExtension>().apply {
            compileSdk = compileSdkVersion

            defaultConfig.apply {
                minSdk = minSdkVersion
            }

            compileOptions.apply {
                sourceCompatibility(javaVersion)
                targetCompatibility(javaVersion)
            }
        }
    }

    private fun Project.configureCommonDependencies() {
        dependencies {
            implementation("kotlin")
            implementation("coroutines-core")

            implementation("core-ktx")
        }
    }

    private fun Project.configureCoreLibraryDesugaring() {
        extensions.getByType<CommonExtension>().apply {
            compileOptions.apply {
                isCoreLibraryDesugaringEnabled = true
            }
        }

        dependencies {
            "coreLibraryDesugaring"(versionCatalog.findLibrary("core-library-desugaring").get())
        }
    }

    @Suppress("UnstableApiUsage")
    private fun Project.configureAndroidTestOptions() {
        extensions.getByType<CommonExtension>().apply {
            testOptions.apply {
                animationsDisabled = true

                unitTests {
                    all {
                        it.useJUnitPlatform()
                    }
                }

                managedDevices {
                    allDevices {
                        maybeCreate<com.android.build.api.dsl.ManagedVirtualDevice>("pixel8api34").apply {
                            device = "Pixel 8"
                            apiLevel = 34
                            systemImageSource = "google"
                        }
                    }
                }
            }
        }
    }
}
