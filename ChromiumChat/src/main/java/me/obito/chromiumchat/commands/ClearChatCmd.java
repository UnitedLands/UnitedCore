package me.obito.chromiumchat.commands;

import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ClearChatCmd implements CommandExecutor {
    //
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(!sender.hasPermission("chromium.chat.admin")) {
            sender.sendMessage(org.bukkit.ChatColor.RED + "You don't have permission.");
            return false;
        }

        Bukkit.broadcastMessage(StringUtils.repeat(" \n", 150)+ ChatColor.translateAlternateColorCodes('&', "&b&lGlobal Chat Cleared"));

        return true;
    }

}
