package org.justme.justPlugin.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public final class CC {

    private static final MiniMessage MINI = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY_AMP = LegacyComponentSerializer.legacyAmpersand();
    public static final String PREFIX = "<gradient:#00aaff:#00ffaa><bold>JustPlugin</bold></gradient> <dark_gray>» <reset>";

    private CC() {}

    /** Translates MiniMessage tags only. */
    public static Component translate(String miniMessage) {
        return MINI.deserialize(miniMessage);
    }

    /**
     * Translates both legacy &amp; color codes (&amp;1, &amp;a, &amp;l, etc.)
     * AND MiniMessage tags. Useful for player-facing input.
     */
    public static Component colorize(String input) {
        // First pass: convert &x codes to MiniMessage-safe components
        // Convert & codes to § codes, serialize via legacy, then back to MiniMessage string
        Component legacyParsed = LEGACY_AMP.deserialize(input);
        String asMini = MINI.serialize(legacyParsed);
        // Second pass: parse MiniMessage tags that may still exist
        return MINI.deserialize(asMini);
    }

    public static Component prefixed(String miniMessage) {
        return MINI.deserialize(PREFIX + miniMessage);
    }

    public static Component error(String message) {
        return MINI.deserialize(PREFIX + "<red>" + message);
    }

    public static Component success(String message) {
        return MINI.deserialize(PREFIX + "<green>" + message);
    }

    public static Component info(String message) {
        return MINI.deserialize(PREFIX + "<gray>" + message);
    }

    public static Component warning(String message) {
        return MINI.deserialize(PREFIX + "<yellow>" + message);
    }

    /** Continuation line — no prefix, just an indent with ">" marker. */
    public static Component line(String message) {
        return MINI.deserialize(" <dark_gray>></dark_gray> <gray>" + message);
    }

    public static String legacy(Component component) {
        return LegacyComponentSerializer.legacySection().serialize(component);
    }
}

