package me.obito.chromiumpvp.util;

import me.obito.chromiumpvp.ChromiumPvP;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;

public class Utils {

    public static void setPvPStatus(Player player, boolean bool) {

        File customConfigFile = new File(ChromiumPvP.chromiumFinal.getDataFolder(),
                "/players/" + player.getUniqueId() + ".yml");

        FileConfiguration playerConfig;
        playerConfig = new YamlConfiguration();
        try {
            playerConfig.load(customConfigFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
        playerConfig.set("PvP", bool);
        try {
            playerConfig.save(customConfigFile);
        } catch (Exception e1) {
            System.out.println("Error with saving configuration for player " + player.getName());
            player.sendMessage("Error with saving configuration.");
        }


    }

    public static boolean getPvPStatus(Player player) {

        boolean isCitizensNPC = player.hasMetadata("NPC");

        if (isCitizensNPC) {
            return false;
        }

        File customConfigFile = new File(ChromiumPvP.chromiumFinal.getDataFolder(),
                "/players/" + player.getUniqueId() + ".yml");

        FileConfiguration playerConfig;
        playerConfig = new YamlConfiguration();
        try {
            playerConfig.load(customConfigFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return playerConfig.getBoolean("PvP");


    }

    public static String color(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

}
