package org.unitedlands.pvp.util;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.unitedlands.pvp.UnitedPvP;

import java.io.File;
import java.io.IOException;

public class Utils {

    private final UnitedPvP unitedPVP;

    public Utils(UnitedPvP unitedPVP) {
        this.unitedPVP = unitedPVP;
    }


    public void setPvPStatus(Player player, boolean status) {
        FileConfiguration playerConfig = unitedPVP.getPlayerConfig(player);
        File file = unitedPVP.getPlayerFile(player);
        playerConfig.set("PvP", status);
        try {
            playerConfig.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean getPvPStatus(Player player) {
        boolean isCitizensNPC = player.hasMetadata("NPC");

        if (isCitizensNPC) {
            return false;
        }

       return unitedPVP.getPlayerConfig(player).getBoolean("PvP");
    }

    private String color(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public String getMessage(String s) {
        return color(unitedPVP.getConfig().getString("messages." + s));
    }

    public boolean isPvP(final EntityDamageByEntityEvent event) {
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
