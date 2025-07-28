package dev.amsam0.voicechatdiscord;

import com.mojang.brigadier.context.CommandContext;
import de.maxhenkel.voicechat.api.Player;
import de.maxhenkel.voicechat.api.Position;
import de.maxhenkel.voicechat.api.ServerLevel;
import de.maxhenkel.voicechat.api.ServerPlayer;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minecraft.GameVersion;
import net.minecraft.MinecraftVersion;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.*;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.UUID;

import static dev.amsam0.voicechatdiscord.Core.api;
import static dev.amsam0.voicechatdiscord.Core.platform;
import static dev.amsam0.voicechatdiscord.FabricMod.LOGGER;

public class FabricPlatform implements Platform {
    public boolean isValidPlayer(Object sender) {
        if (sender instanceof CommandContext<?> source)
            return ((ServerCommandSource) source.getSource()).getPlayer() != null;
        return sender != null;
    }

    public ServerPlayer commandContextToPlayer(CommandContext<?> context) {
        return api.fromServerPlayer(((ServerCommandSource) context.getSource()).getPlayer());
    }

    private static boolean canUseGetEntityDirect = true;
    private static Method ServerWorld$getEntity = null;

    public @Nullable Position getEntityPosition(ServerLevel level, UUID uuid) {
        ServerWorld world = (ServerWorld) level.getServerLevel();
        Entity entity = null;
        if (canUseGetEntityDirect) {
            try {
                entity = world.getEntity(uuid);
            } catch (Throwable e) {
                canUseGetEntityDirect = false;
                debug("Couldn't get entity directly: " + e);
                debug(e);
            }
        }
        if (!canUseGetEntityDirect) {
            if (ServerWorld$getEntity == null) {
                ServerWorld$getEntity = Arrays.stream(ServerWorld.class.getMethods())
                        .filter(method -> method.getParameterCount() == 1)
                        .filter(method -> Arrays.equals(method.getParameterTypes(), new Class[]{UUID.class}))
                        .filter(method -> method.getReturnType() == Entity.class)
                        .findFirst()
                        .get();
            }
            try {
                entity = (Entity) ServerWorld$getEntity.invoke(world, uuid);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
        //noinspection DataFlowIssue
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
            player.sendMessage(toNative(mm(message)));
        else if (sender instanceof CommandContext<?> context) {
            ServerCommandSource source = (ServerCommandSource) context.getSource();
            source.sendMessage(toNative(mm(message)));
        } else
            warn("Seems like we are trying to send a message to a sender which was not recognized (it is a " + sender.getClass().getSimpleName() + "). Please report this on GitHub issues!");
    }

    private static boolean canUseSingleArgumentSendMessage = true;

    public void sendMessage(Player player, String message) {
        if (canUseSingleArgumentSendMessage) {
            try {
                ((ServerPlayerEntity) player.getPlayer()).sendMessage(toNative(mm(message)));
            } catch (Throwable ignored) {
                canUseSingleArgumentSendMessage = false;
            }
        }
        if (!canUseSingleArgumentSendMessage) {
            ((ServerPlayerEntity) player.getPlayer()).sendMessage(toNative(mm(message)), false);
        }
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

    @SuppressWarnings("deprecation")
    @Override
    public String getMinecraftVersion() {
        try {
            return MinecraftVersion.CURRENT.name();
        } catch (Throwable ignored) {
            var methods = Arrays.stream(GameVersion.class.getMethods())
                    .filter(m -> m.getParameterCount() == 0 && m.getReturnType() == String.class)
                    .toList();
            for (var method : methods) {
                try {
                    String version = (String) method.invoke(MinecraftVersion.CURRENT);
                    if (version.contains(".")) return version;
                } catch (Throwable ignored2) {
                }
            }
            // Right now, minecraft version is just used for the Modrinth versions link - returning an empty string will work fine
            return "";
        }
    }

    private static final String logPrefix = "[" + Constants.PLUGIN_ID + "] ";

    public void info(String message) {
        LOGGER.info(logPrefix + "{}", ansi(mm(message)));
    }

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

    private static boolean canConstructClickEvent = true;
    private static Method FabricPlatformClickEventHelper$toNative = null;

    private Text toNative(Component component) {
        try {
            MutableText text;
            if (component instanceof TextComponent textComponent) {
                text = Text.literal(textComponent.content());
//                TextContent content;
//                try {
//                    // This should work in >=1.20.3
//                    content = PlainTextContent.of(textComponent.content());
//                    debug("used PlainTextContent");
//                } catch (NoClassDefFoundError ignored) {
//                    // In <=1.20.2, we can try to use reflection
//                    try {
//                        // Try to get the LiteralTextContent class and use its constructor
//                        content = (TextContent) Class
//                                .forName("net.minecraft.class_2585")
//                                .getDeclaredConstructor(String.class)
//                                .newInstance(textComponent.content());
//                        debug("used LiteralTextContent");
//                    } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException |
//                             IllegalAccessException | InvocationTargetException ignored2) {
//                        // If we can't use the official classes, try using our potentially broken TextContent implementation
//                        content = new Literal(textComponent.content());
//                        debug("used scuffed TextContent implementation");
//                    }
//                }
//                text = MutableText.of(content);
            } else {
                warn("Unimplemented component type: " + component.getClass().getName());
                return Text.of(LegacyComponentSerializer.legacySection().serialize(component));
            }

            Style style = Style.EMPTY;

            var font = component.font();
            if (font != null) {
                warn("Fonts are not implemented");
            }

            var color = component.color();
            if (color != null)
                style = style.withColor(TextColor.fromRgb(Integer.parseInt(color.asHexString().substring(1), 16)));

            for (var entry : component.decorations().entrySet()) {
                var decoration = entry.getKey();
                var state = entry.getValue();

                if (state != TextDecoration.State.TRUE)
                    continue;

                switch (decoration) {
                    case OBFUSCATED -> style = style.withObfuscated(true);
                    case BOLD -> style = style.withBold(true);
                    case STRIKETHROUGH -> style = style.withStrikethrough(true);
                    case UNDERLINED -> style = style.withUnderline(true);
                    case ITALIC -> style = style.withItalic(true);
                    default -> warn("Unknown decoration: " + decoration);
                }
            }

            var clickEvent = component.clickEvent();
            if (clickEvent != null) {
                if (canConstructClickEvent) {
                    try {
                        Constructor<ClickEvent> constructor = ClickEvent.class.getConstructor(ClickEvent.Action.class, String.class);
                        ClickEvent.Action action = null;
                        switch (clickEvent.action()) {
                            case OPEN_URL -> action = ClickEvent.Action.OPEN_URL;
                            case OPEN_FILE -> action = ClickEvent.Action.OPEN_FILE;
                            case RUN_COMMAND -> action = ClickEvent.Action.RUN_COMMAND;
                            case SUGGEST_COMMAND -> action = ClickEvent.Action.SUGGEST_COMMAND;
                            case CHANGE_PAGE -> action = ClickEvent.Action.CHANGE_PAGE;
                            case COPY_TO_CLIPBOARD -> action = ClickEvent.Action.COPY_TO_CLIPBOARD;
                            default -> warn("Unknown click event action: " + clickEvent.action());
                        }
                        style = style.withClickEvent(constructor.newInstance(action, clickEvent.value()));
                    } catch (Throwable e) {
                        canConstructClickEvent = false;
                        debug("Constructing click event failed: " + e);
                        debug(e);
                    }
                }
                if (!canConstructClickEvent) {
                    if (FabricPlatformClickEventHelper$toNative == null) {
                        FabricPlatformClickEventHelper$toNative = Class.forName("dev.amsam0.voicechatdiscord.FabricPlatformClickEventHelper").getMethods()[0];
                    }
                    // We can't use action classes such as ClickEvent.OpenUrl in this method directly because those classes will try to load
                    // on earlier minecraft versions (where they don't exist yet). By using them in a separate class, we avoid
                    // loading that class (and thus the action classes) unless we need them
                    style = style.withClickEvent((ClickEvent) FabricPlatformClickEventHelper$toNative.invoke(null, clickEvent));
                }
            }

            var hoverEvent = component.hoverEvent();
            if (hoverEvent != null) {
                warn("Hover events are not implemented");
            }

            var insertion = component.insertion();
            if (insertion != null) {
                warn("Insertions are not implemented");
            }

            text.setStyle(style);
            for (var child : component.children()) {
                text.append(toNative(child));
            }

            return text;
        } catch (Throwable e) {
            warn("Error when converting component to native: " + e);
            debug(e);
            return Text.of(LegacyComponentSerializer.legacySection().serialize(component));
        }
    }

//    private record Literal(String string) implements TextContent {
//        public <T> Optional<T> visit(StringVisitable.Visitor<T> visitor) {
//            return visitor.accept(this.string);
//        }
//
//        @Override
//        public Type<?> getType() {
//            try {
//                return PlainTextContent.TYPE;
//            } catch (NoClassDefFoundError ignored) {
//                return KeybindTextContent.TYPE;
//            }
//        }
//
//        public <T> Optional<T> visit(StringVisitable.StyledVisitor<T> visitor, Style style) {
//            return visitor.accept(style, this.string);
//        }
//
//        public String toString() {
//            return "literal{" + this.string + "}";
//        }
//    }
}