package org.unitedlands.unitedchat.commands;

import org.unitedlands.unitedchat.UnitedChat;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ClearChatCmd implements CommandExecutor {

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if(!sender.hasPermission("united.chat.admin")) {
            sender.sendMessage(
                    ChatColor.translateAlternateColorCodes('&', UnitedChat.getGlobalMsg("NoPerm")));
            return false;
        }

        Bukkit.broadcastMessage(StringUtils.repeat(" \n", 150)+ ChatColor.translateAlternateColorCodes('&', UnitedChat.getMsg("ChatCleared")));

        return true;
    }

}
