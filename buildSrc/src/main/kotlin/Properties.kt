@Suppress("ConstPropertyName", "MemberVisibilityCanBePrivate")
object Properties {
    const val javaVersion = 21

    /* Project */
    const val pluginVersion = "3.0.11" // Make sure to sync with setup_servers.sh
    const val mavenGroup = "dev.amsam0.voicechatdiscord"
    const val archivesBaseName = "voicechat-discord"
    const val modrinthProjectId = "S1jG5YV5"

    /* Paper */
    const val paperApiVersion = "1.19"
    val paperSupportedMinecraftVersions = listOf(
        // We follow https://modrepo.de/minecraft/voicechat/wiki/supported_versions
        "1.19.2",
        "1.20.1",
        "1.21.1",
        "1.21.4",
        "1.21.5",
        "1.21.8"
    )
    val paperDevBundleVersion = "${paperSupportedMinecraftVersions.last()}-R0.1-SNAPSHOT"
    const val paperAdventureVersion = "4.14.0"

    /* Fabric (https://fabricmc.net/develop) */
    data class FabricMetadata(val yarnMappingsVersion: String, val fabricApiVersion: String, val permissionsApiVersion: String, val adventureVersion: String, val adventurePlatformFabricVersion: String)
    const val fabricLoaderVersion = "0.16.13" // Make sure to sync with setup_servers.sh
    val fabricVersions = mapOf(
        // We follow https://modrepo.de/minecraft/voicechat/wiki/supported_versions
        // See https://github.com/lucko/fabric-permissions-api/releases for permissions API
        // See https://docs.advntr.dev/platform/fabric.html and https://github.com/KyoriPowered/adventure-platform-mod/releases for adventure
        "1.19.2" to FabricMetadata(yarnMappingsVersion = "1.19.2+build.28", fabricApiVersion = "0.77.0+1.19.2", permissionsApiVersion = "0.3.3", adventureVersion = "4.12.0", adventurePlatformFabricVersion = "5.5.2"),
        "1.20.1" to FabricMetadata(yarnMappingsVersion = "1.20.1+build.10", fabricApiVersion = "0.92.6+1.20.1", permissionsApiVersion = "0.3.3", adventureVersion = "4.14.0", adventurePlatformFabricVersion = "5.9.0"),
        "1.21.1" to FabricMetadata(yarnMappingsVersion = "1.21.1+build.3", fabricApiVersion = "0.116.4+1.21.1", permissionsApiVersion = "0.3.3", adventureVersion = "4.17.0", adventurePlatformFabricVersion = "5.14.2"),
        "1.21.4" to FabricMetadata(yarnMappingsVersion = "1.21.4+build.8", fabricApiVersion = "0.119.3+1.21.4", permissionsApiVersion = "0.3.3", adventureVersion = "4.20.0", adventurePlatformFabricVersion = "6.3.0"),
        "1.21.5" to FabricMetadata(yarnMappingsVersion = "1.21.5+build.1", fabricApiVersion = "0.128.1+1.21.5", permissionsApiVersion = "0.3.3", adventureVersion = "4.21.0", adventurePlatformFabricVersion = "6.4.0"),
        "1.21.8" to FabricMetadata(yarnMappingsVersion = "1.21.8+build.1", fabricApiVersion = "0.130.0+1.21.8", permissionsApiVersion = "0.4.1", adventureVersion = "4.23.0", adventurePlatformFabricVersion = "6.5.1")
    )

    /* Dependencies */
    const val voicechatApiVersion = "2.4.11"
    const val yamlConfigurationVersion = "2.0.2"
    const val javaSemverVersion = "0.10.2"
    const val gsonVersion = "2.10.1"
}
