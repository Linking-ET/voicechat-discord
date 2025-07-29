package dev.amsam0.voicechatdiscord;

import com.mojang.brigadier.context.CommandContext;
import de.maxhenkel.voicechat.api.Player;
import de.maxhenkel.voicechat.api.Position;
import de.maxhenkel.voicechat.api.ServerLevel;
import de.maxhenkel.voicechat.api.ServerPlayer;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

import static dev.amsam0.voicechatdiscord.Constants.PLUGIN_ID;
import static dev.amsam0.voicechatdiscord.Core.api;

public abstract class CommonFabricPlatform implements Platform {
    public static final Logger LOGGER = LoggerFactory.getLogger(PLUGIN_ID);

    public boolean isValidPlayer(Object sender) {
        if (sender instanceof CommandContext<?> source)
            return ((ServerCommandSource) source.getSource()).getPlayer() != null;
        return sender != null;
    }

    public ServerPlayer commandContextToPlayer(CommandContext<?> context) {
        return api.fromServerPlayer(((ServerCommandSource) context.getSource()).getPlayer());
    }

    public @Nullable Position getEntityPosition(ServerLevel level, UUID uuid) {
        ServerWorld world = (ServerWorld) level.getServerLevel();
        Entity entity = world.getEntity(uuid);
        if (entity == null) {
            return null;
        }
        return api.createPosition(
                entity.getX(),
                entity.getY(),
                entity.getZ()
        );
    }

    @SuppressWarnings("DataFlowIssue")
    public boolean isOperator(Object sender) {
        if (sender instanceof CommandContext<?> source)
            return ((ServerCommandSource) source.getSource()).hasPermissionLevel(2);
        if (sender instanceof ServerPlayerEntity player)
            // player.hasPermissionLevel doesn't exist on 1.19.2
            return player.getServer().getPermissionLevel(player.getGameProfile()) >= 2;

        return false;
    }

    public boolean hasPermission(Object sender, String permission) {
        if (sender instanceof CommandContext<?> source)
            return Permissions.check((ServerCommandSource) source.getSource(), permission);
        if (sender instanceof ServerPlayerEntity player)
            return Permissions.check(player, permission);

        return false;
    }

    public void sendMessage(Object sender, String message) {
        if (sender instanceof ServerPlayerEntity player)
            player.sendMessage(mm(message));
        else if (sender instanceof CommandContext<?> context) {
            ServerCommandSource source = (ServerCommandSource) context.getSource();
            source.sendMessage(mm(message));
        } else
            warn("Seems like we are trying to send a message to a sender which was not recognized (it is a " + sender.getClass().getSimpleName() + "). Please report this on GitHub issues!");
    }

    public void sendMessage(Player player, String message) {
        ((ServerPlayerEntity) player.getPlayer()).sendMessage(mm(message));
    }

    public String getName(Player player) {
        return ((PlayerEntity) player.getPlayer()).getName().getString();
    }

    public String getConfigPath() {
        return "config/voicechat-discord.yml";
    }

    public Loader getLoader() {
        return Loader.FABRIC;
    }

    protected static final String logPrefix = "[" + Constants.PLUGIN_ID + "] ";

    public void infoRaw(String message) {
        LOGGER.info(logPrefix + "{}", message);
    }

    // warn and error will already be colored yellow and red respectfully

    public void warn(String message) {
        LOGGER.warn(logPrefix + "{}", message);
    }

    public void error(String message) {
        LOGGER.error(logPrefix + "{}", message);
    }

    protected Component mm(String message) {
        return MiniMessage.miniMessage().deserialize(message);
    }
}