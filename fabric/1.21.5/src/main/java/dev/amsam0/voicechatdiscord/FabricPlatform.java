package dev.amsam0.voicechatdiscord;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.ansi.ANSIComponentSerializer;
import net.minecraft.MinecraftVersion;


public class FabricPlatform extends CommonFabricPlatform {
    @Override
    public String getMinecraftVersion() {
        return MinecraftVersion.CURRENT.getName();
    }

    public void info(String message) {
        LOGGER.info(logPrefix + "{}", ansi(mm(message)));
    }

    private String ansi(Component component) {
        return ANSIComponentSerializer.ansi().serialize(component);
    }
}