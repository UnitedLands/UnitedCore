package me.obito.chromiumchat.commands;

import me.obito.chromiumchat.ChromiumChat;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.yaml.snakeyaml.Yaml;

import java.io.File;

public class ClearChatCmd implements CommandExecutor {

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if(!sender.hasPermission("chromium.chat.admin")) {
            sender.sendMessage(
                    ChatColor.translateAlternateColorCodes('&', ChromiumChat.getGlobalMsg("NoPerm")));
            return false;
        }

        Bukkit.broadcastMessage(StringUtils.repeat(" \n", 150)+ ChatColor.translateAlternateColorCodes('&', ChromiumChat.getMsg("ChatCleared")));

        return true;
    }

}
