package dev.amsam0.voicechatdiscord;

import com.mojang.brigadier.context.CommandContext;
import de.maxhenkel.voicechat.api.Position;
import de.maxhenkel.voicechat.api.ServerLevel;
import de.maxhenkel.voicechat.api.ServerPlayer;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

import static dev.amsam0.voicechatdiscord.BukkitHelper.getCraftWorld;
import static dev.amsam0.voicechatdiscord.Core.api;
import static dev.amsam0.voicechatdiscord.PaperPlugin.LOGGER;
import static dev.amsam0.voicechatdiscord.PaperPlugin.commandHelper;

public class PaperPlatform implements Platform {
    @Override
    public boolean isValidPlayer(CommandContext<?> sender) {
        return commandHelper.bukkitEntity(sender) instanceof Player;
    }

    @Override
    public ServerPlayer commandContextToPlayer(CommandContext<?> context) {
        return api.fromServerPlayer(commandHelper.bukkitEntity(context));
    }

    private boolean shouldTryMoonrise = true;
    private boolean shouldTryGetBukkitEntityWithoutReflection = true;
    private Method CraftWorld$getHandle;
    private Method ServerLevel$getEntityLookup;
    private Method EntityLookup$get;
    private Method Entity$getBukkitEntity;
    private boolean printedReflectionFail = false;

    private net.minecraft.world.entity.Entity getNmsEntityOld(net.minecraft.server.level.ServerLevel nmsLevel, UUID uuid) {
        try {
            if (ServerLevel$getEntityLookup == null)
                ServerLevel$getEntityLookup = nmsLevel.getClass().getDeclaredMethod("getEntityLookup");

            var entityLookup = ServerLevel$getEntityLookup.invoke(nmsLevel);

            if (EntityLookup$get == null)
                EntityLookup$get = entityLookup.getClass().getDeclaredMethod("get", UUID.class);

            return (net.minecraft.world.entity.Entity) EntityLookup$get.invoke(entityLookup, uuid);
        } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            debug("Failed to get nms entity", e);
            // This should always fail unfortunately, but we don't have any other options
            return nmsLevel.getEntity(uuid);
        }
    }

    private Entity getBukkitEntityReflection(net.minecraft.world.entity.Entity nmsEntity) {
        // Why do we need to use reflection for an unobfuscated method?
        // Good question! Because Bukkit is fundamentally flawed
        // On older versions, CraftEntity is located in something like org.bukkit.craftbukkit.v1_21_R2.entity.CraftEntity
        // Notice the v1_21_R2. This changes between versions, and Paper's change that
        // removes that version tag doesn't apply to older versions
        // That means that java, when internally looking for Entity.getBukkitEntity() is
        // expecting a method that returns org.bukkit.craftbukkit.v1_21_R2.entity.CraftEntity
        // or wherever CraftEntity was during compilation
        // That doesn't always happen
        try {
            if (Entity$getBukkitEntity == null)
                Entity$getBukkitEntity = nmsEntity.getClass().getDeclaredMethod("getBukkitEntity");

            return (Entity) Entity$getBukkitEntity.invoke(nmsEntity);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            if (!printedReflectionFail) {
                error("Failed to get bukkit entity", e);
                printedReflectionFail = true;
            }
            // This will probably fail; see above notes
            return nmsEntity.getBukkitEntity();
        }
    }

    private @Nullable Position getEntityPosition(net.minecraft.server.level.ServerLevel nmsLevel, UUID uuid) {
        net.minecraft.world.entity.Entity nmsEntity = null;
        if (shouldTryMoonrise) {
            try {
                // Works on 1.21+
                nmsEntity = nmsLevel.moonrise$getEntityLookup().get(uuid);
            } catch (NoSuchMethodError ignored) {
                shouldTryMoonrise = false;
                debug("Moonrise failed");
            }
        }
        if (!shouldTryMoonrise) {
            nmsEntity = getNmsEntityOld(nmsLevel, uuid);
        }
        if (nmsEntity == null) return null;

        Entity bukkitEntity = null;
        if (shouldTryGetBukkitEntityWithoutReflection) {
            try {
                bukkitEntity = nmsEntity.getBukkitEntity();
            } catch (NoSuchMethodError ignored) {
                shouldTryGetBukkitEntityWithoutReflection = false;
                debug("Getting bukkit entity without reflection failed");
            }
        }
        if (!shouldTryGetBukkitEntityWithoutReflection) {
            bukkitEntity = getBukkitEntityReflection(nmsEntity);
        }
        if (bukkitEntity == null) return null;

        return api.createPosition(
                // Finally, a method that doesn't check thread safety
                // Thank you Spigot authors
                bukkitEntity.getLocation().getX(),
                bukkitEntity.getLocation().getY(),
                bukkitEntity.getLocation().getZ()
        );
    }

    @Override
    public @Nullable Position getEntityPosition(ServerLevel level, UUID uuid) {
        try {
            if (level.getServerLevel() instanceof World world) {
                // Stupid Bukkit API prevents us from using world.getEntity(uuid) since we aren't on the main thread
                // Using Bukkit.getScheduler().callSyncMethod takes too much time
                // so we are forced to use reflection to get the inner ServerLevel
                // from there we can get Paper's EntityLookup, which allows us to get the entity
                // but wait - we aren't done yet!
                // the NMS Entity getX/Y/Z methods will be obfuscated, which obviously doesn't work well across versions (this is when I wish Paper had support for Fabric's Intermediary mappings, which solves this kind of issue on Fabric)
                // so instead we need to get the Bukkit entity
                // but for some reason getBukkitEntity doesn't exist so instead we cast the CommandSender to an Entity
                // the cast is safe because getBukkitEntity and getBukkitSender return the same thing

                if (CraftWorld$getHandle == null)
                    CraftWorld$getHandle = getCraftWorld().getMethod("getHandle");

                net.minecraft.server.level.ServerLevel nmsLevel = (net.minecraft.server.level.ServerLevel) CraftWorld$getHandle.invoke(world);
                return getEntityPosition(nmsLevel, uuid);
            }
            if (level.getServerLevel() instanceof net.minecraft.server.level.ServerLevel nmsLevel) {
                return getEntityPosition(nmsLevel, uuid);
            }
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException |
                 ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        error("level is not World or ServerLevel, it is " + level.getClass().getSimpleName() + ". Please report this on GitHub Issues!");
        return null;
    }

    @Override
    public boolean isOperator(CommandContext<?> sender) {
        return commandHelper.bukkitSender(sender).isOp();
    }

    @Override
    public boolean hasPermission(CommandContext<?> sender, String permission) {
        return commandHelper.bukkitSender(sender).hasPermission(permission);
    }

    @Override
    public void sendMessage(CommandContext<?> sender, Component... message) {
        if (commandHelper.bukkitEntity(sender) instanceof Player player) {
            player.sendMessage(toNative(message));
        } else {
            commandHelper.bukkitSender(sender).sendMessage(toNative(message));
        }
    }

    @Override
    public void sendMessage(de.maxhenkel.voicechat.api.Player player, Component... message) {
        ((Player) player.getPlayer()).sendMessage(toNative(message));
    }

    public void sendMessage(CommandSender sender, Component... message) {
        sender.sendMessage(toNative(message));
    }

    private net.kyori.adventure.text.Component toNative(Component... message) {
        net.kyori.adventure.text.Component nativeComponent = null;

        for (var component : message) {
            net.kyori.adventure.text.Component mapped = net.kyori.adventure.text.Component.text(
                    component.text(),
                    switch (component.color()) {
                        case WHITE -> NamedTextColor.WHITE;
                        case RED -> NamedTextColor.RED;
                        case YELLOW -> NamedTextColor.YELLOW;
                        case GREEN -> NamedTextColor.GREEN;
                    }
            );
            if (nativeComponent == null) {
                nativeComponent = mapped;
            } else {
                nativeComponent = nativeComponent.append(mapped);
            }
        }

        if (nativeComponent == null) {
            return net.kyori.adventure.text.Component.empty();
        }
        return nativeComponent;
    }

    @Override
    public String getName(de.maxhenkel.voicechat.api.Player player) {
        return ((Player) player.getPlayer()).getName();
    }

    @Override
    public String getConfigPath() {
        return "plugins/voicechat-discord/config.yml";
    }

    @Override
    public Loader getLoader() {
        return Loader.PAPER;
    }

    @Override
    public void info(String message) {
        LOGGER.info(message);
    }

    @Override
    public void warn(String message) {
        LOGGER.warn(message);
    }

    @Override
    public void error(String message) {
        LOGGER.error(message);
    }

    @Override
    public void error(String message, Throwable throwable) {
        LOGGER.error(message, throwable);
    }
}
