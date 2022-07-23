package org.unitedlands.pvp.util;

import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.object.WorldCoord;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.jetbrains.annotations.NotNull;
import org.unitedlands.pvp.player.PvpPlayer;
import org.unitedlands.pvp.UnitedPvP;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class Utils {

    private final UnitedPvP unitedPVP;
    public static final MiniMessage miniMessage = MiniMessage.miniMessage();

    public Utils(UnitedPvP unitedPVP) {
        this.unitedPVP = unitedPVP;
    }

    public void setPvPStatus(Player player, boolean status) {
        PvpPlayer file = new PvpPlayer(player);
        FileConfiguration playerConfig = file.getFileConfiguration();
        playerConfig.set("PvP", status);
        try {
            playerConfig.save(playerConfig.getCurrentPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public int getPvPStatus(Player player) {
        boolean isCitizensNPC = player.hasMetadata("NPC");

        if (isCitizensNPC) {
            return 0;
        }
        PvpPlayer file = new PvpPlayer(player);
        FileConfiguration playerConfig = file.getFileConfiguration();

       return playerConfig.getInt("status");
    }

    public static UnitedPvP getUnitedPvP() {
        return (UnitedPvP) Bukkit.getPluginManager().getPlugin("UnitedPvP");
    }


    @NotNull
    public static Component getMessage(String message) {
        FileConfiguration config = getUnitedPvP().getConfig();
        String prefix = config.getString("messages.prefix");
        String configuredMessage = prefix + config.getString("messages." + message);
        return miniMessage.deserialize(Objects.requireNonNullElseGet(configuredMessage, () -> "<red>Message <yellow>" + message + "<red> could not be found in the config file!"));
    }

}
