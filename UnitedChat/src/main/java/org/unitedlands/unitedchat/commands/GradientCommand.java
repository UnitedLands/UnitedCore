package org.unitedlands.unitedchat.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.unitedlands.unitedchat.UnitedChat;
import org.unitedlands.unitedchat.player.ChatPlayer;

import static org.unitedlands.unitedchat.UnitedChat.getMessage;

public class GradientCommand implements CommandExecutor {

    @Nullable
    private static ConfigurationSection getPresetSection() {
        return UnitedChat.getPlugin().getConfig().getConfigurationSection("Presets");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {

        if (!(sender instanceof Player player)) {
            return false;
        }

        Component noPermMessage = getMessage("no-perm");
        if (!player.hasPermission("united.chat.gradient")) {
            player.sendMessage(noPermMessage);
            return false;
        }
        ChatPlayer chatPlayer = new ChatPlayer(player.getUniqueId());

        if (args.length != 1) {
            player.sendMessage(getMessage("gradient-command"));
            return true;
        }

        if (args[0].equalsIgnoreCase("info")) {
            String gradient = chatPlayer.getGradient().replace(":", "\n- ");
            TextReplacementConfig gradientPlaceholder = TextReplacementConfig.builder().match("<gradient>").replacement(gradient).build();
            player.sendMessage(getMessage("current-gradient").replaceText(gradientPlaceholder));
            return true;
        }

        if (args[0].equalsIgnoreCase("on")) {
            if (!chatPlayer.isGradientEnabled()) {
                chatPlayer.setGradientEnabled(true);
                player.sendMessage(getMessage("gradient-on"));
            } else {
                player.sendMessage(getMessage("gradient-is-on"));
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("off")) {
            if (chatPlayer.isGradientEnabled()) {
                chatPlayer.setGradientEnabled(false);
                player.sendMessage(getMessage("gradient-off"));
            } else {
                player.sendMessage(getMessage("gradient-is-off"));
            }
            return true;
        }

        ConfigurationSection presets = getPresetSection();

        if (presets.getString(args[0]) == null) {
            player.sendMessage(getMessage("gradient-unknown-preset"));
            return true;
        }

        if (presets.contains(args[0])) {
            if (player.hasPermission("united.chat.gradient." + args[0])) {
                String preset = presets.getString(args[0]);
                chatPlayer.setGradient(preset);
                player.sendMessage(getMessage("gradient-changed"));
            } else {
                player.sendMessage(noPermMessage);
            }
            return true;
        }

        if (args[0].contains("#")) {
            if (player.hasPermission("unitedchat.gradient.all")) {
                chatPlayer.setGradient(args[0]);
                player.sendMessage(getMessage("gradient-changed"));
                return true;
            }
        } else {
            player.sendMessage(noPermMessage);
            return true;
        }

        return false;
    }

}
