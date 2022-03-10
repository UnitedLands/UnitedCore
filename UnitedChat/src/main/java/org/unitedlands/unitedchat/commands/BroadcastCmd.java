package org.unitedlands.unitedchat.commands;

import org.unitedlands.unitedchat.UnitedChat;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class BroadcastCmd implements CommandExecutor {

    String usageAdmin = ChatColor.YELLOW + "Use /cbc <stop> | <text>";

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        UnitedChat.setConfigBool("BroadcastEnabled", false);

        return false;

    }
}