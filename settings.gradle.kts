pluginManagement {
    repositories {
        gradlePluginPortal()
        maven { url = uri("https://maven.fabricmc.net/") }
        maven { url = uri("https://repo.papermc.io/repository/maven-public/") }
    }
}

rootProject.name = "voicechat-discord"
include(
    "core",
    "paper",
    "fabric",

    "fabric:1.19.2",
    "fabric:1.20.1",
    "fabric:1.21.1",
    "fabric:1.21.4",
    "fabric:1.21.5",
    "fabric:1.21.8"
)
