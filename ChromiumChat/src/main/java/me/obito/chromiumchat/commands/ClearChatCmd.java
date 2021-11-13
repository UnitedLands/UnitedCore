package me.obito.chromiumchat.commands;

import me.obito.chromiumchat.ChromiumChat;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.yaml.snakeyaml.Yaml;

import java.io.File;

public class ClearChatCmd implements CommandExecutor {
    //

        //Bukkit.getPluginManager().getPlugin("ChromiumCore").saveResource(e.getPlayer().getUniqueId() + ".yml", false);

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if(!sender.hasPermission("chromium.chat.admin")) {
            sender.sendMessage(org.bukkit.ChatColor.RED + "You don't have permission.");
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', ChromiumChat.getMessages().getString("NoPerm")));
            return false;
        }

        Bukkit.broadcastMessage(StringUtils.repeat(" \n", 150)+ ChatColor.translateAlternateColorCodes('&', "&b&lGlobal Chat Cleared"));

        return true;
    }

}
