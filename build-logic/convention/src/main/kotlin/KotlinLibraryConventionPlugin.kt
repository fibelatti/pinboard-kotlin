import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

@Suppress("Unused")
class KotlinLibraryConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            apply(plugin = versionCatalog.findPluginIdByAlias("fibelatti-spotless"))

            configureKotlinCompilerOptions()
            configureKotlinExplicitApi()
        }
    }

    private fun Project.configureKotlinExplicitApi() {
        extensions.getByType<KotlinProjectExtension>().apply {
            explicitApi()
        }
    }
}

internal fun Project.configureKotlinCompilerOptions() {
    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(JvmTarget.fromTarget(javaVersion.toString()))
            freeCompilerArgs.set(
                buildList {
                    addAll(freeCompilerArgs.get())
                    add("-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi")
                    add("-Xannotation-default-target=param-property")
                },
            )
        }
    }
}
