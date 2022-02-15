package me.obito.chromiumchat.commands;

import me.obito.chromiumchat.ChromiumChat;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class BroadcastCmd implements CommandExecutor {

    String usageAdmin = ChatColor.YELLOW + "Use /cbc <stop> | <text>";

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        ChromiumChat.setConfigBool("BroadcastEnabled", false);

        return false;

    }
}