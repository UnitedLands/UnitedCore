package org.unitedlands.alcohol;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

public class Util {
    private static final FileConfiguration CONFIG = Bukkit.getPluginManager().getPlugin("UnitedBrands").getConfig();

    public static String getMessage(String message, String brandName) {
        return color(CONFIG.getString("message." + message).replace("<brand>", brandName));
    }

    private static String color(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
