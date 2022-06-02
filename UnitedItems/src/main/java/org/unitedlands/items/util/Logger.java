package org.unitedlands.items.util;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class Logger {
    public static void log(String msg) {
        msg = ChatColor.translateAlternateColorCodes('&', "&b[&c&lUnited&f&lItems&b]&r " + msg);
        Bukkit.getConsoleSender().sendMessage(msg);
    }

    public static void logNoPrefix(Player p, String msg) {
        if(p == null)
            return;
        msg = ChatColor.translateAlternateColorCodes('&', msg);
        p.sendMessage(msg);
    }
}
