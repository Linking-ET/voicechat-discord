@Suppress("ConstPropertyName", "MemberVisibilityCanBePrivate")
object Properties {
    const val javaVersion = 21

    /* Project */
    const val pluginVersion = "3.0.11" // Make sure to sync with setup_servers.sh
    const val mavenGroup = "dev.amsam0.voicechatdiscord"
    const val archivesBaseName = "voicechat-discord"
    const val modrinthProjectId = "S1jG5YV5"
    val supportedMinecraftVersions = listOf(
        // We follow https://modrepo.de/minecraft/voicechat/wiki/supported_versions
        "1.19.2",
        "1.20.1",
        "1.21.1",
        "1.21.4",
        "1.21.5",
        "1.21.8"
    )

    val minecraftRequiredVersion = supportedMinecraftVersions.first()
    val minecraftBuildVersion = supportedMinecraftVersions.last()

    /* Paper */
    const val paperApiVersion = "1.19"
    val paperDevBundleVersion = "$minecraftBuildVersion-R0.1-SNAPSHOT"

    /* Fabric (https://fabricmc.net/develop) */
    const val fabricLoaderVersion = "0.16.13" // Make sure to sync with setup_servers.sh
    val yarnMappingsDevVersion = "$minecraftBuildVersion+build.1"
    val fabricApiDevVersion = "0.130.0+$minecraftBuildVersion"

    /* Dependencies */
    const val voicechatApiVersion = "2.4.11"
    const val yamlConfigurationVersion = "2.0.2"
    const val javaSemverVersion = "0.10.2"
    const val gsonVersion = "2.10.1"
    const val adventureVersion = "4.14.0"

    /* Gradle Plugins */
    const val minotaurVersion = "2.+"
    const val shadowVersion = "8.1.1"
}
