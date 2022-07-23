package org.unitedlands.pvp.hooks;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.unitedlands.pvp.player.PvpPlayer;
import org.unitedlands.pvp.player.Status;
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
            PvpPlayer pvpPlayer = new PvpPlayer((Player) player);
            if (params.equalsIgnoreCase("status")) {
                int hostility = pvpPlayer.getHostility();
                if (hostility < Status.HOSTILE.getStartingValue()) {
                    return "";
                }
                return pvpPlayer.getIconHex(hostility) + pvpPlayer.getStatus().getIcon();
            } else if (params.equalsIgnoreCase("status-string")) {
                return String.valueOf(pvpPlayer.getStatus());
            }
        }

        return null;
    }
}
