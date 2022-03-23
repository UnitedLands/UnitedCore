package org.unitedlands.pvp.hooks;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.unitedlands.pvp.util.Utils;
import org.bukkit.OfflinePlayer;

public class Placeholders extends PlaceholderExpansion {
    private final Utils utils;

    public Placeholders(Utils utils) {
        this.utils = utils;
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
            boolean pvp = utils.getPvPStatus(player.getPlayer());
            if (params.equalsIgnoreCase("status")) {
                if (pvp) {
                    return "§c⚔";
                } else {
                    return "§a🛡";
                }
            } else if (params.equalsIgnoreCase("status-string")) {
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
