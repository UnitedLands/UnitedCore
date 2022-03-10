package org.unitedlands.unitedpvp.util;

import org.unitedlands.unitedpvp.UnitedPvP;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.projectiles.ProjectileSource;

import java.io.File;
import java.util.logging.Logger;

import static java.util.logging.Logger.getLogger;

public class Utils {


    public static void setPvPStatus(Player player, boolean bool) {

        Logger logger = getLogger("UnitedPvP");

        File customConfigFile = new File(UnitedPvP.unitedCore.getDataFolder(),
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
            logger.warning("Error with saving configuration for player " + player.getName());
            player.sendMessage("Error with saving configuration.");
        }


    }

    public static boolean getPvPStatus(Player player) {

        boolean isCitizensNPC = player.hasMetadata("NPC");

        if (isCitizensNPC) {
            return false;
        }

        File customConfigFile = new File(UnitedPvP.unitedCore.getDataFolder(),
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

    public static String getGlobalMsg(String s) {
        Logger logger = getLogger("UnitedPvP");
        File customConfigFile;
        customConfigFile = new File(UnitedPvP.unitedCore.getDataFolder(), "messages.yml");
        FileConfiguration customConfig;
        customConfig = new YamlConfiguration();
        try {
            customConfig.load(customConfigFile);
        } catch (Exception e2) {
            logger.warning("Error with loading messages.");
        }

        return customConfig.getConfigurationSection("Global").getString(s);
    }

    public static String getMsg(String s) {
        Logger logger = getLogger("UnitedPvP");
        File customConfigFile;
        customConfigFile = new File(UnitedPvP.unitedCore.getDataFolder(), "messages.yml");
        FileConfiguration customConfig;
        customConfig = new YamlConfiguration();
        try {
            customConfig.load(customConfigFile);
        } catch (Exception e2) {
            logger.warning("Error with loading messages.");
        }

        return customConfig.getConfigurationSection("PvP").getString(s);

    }

    public static boolean isPvP(final EntityDamageByEntityEvent event) {
        final Entity damager = event.getDamager();
        final Entity target = event.getEntity();

        if (target instanceof Player && !target.hasMetadata("NPC")) {
            if (damager instanceof Player && !damager.hasMetadata("NPC"))
                return true;
            if (damager instanceof Projectile) {
                final ProjectileSource projSource = ((Projectile) damager).getShooter();
                if (projSource instanceof Player) {
                    final Entity shooter = (Entity) projSource;
                    if (!shooter.equals(target) && !shooter.hasMetadata("NPC"))
                        return !(event.getDamage() == 0);
                }
            }
        }
        return false;
    }
}
