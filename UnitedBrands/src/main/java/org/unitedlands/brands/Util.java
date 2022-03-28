package org.unitedlands.brands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.unitedlands.brands.brewery.Brewery;
import org.unitedlands.brands.brewery.BreweriesFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class Util {

    public static String getMessage(String message) {
        return color(getUnitedBrands().getConfig().getString("messages." + message));
    }

    public static String getMessage(String message, String breweryName) {
        return getMessage(message).replace("<brewery>", breweryName);
    }

    public static String getMessage(String message, Player player) {
        return getMessage(message).replace("<player>", player.getName());
    }

    public static String getMessage(String message, String breweryName, Player player) {
        return getMessage(message, breweryName).replace("<player>", player.getName());
    }

    private static String color(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    private static UnitedBrands getUnitedBrands() {
        return (UnitedBrands) Bukkit.getPluginManager().getPlugin("UnitedBrands");
    }

    public static Brewery getPlayerBrewery(Player player) {
        FileConfiguration breweriesConfig = getBreweriesConfig();
        String uuid = player.getUniqueId().toString();
        Set<String> keys = breweriesConfig.getConfigurationSection("breweries").getKeys(true);

        for (String key : keys) {
            if (key.contains("owner-uuid") || key.contains("members")) {
                if (uuid.equals(breweriesConfig.getString("breweries." + key)) ||
                        breweriesConfig.getStringList("breweries." + key).contains(uuid)) {
                    // MyBrand.owner-uuid -> [MyBrand, owner-uuid] -> MyBrand. Fuck this
                    String breweryName = key.split("\\.")[0];
                    UUID ownerUUID = UUID.fromString(breweriesConfig.getString("breweries." + breweryName + ".owner-uuid"));
                    List<String> members = breweriesConfig.getStringList("breweries." + breweryName + ".members");
                    try {
                        return new Brewery(getUnitedBrands(), breweryName, Bukkit.getOfflinePlayer(ownerUUID), members);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return null;
    }

    public static Brewery getBreweryFromName(String name) {
        FileConfiguration breweriesConfig = getBreweriesConfig();
        ConfigurationSection brewerySection = breweriesConfig.getConfigurationSection("breweries");
        Set<String> breweryNames = getBreweryNames();

        for (String breweryName : breweryNames) {
            if (name.equals(breweryName)) {
                UUID ownerUUID = UUID.fromString(brewerySection.getString(breweryName + ".owner-uuid"));
                List<String> members = brewerySection.getStringList(breweryName + ".members");
                return new Brewery(getUnitedBrands(), breweryName, Bukkit.getOfflinePlayer(ownerUUID), members);
            }
        }
        return null;
    }

    public static boolean breweryExists(Brewery brewery) {
        Set<String> breweryNames = getBreweryNames();
        for (String breweryName : breweryNames) {
            if (brewery.getBreweryName().equals(breweryName)) {
                return true;
            }
        }
        return false;
    }

    @NotNull
    public static Set<String> getBreweryNames() {
        FileConfiguration breweriesConfig = getBreweriesConfig();
        return breweriesConfig.getConfigurationSection("breweries").getKeys(false);
    }

    private static FileConfiguration getBreweriesConfig() {
        BreweriesFile breweriesFile = new BreweriesFile(getUnitedBrands());;
        return breweriesFile.getBreweriesConfig();
    }

    public static ArrayList<Brewery> getAllBreweries() {
        Set<String> breweryNames = Util.getBreweryNames();
        ArrayList<Brewery> breweries = new ArrayList<>();
        for (String breweryName : breweryNames) {
            Brewery brewery = Util.getBreweryFromName(breweryName);
            breweries.add(brewery);
        }
        return breweries;
    }

    public static boolean hasBrewery(Player player) {
        return getPlayerBrewery(player) != null;
    }
}
