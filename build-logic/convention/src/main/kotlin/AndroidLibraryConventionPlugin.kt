import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply

@Suppress("Unused")
class AndroidLibraryConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            apply(plugin = versionCatalog.findPluginIdByAlias("android-library"))
            apply(plugin = versionCatalog.findPluginIdByAlias("fibelatti-android-base"))
            apply(plugin = versionCatalog.findPluginIdByAlias("fibelatti-kotlin-library"))
        }
    }
}
