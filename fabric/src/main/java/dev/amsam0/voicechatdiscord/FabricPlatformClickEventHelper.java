package dev.amsam0.voicechatdiscord;

import net.minecraft.text.ClickEvent;

import java.net.URI;

/** See FabricPlatform#toNative for why we need this */
public class FabricPlatformClickEventHelper {
    public static ClickEvent toNative(net.kyori.adventure.text.event.ClickEvent clickEvent) {
        return switch (clickEvent.action()) {
            case OPEN_URL -> new ClickEvent.OpenUrl(URI.create(clickEvent.value()));
            case OPEN_FILE -> new ClickEvent.OpenFile(clickEvent.value());
            case RUN_COMMAND -> new ClickEvent.RunCommand(clickEvent.value());
            case SUGGEST_COMMAND -> new ClickEvent.SuggestCommand(clickEvent.value());
            case CHANGE_PAGE -> new ClickEvent.ChangePage(Integer.parseInt(clickEvent.value()));
            case COPY_TO_CLIPBOARD -> new ClickEvent.CopyToClipboard(clickEvent.value());
        };
    }
}
