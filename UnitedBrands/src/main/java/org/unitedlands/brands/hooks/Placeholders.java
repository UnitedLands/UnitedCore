package org.unitedlands.brands.hooks;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.unitedlands.brands.Util;
import org.unitedlands.brands.brewery.Brewery;
import org.unitedlands.brands.stats.PlayerStatsFile;

public class Placeholders extends PlaceholderExpansion {

    private final PlayerStatsFile playerStatsFile;

    public Placeholders(PlayerStatsFile playerStatsFile) {
        this.playerStatsFile = playerStatsFile;
    }

    @Override
    public String getAuthor() {
        return "Maroon28";
    }

    @Override
    public String getIdentifier() {
        return "unitedbrands";
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
            switch (params) {
                case "brewery-name":
                    return Util.getPlayerBrewery(player).getBreweryName();
                case "personal_brews-made":
                    return getPlayerStat(player, "brews-made");
                case "personal_brews-drunk":
                    return getPlayerStat(player, "brews-drunk");
                case "personal_average-stars":
                    return getPlayerStat(player, "average-stars");
                case "brewery_brews-made":
                    return getBreweryStat(player, "brews-made");
                case "brewery_brews-drunk":
                    return getBreweryStat(player, "brews-drunk");
                case "brewery_average-stars":
                    return getBreweryStat(player, "average-stars");
                case "has-brewery":
                    return String.valueOf(Util.hasBrewery(player));
            }
        }

        return null;
    }

    @NotNull
    private String getPlayerStat(OfflinePlayer player, String stat) {
        final String playerUUID = player.getUniqueId().toString();
        return String.valueOf(playerStatsFile.getStatsConfig().getInt("players." + playerUUID + "." + stat));
    }

    private String getBreweryStat(OfflinePlayer player, String stat) {
        Brewery brewery = Util.getPlayerBrewery(player);
        return String.valueOf(brewery.getBreweryStat(stat));
    }

}
