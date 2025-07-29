package dev.amsam0.voicechatdiscord;

import com.github.zafarkhaja.semver.ParseException;
import com.github.zafarkhaja.semver.Version;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static dev.amsam0.voicechatdiscord.Constants.MODRINTH_PROJECT_ID;
import static dev.amsam0.voicechatdiscord.Constants.VERSION;
import static dev.amsam0.voicechatdiscord.PlatformProvider.platform;

public final class UpdateChecker {
    public static @Nullable String updateMessage = null;

    private static final String modrinthVersionPage = "https://modrinth.com/plugin/" + MODRINTH_PROJECT_ID + "/versions?g=" + platform.getMinecraftVersion() + "&l=" + platform.getLoader().modrinthName;

    /**
     * Returns true if successful, false if not successful
     */
    public static boolean checkForUpdate() {
        try {
            List<Version> tags = getTags();
            Version latest = null;
            for (Version tag : tags) {
                if (latest == null || tag.isHigherThan(latest))
                    latest = tag;
            }

            Version current = Version.parse(VERSION);

            platform.debug("Current version is " + current + ", latest version is " + latest);
            if (latest != null) {
                if (latest.isHigherThan(current)) {
                    platform.debug("New update!");
                    platform.debugVerbose("Modrinth version page: " + modrinthVersionPage);
                    String message = "<green>A new version of Simple Voice Chat Discord Bridge is available! " +
                            "You are currently on version <white>" + current +
                            "<green> and the latest version is version <white>" + latest +
                            "<green>. Go to <dark_green><click:open_url:" + modrinthVersionPage + ">" + modrinthVersionPage + "</click><green> to download the update!";
                    platform.info(message);
                    updateMessage = message + " To disable these messages, set `alert_ops_of_updates` to false in the config.";
                }
                return true;
            } else {
                return false;
            }
        } catch (Throwable e) {
            platform.error("Failed to check for update: " + e);
            platform.debug(e);
            return false;
        }
    }

    private static List<Version> getTags() throws Throwable {
        HttpURLConnection connection = null;

        try {
            URL url = new URI("https://api.github.com/repos/amsam0/voicechat-discord/tags").toURL();
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Accept", "application/vnd.github+json");
            connection.setRequestProperty("X-GitHub-Api-Version", "2022-11-28");
            connection.setUseCaches(false);

            InputStream inputStream = connection.getInputStream();
            JsonObject[] jsonTags = new Gson().fromJson(new InputStreamReader(inputStream), JsonObject[].class);
            inputStream.close();

            List<Version> tags = new ArrayList<>();
            for (JsonObject jsonTag : jsonTags) {
                JsonElement jsonName = jsonTag.get("name");
                if (jsonName == null) {
                    platform.debug("Couldn't get name for tag: " + jsonTag);
                    continue;
                }
                try {
                    String name = jsonName.getAsJsonPrimitive().getAsString();
                    try {
                        tags.add(Version.parse(name));
                        platform.debugVerbose("Found tag: " + name);
                    } catch (IllegalArgumentException | ParseException e) {
                        platform.debug("Failed to parse tag: " + name + " with error: " + e);
                        platform.debug(e);
                    }
                } catch (IllegalStateException | AssertionError ignored) {
                    platform.debug("name is not string for tag: " + jsonTag);
                }
            }
            return tags;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
