package dev.amsam0.voicechatdiscord;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;
import java.util.function.Consumer;

public class EventListener implements Listener {
    public static Consumer<UUID> onPlayerLeaveHandler = (ignored) -> {};

    @EventHandler
    public void playerLeave(PlayerQuitEvent e) {
        onPlayerLeaveHandler.accept(e.getPlayer().getUniqueId());
    }
}
