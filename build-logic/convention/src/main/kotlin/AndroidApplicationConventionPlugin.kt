import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply

@Suppress("Unused")
class AndroidApplicationConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            apply(plugin = versionCatalog.findPluginIdByAlias("android-application"))
            apply(plugin = versionCatalog.findPluginIdByAlias("fibelatti-android-base"))
            apply(plugin = versionCatalog.findPluginIdByAlias("fibelatti-android-compose"))
            apply(plugin = versionCatalog.findPluginIdByAlias("fibelatti-spotless"))

            configureKotlinCompilerOptions()
        }
    }
}
