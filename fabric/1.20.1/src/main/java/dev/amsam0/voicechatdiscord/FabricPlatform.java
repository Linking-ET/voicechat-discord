package dev.amsam0.voicechatdiscord;

import net.minecraft.MinecraftVersion;

public class FabricPlatform extends CommonFabricPlatform {
    @Override
    public String getMinecraftVersion() {
        return MinecraftVersion.CURRENT.getName();
    }
}