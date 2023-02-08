package org.unitedlands.brands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.unitedlands.brands.brewery.BreweriesFile;
import org.unitedlands.brands.brewery.Brewery;
import org.unitedlands.brands.stats.BrandPlayer;
import org.unitedlands.brands.stats.PlayerStatsFile;

import java.util.*;
import java.util.logging.Level;

public class BreweryDatabase {
    private static final UnitedBrands PLUGIN = UnitedBrands.getInstance();
    private static final HashSet<Brewery> BREWERIES = new HashSet<>();
    private static final HashSet<BrandPlayer> PLAYERS = new HashSet<>();
    private static final BreweriesFile BREWERIES_FILE = new BreweriesFile();

    public static void load() {
        loadBreweries();
        loadPlayers();
    }

    public static void save() {
        saveBreweries();
        savePlayers();
    }

    private static void loadBreweries() {
        Set<String> breweryNames = PLUGIN.getBreweriesConfig().getKeys(false);
        for (String breweryName : breweryNames) {
            Brewery brewery = generateBreweryFromUUID(breweryName);
            BREWERIES.add(brewery);
        }
    }

    private static void loadPlayers() {
        Set<String> playerUUIDs = PLUGIN.getPlayerStatsConfig().getKeys(false);
        for (String uuid : playerUUIDs) {
            BrandPlayer brandPlayer = new BrandPlayer(UUID.fromString(uuid));
            setPlayerStats(uuid, brandPlayer);
            addPlayer(brandPlayer);
        }
    }


    private static void saveBreweries() {
        PLUGIN.getLogger().log(Level.INFO, "Saving brewery data...");
        FileConfiguration breweriesConfig = PLUGIN.getBreweriesConfig();
        for (Brewery brewery : BREWERIES) {
            // Create the section
            ConfigurationSection section = breweriesConfig.createSection(brewery.getUUID().toString());
            // Set the data for each object
            section.set("name", brewery.getName());
            section.set("owner-uuid", brewery.getOwner().getUniqueId().toString());
            section.set("slogan", brewery.getSlogan());
            section.set("members", brewery.getMembers());
            section.set("level", brewery.getLevel());
            section.set("brews-drunk", brewery.getBrewsDrunk());
            section.set("brews-made", brewery.getBrewsMade());
            section.set("total-stars", brewery.getTotalStars());
            section.set("average-stars", brewery.getAverageStars());
        }
        // Create the file object and save it to disk
        BREWERIES_FILE.saveConfig(breweriesConfig);
    }

    private static void savePlayers() {
        FileConfiguration config = PLUGIN.getPlayerStatsConfig();
        for (BrandPlayer p: PLAYERS) {
            ConfigurationSection playerSection = config.createSection(p.getUUID().toString());
            playerSection.set("brews-drunk", p.getBrewsDrunk());
            playerSection.set("brews-made", p.getBrewsMade());
            playerSection.set("total-stars", p.getTotalStars());
            playerSection.set("average-stars", p.getAverageStars());
        }
        PlayerStatsFile file = new PlayerStatsFile();
        file.saveConfig(config);
    }

    public static boolean hasBrewery(String name) {
        for (Brewery b : BREWERIES) {
            if (b.getName().equalsIgnoreCase(name))
                return true;
        }
        return false;
    }

    public static BrandPlayer getBrandPlayer(Player player) {
        return getBrandPlayer(player.getUniqueId());
    }

    public static BrandPlayer getBrandPlayer(UUID uuid) {
        for (BrandPlayer bp : PLAYERS) {
            if (bp.getUUID().equals(uuid))
                return bp;
        }
        return new BrandPlayer(uuid);
    }

    public static boolean isInBrewery(Player player) {
        return getPlayerBrewery(player) != null;
    }

    public static boolean isInBrewery(UUID uuid) {
        return getPlayerBrewery(uuid) != null;
    }

    public static Brewery getPlayerBrewery(Player player) {
        return getPlayerBrewery(player.getUniqueId());
    }

    public static Brewery getPlayerBrewery(UUID uuid) {
        for (Brewery brewery : BREWERIES) {
            if (brewery.getOwner().getUniqueId().equals(uuid))
                return brewery;
            if (brewery.getMembers().contains(uuid.toString()))
                return brewery;
        }
        return null;
    }

    public static Brewery getBreweryFromName(String name) {
        for (Brewery brewery : BREWERIES) {
            if (brewery.getName().equalsIgnoreCase(name))
                return brewery;
        }
        return null;
    }

    public static void createBrewery(String name, OfflinePlayer owner) {
        Brewery brewery = new Brewery(name, owner, new ArrayList<>());
        brewery.setSlogan("");
        BREWERIES.add(brewery);
        owner.getPlayer().sendMessage(Util.getMessage("brewery-created", name));
    }

    public static HashSet<Brewery> getBreweries() {
        return BREWERIES;
    }


    private static Brewery generateBreweryFromUUID(String uuid) {
        FileConfiguration config = PLUGIN.getBreweriesConfig();
        Set<String> breweryNames = config.getKeys(false);

        for (String current : breweryNames) {
            if (!uuid.equalsIgnoreCase(current))
                continue;

            UUID ownerUUID = UUID.fromString(config.getString(uuid + ".owner-uuid"));
            List<String> members = config.getStringList(uuid + ".members");
            String name = config.getString(uuid + ".name");
            Brewery brewery = new Brewery(name, Bukkit.getOfflinePlayer(ownerUUID), members, UUID.fromString(uuid));
            brewery.setBrewsDrunk(getStat(brewery, "brews-drunk"));
            brewery.setBrewsMade(getStat(brewery, "brews-made"));
            brewery.setAverageStars(getStat(brewery, "average-stars"));
            brewery.setLevel(getStat(brewery, "level"));
            brewery.setTotalStars(getStat(brewery, "total-stars"));
            brewery.setSlogan(config.getString(uuid + ".slogan"));
            return brewery;
        }
        return null;
    }

    private static int getStat(Brewery brewery, String name) {
        FileConfiguration config = PLUGIN.getBreweriesConfig();
        return config.getInt(brewery.getUUID() + "." + name);
    }

    private static BrandPlayer setPlayerStats(String uuid, BrandPlayer brandPlayer) {
        brandPlayer.setAverageStars(getPlayerStat(uuid, "average-stars"));
        brandPlayer.setBrewsDrunk(getPlayerStat(uuid, "brews-drunk"));
        brandPlayer.setBrewsMade(getPlayerStat(uuid, "brews-made"));
        brandPlayer.setTotalStars(getPlayerStat(uuid, "total-stars"));
        return brandPlayer;
    }

    private static int getPlayerStat(String uuid, String name) {
        return PLUGIN.getPlayerStatsConfig().getInt(uuid + "." + name);
    }

    public static void delete(Brewery brewery) {
        BREWERIES.remove(brewery);
        FileConfiguration breweriesConfig = PLUGIN.getBreweriesConfig();
        breweriesConfig.set(brewery.getUUID().toString(), null);
        BREWERIES_FILE.saveConfig(breweriesConfig);
    }

    public static void addPlayer(BrandPlayer brandPlayer) {
        PLAYERS.add(brandPlayer);
    }

}
