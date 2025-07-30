package dev.amsam0.voicechatdiscord;

import com.github.zafarkhaja.semver.ParseException;
import com.github.zafarkhaja.semver.Version;
import de.maxhenkel.voicechat.api.BukkitVoicechatService;
import dev.amsam0.voicechatdiscord.post_1_20_6.Post_1_20_6_CommandHelper;
import dev.amsam0.voicechatdiscord.pre_1_20_6.Pre_1_20_6_CommandHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import static dev.amsam0.voicechatdiscord.Constants.PLUGIN_ID;
import static dev.amsam0.voicechatdiscord.Core.*;

public final class PaperPlugin extends JavaPlugin implements Listener {
    public static final Logger LOGGER = LogManager.getLogger(PLUGIN_ID);
    public static PaperPlugin INSTANCE;
    public static CommandHelper commandHelper;

    private PaperVoicechatPlugin voicechatPlugin;

    public static PaperPlugin get() {
        return INSTANCE;
    }

    @Override
    public void onEnable() {
        INSTANCE = this;

        if (platform == null) {
            platform = new PaperPlatform();
        }

        try {
            var parsed = Version.parse(getServer().getMinecraftVersion(), false);

            var wantedCommandHelper = Version.of(1, 20, 6);
            if (parsed.isHigherThanOrEquivalentTo(wantedCommandHelper)) {
                platform.info("Server is >=1.20.6");
                commandHelper = new Post_1_20_6_CommandHelper();
            } else {
                platform.info("Server is <1.20.6");
                commandHelper = new Pre_1_20_6_CommandHelper();
            }
        } catch (IllegalArgumentException | ParseException e) {
            var v = getServer().getMinecraftVersion();
            platform.error("Unable to parse server version (" + v + ")", e);

            if (v.equals("1.19.4") ||
                    v.equals("1.20") ||
                    v.equals("1.20.0") ||
                    v.equals("1.20.1") ||
                    v.equals("1.20.2") ||
                    v.equals("1.20.3") ||
                    v.equals("1.20.4") ||
                    v.equals("1.20.5")
            ) {
                platform.info("Server is most likely <1.20.6");
                commandHelper = new Pre_1_20_6_CommandHelper();
            } else {
                platform.info("Server is most likely >=1.20.6");
                commandHelper = new Post_1_20_6_CommandHelper();
            }
        }

        BukkitVoicechatService service = getServer().getServicesManager().load(BukkitVoicechatService.class);
        if (service != null) {
            voicechatPlugin = new PaperVoicechatPlugin();
            service.registerPlugin(voicechatPlugin);
            LOGGER.info("Successfully registered voicechat discord plugin");
        } else {
            LOGGER.error("Failed to register voicechat discord plugin");
            throw new RuntimeException("Failed to register voicechat discord plugin");
        }

        enable();

        Plugin svcPlugin = getServer().getPluginManager().getPlugin("voicechat");
        //noinspection deprecation
        checkSVCVersion(svcPlugin != null ? svcPlugin.getDescription().getVersion() : null);

        Bukkit.getPluginManager().registerEvents(this, this);

        commandHelper.registerCommands();
    }

    @Override
    public void onDisable() {
        disable();

        if (voicechatPlugin != null) {
            getServer().getServicesManager().unregister(voicechatPlugin);
            LOGGER.info("Successfully unregistered voicechat discord plugin");
        }
    }

    @EventHandler
    public void playerLeave(PlayerQuitEvent e) {
        onPlayerLeave(e.getPlayer().getUniqueId());
    }
}
