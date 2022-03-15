package org.unitedlands.unitedchat.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.unitedlands.unitedchat.UnitedChat;

import java.io.File;
import java.io.IOException;

public class GradientCmd implements CommandExecutor {
    private final UnitedChat uc;

    public GradientCmd(UnitedChat uc) {
        this.uc = uc;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        FileConfiguration config = uc.getConfig();

        if (!(sender instanceof Player)) {
            return false;
        }

        Player player = (Player) sender;
        ConfigurationSection presets = config.getConfigurationSection("Presets");
        String helpMessage = UnitedChat.getMsg("GradCommand", config);
        String noPermMessage = UnitedChat.getMsg("NoPerm", config);

        if (!player.hasPermission("united.chat.gradient")) {
            player.sendMessage(noPermMessage);
            return false;
        }


        if (args.length != 1) {
            player.sendMessage(helpMessage);
            return true;
        }

        if (args[0].equalsIgnoreCase("info")) {
            String gradient = uc.getPlayerConfig(player).getString("Gradient");
            player.sendMessage(color("&c&lU&f&lL &cChat &8&l» &cYour current gradient is:&f " + gradient));
            return true;
        }
        if (args[0].equalsIgnoreCase("on")) {
            if (!getGradientStatus(player)) {
                setGradientStatus(player, true);
                player.sendMessage(UnitedChat.getMsg("GradientOn", config));
            } else {
                player.sendMessage(UnitedChat.getMsg("GradientIsOn", config));
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("off")) {
            if (getGradientStatus(player)) {
                setGradientStatus(player, false);
                player.sendMessage(UnitedChat.getMsg("GradientOff", config));
            } else {
                player.sendMessage(UnitedChat.getMsg("GradientIsOff", config));
            }
            return true;
        }

        if (presets.getString(args[0]) == null) {
            player.sendMessage(UnitedChat.getMsg("GradientUnknownPreset", config));
        }

        if (presets.contains(args[0])) {
            if (player.hasPermission("united.chat.gradient." + args[0])) {
                String preset = presets.getString(args[0]);
                setGradient(player, preset);
                player.sendMessage(UnitedChat.getMsg("GradientChanged", config));
            } else {
                player.sendMessage(noPermMessage);
            }
            return true;
        }

        if (args[0].contains("#")) {
            if (player.hasPermission("unitedchat.gradient.all")) {
                setGradient(player, args[0]);
                player.sendMessage(UnitedChat.getMsg("GradientChanged", config));
                return true;
            }
        } else {
            player.sendMessage(noPermMessage);
        }


        return false;
    }

    private String color(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    private void setGradient(Player player, String gradient) {
        FileConfiguration playerConfig = uc.getPlayerConfig(player);
        File file = uc.getPlayerFile(player);
        playerConfig.set("Gradient", gradient);
        try {
            playerConfig.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setGradientStatus(Player player, boolean status) {
        FileConfiguration playerConfig = uc.getPlayerConfig(player);
        File file = uc.getPlayerFile(player);
        playerConfig.set("GradientEnabled", status);
        try {
            playerConfig.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean getGradientStatus(Player player) {
        return uc.getPlayerConfig(player).getBoolean("GradientEnabled");
    }

}
