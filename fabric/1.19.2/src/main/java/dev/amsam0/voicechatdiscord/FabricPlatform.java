package dev.amsam0.voicechatdiscord;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.minecraft.MinecraftVersion;

public class FabricPlatform extends CommonFabricPlatform {
    @Override
    public String getMinecraftVersion() {
        return MinecraftVersion.CURRENT.getName();
    }

    public void info(String message) {
        LOGGER.info(logPrefix + "{}", plain(mm(message)));
    }

    private String plain(Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }
}