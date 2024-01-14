package org.unitedlands.unitedchat.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.unitedlands.unitedchat.player.ChatFeature;
import org.unitedlands.unitedchat.player.ChatPlayer;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.minimessage.tag.resolver.Placeholder.component;
import static org.unitedlands.unitedchat.UnitedChat.getMessage;

public class ChatToggleCommand implements TabExecutor {
    private static final List<String> CHAT_FEATURE_TAB_COMPLETES = Arrays.asList("prefixes", "ranks", "broadcasts", "games", "gradients");
    private static final List<String> TOGGLES = Arrays.asList("on", "off");
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length == 2) {
            return TOGGLES;
        }

        return args.length == 1 ? CHAT_FEATURE_TAB_COMPLETES : Collections.emptyList();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player))  {
            return true;
        }
        if (args.length != 2) {
            player.sendMessage(getMessage("chat-toggle-command"));
            return true;
        }
        ChatFeature feature;
        try {
            feature = ChatFeature.valueOf(args[0].toUpperCase());
        } catch (IllegalArgumentException e) {
            player.sendMessage(getMessage("invalid-feature"));
            return true;
        }
        boolean toggle = args[1].equalsIgnoreCase("on");
        ChatPlayer chatPlayer = new ChatPlayer(player.getUniqueId());
        chatPlayer.toggleChatFeature(feature, toggle);
        player.sendMessage(getMessage("toggled-feature",
                component("feature", text(feature.name().toLowerCase())),
                component("toggle", text(args[1]))));
        return true;
    }

}
