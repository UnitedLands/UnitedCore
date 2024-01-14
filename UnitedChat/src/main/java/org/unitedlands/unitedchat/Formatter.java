package org.unitedlands.unitedchat;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Formatter {

    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final LegacyComponentSerializer sectionRGB = LegacyComponentSerializer.builder().character('§').hexCharacter('#').hexColors().build();
    private final String URL_REGEX = "(https?://|www\\\\.)[-a-zA-Z0-9+&@#/%?=~_|!:.;]*[-a-zA-Z0-9+&@#/%=~_|]";

    public String finalizeMessage(Player player, String message) {
        if (hasURLs(message)) {
            message = highlightURLs(message);
        }
        if (message.contains(":")) {
            message = excludeEmojis(message);
        }
        if (message.contains("[")) {
            message = excludeInteractiveChatDisplays(player, message);
        }
        return message;
    }

    public String colorMessage(String message) {
        Component gradientedComponent = miniMessage.deserialize(message);
        return color(sectionRGB.serialize(gradientedComponent));
    }
    public String gradientMessage(String message, String gradient) {
        Component gradientedComponent = miniMessage.deserialize("<gradient:" + gradient + ">" + message + "</gradient>");
        return color(sectionRGB.serialize(gradientedComponent));
    }

    private String color(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    private String highlightURLs(String message) {
        return message.replaceAll(URL_REGEX,"<click:open_url:'$0'><u><color:#10afee>$0</color:#10afee></u></click>");
    }

    private String excludeEmojis(String message) {
        return message.replaceAll(":[a-zA-Z]+:", "<white>$0</white>");
    }

    private String excludeInteractiveChatDisplays(Player player, String message) {
        return PlaceholderAPI.setPlaceholders(player, message.replaceAll("\\[[^)]*]", "<white>$0</white>"));
    }

    private boolean hasURLs(final String input) {
        final Pattern pattern = Pattern.compile(URL_REGEX, Pattern.CASE_INSENSITIVE);
        final Matcher matcher = pattern.matcher(input);
        return matcher.matches();
    }
}
