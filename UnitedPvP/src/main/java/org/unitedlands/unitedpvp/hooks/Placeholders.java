package org.unitedlands.unitedpvp.hooks;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.unitedlands.unitedpvp.UnitedPvP;
import org.unitedlands.unitedpvp.util.Utils;
import org.bukkit.OfflinePlayer;

public class Placeholders extends PlaceholderExpansion {
    private final UnitedPvP plugin;

    public Placeholders(UnitedPvP plugin) {
        this.plugin = plugin;
    }
    @Override
    public String getAuthor() {
        return "Maroon28";
    }

    @Override
    public String getIdentifier() {
        return "unitedpvp";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        if (player.getPlayer() != null) {
            if (params.equalsIgnoreCase("status")) {
                boolean pvp = Utils.getPvPStatus(player.getPlayer());
                if (pvp) {
                    return "§c⚔";
                } else {
                    return "§a🛡";
                }
            } else if (params.equalsIgnoreCase("status-string")) {
                boolean pvp = Utils.getPvPStatus(player.getPlayer());
                if (pvp) {
                    return "enabled";
                } else {
                    return "disabled";
                }
            }
        }

        return null;
    }
}
