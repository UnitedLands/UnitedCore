package org.unitedlands.brands.hooks;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.unitedlands.brands.BreweryDatabase;
import org.unitedlands.brands.brewery.Brewery;
import org.unitedlands.brands.stats.BrandPlayer;

public class Placeholders extends PlaceholderExpansion {

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
                case "brewery-name" -> {
                    return BreweryDatabase.getPlayerBrewery(player.getUniqueId()).getName();
                }
                case "personal_brews-made" -> {
                    return getPlayerStat(player, "brews-made");
                }
                case "personal_brews-drunk" -> {
                    return getPlayerStat(player, "brews-drunk");
                }
                case "personal_average-stars" -> {
                    return getPlayerStat(player, "average-stars");
                }
                case "brewery_brews-made" -> {
                    return getBreweryStat(player, "brews-made");
                }
                case "brewery_brews-drunk" -> {
                    return getBreweryStat(player, "brews-drunk");
                }
                case "brewery_average-stars" -> {
                    return getBreweryStat(player, "average-stars");
                }
                case "has-brewery" -> {
                    return String.valueOf(BreweryDatabase.isInBrewery(player.getUniqueId()));
                }
            }
        }

        return null;
    }

    @NotNull
    private String getPlayerStat(OfflinePlayer player, String stat) {
        BrandPlayer brandPlayer = BreweryDatabase.getBrandPlayer(player.getUniqueId());
        int value = switch (stat) {
            case "brews-made" -> brandPlayer.getBrewsMade();
            case "brews-drunk" -> brandPlayer.getBrewsDrunk();
            case "total-stars" -> brandPlayer.getTotalStars();
            case "average-stars" -> brandPlayer.getAverageStars();
            default -> 0;
        };
        return String.valueOf(value);
    }

    private String getBreweryStat(OfflinePlayer player, String stat) {
        Brewery brewery = BreweryDatabase.getPlayerBrewery(player.getUniqueId());
        return String.valueOf(brewery.getBreweryStat(stat));
    }

}
