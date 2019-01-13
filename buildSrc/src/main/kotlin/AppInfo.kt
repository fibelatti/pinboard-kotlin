object AppInfo {
    const val appName = "Pinboard"
    const val applicationId = "com.fibelatti.pinboard"

    private const val versionMajor = 1
    private const val versionMinor = 0
    private const val versionPatch = 0
    private const val versionBuild = 0

    val versionCode: Int = versionMajor * 10000 + versionMinor * 100 + versionPatch + versionBuild

    val versionName: String = "${AppInfo.versionMajor}.${AppInfo.versionMinor}.${AppInfo.versionPatch}"
}
