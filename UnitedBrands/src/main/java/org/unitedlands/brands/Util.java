package org.unitedlands.brands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;

public class Util {
    private static final MiniMessage miniMessage = MiniMessage.miniMessage();

    public static Component getMessage(String message) {
        String prefix = UnitedBrands.getInstance().getConfig().getString("messages.prefix");
        message = UnitedBrands.getInstance().getConfig().getString("messages." + message);
        return miniMessage.deserialize(prefix + message);
    }

    public static Component getNoPrefixMessage(String message) {
        message = UnitedBrands.getInstance().getConfig().getString("messages." + message);
        return miniMessage.deserialize(message);
    }

    public static Component getMessage(String message, String breweryName) {
        TextReplacementConfig breweryReplacer = TextReplacementConfig.builder()
                .match("<brewery>")
                .replacement(breweryName)
                .build();
        return getMessage(message).replaceText(breweryReplacer);
    }

    public static Component getMessage(String message, Player player) {
        TextReplacementConfig playerReplacer = TextReplacementConfig.builder()
                .match("<player>")
                .replacement(player.getName())
                .build();
        return getMessage(message).replaceText(playerReplacer);
    }

    public static Component getMessage(String message, String breweryName, Player player) {
        TextReplacementConfig playerReplacer = TextReplacementConfig.builder()
                .match("<player>")
                .replacement(player.getName())
                .build();
        return getMessage(message, breweryName).replaceText(playerReplacer);
    }

}
