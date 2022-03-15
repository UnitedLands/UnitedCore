package org.unitedlands.unitedchat.commands;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ClearChatCmd implements CommandExecutor {

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!sender.hasPermission("united.chat.admin")) {
            sender.sendMessage("You don't have permission to execute this command!");
            return false;
        }

        Bukkit.broadcastMessage(StringUtils.repeat(" \n", 150));

        return true;
    }

}
