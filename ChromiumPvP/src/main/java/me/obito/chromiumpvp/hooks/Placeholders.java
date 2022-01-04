/*package me.obito.chromiumpvp.hooks;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.obito.chromiumpvp.ChromiumPvP;
import org.bukkit.OfflinePlayer;

public class Placeholders extends PlaceholderExpansion {

    @Override
    public String getAuthor() {
        return "Maroon28";
    }

    @Override
    public String getIdentifier() {
        return "chromiumpvp";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        if (player.getPlayer() != null) {
            if (params.equalsIgnoreCase("status")) {
                boolean pvp = ChromiumPvP.getPvPStatus(player.getPlayer());
                if (pvp) {
                    return "§c⚔";
                } else {
                    return "§a🛡";
                }
            }
        }

        return null;
    }
}*/
