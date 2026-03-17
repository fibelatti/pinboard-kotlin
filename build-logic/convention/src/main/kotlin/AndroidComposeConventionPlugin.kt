import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension

@Suppress("Unused")
class AndroidComposeConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            apply(plugin = versionCatalog.findPluginIdByAlias("compose-compiler"))

            extensions.getByType<CommonExtension>().apply {
                buildFeatures.apply {
                    compose = true
                }
            }

            extensions.findByType(ComposeCompilerGradlePluginExtension::class.java)?.apply {
                stabilityConfigurationFiles.add(rootProject.layout.projectDirectory.file("compose_compiler_config.conf"))

                if (project.findProperty("composeCompilerReports") == "true") {
                    val destinationDir = project.layout.buildDirectory.dir("compose_compiler")
                    reportsDestination.set(destinationDir)
                    metricsDestination.set(destinationDir)
                }
            }

            dependencies {
                val bom = versionCatalog.findLibrary("compose-bom").get()
                implementation(platform(bom))
                implementation("compose-runtime")
                implementation("compose-material3")
                implementation("compose-ui")
                implementation("compose-ui-tooling-preview")
                debugImplementation("compose-ui-tooling")
            }
        }
    }
}
