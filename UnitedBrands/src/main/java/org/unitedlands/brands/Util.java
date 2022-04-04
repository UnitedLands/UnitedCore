package org.unitedlands.brands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.unitedlands.brands.brewery.BreweriesFile;
import org.unitedlands.brands.brewery.Brewery;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class Util {
    private static final MiniMessage miniMessage = MiniMessage.miniMessage();

    public static Component getMessage(String message) {
        message = getUnitedBrands().getConfig().getString("messages." + message);
        return miniMessage.deserialize(message);
    }

    public static Component getMessage(String message, String breweryName) {
        TextReplacementConfig breweryReplacer = TextReplacementConfig.builder()
                .match("<brewery>")
                .replacement(breweryName)
                .build();
        return getMessage(message).replaceText(breweryReplacer);
    }

    public static Component getMessage(String message, Player player) {
        TextReplacementConfig playerReplacer = TextReplacementConfig.builder()
                .match("<player>")
                .replacement(player.getName())
                .build();
        return getMessage(message).replaceText(playerReplacer);
    }

    public static Component getMessage(String message, String breweryName, Player player) {
        TextReplacementConfig playerReplacer = TextReplacementConfig.builder()
                .match("<player>")
                .replacement(player.getName())
                .build();
        return getMessage(message, breweryName).replaceText(playerReplacer);
    }


    private static UnitedBrands getUnitedBrands() {
        return (UnitedBrands) Bukkit.getPluginManager().getPlugin("UnitedBrands");
    }

    public static Brewery getPlayerBrewery(OfflinePlayer player) {
        FileConfiguration breweriesConfig = getBreweriesConfig();
        String uuid = player.getUniqueId().toString();
        Set<String> keys = breweriesConfig.getKeys(true);

        for (String key : keys) {
            if (key.contains("owner-uuid") || key.contains("members")) {
                if (uuid.equals(breweriesConfig.getString(key)) ||
                        breweriesConfig.getStringList(key).contains(uuid)) {
                    // MyBrand.owner-uuid -> [MyBrand, owner-uuid] -> MyBrand. Fuck this
                    String breweryName = key.split("\\.")[0];
                    UUID ownerUUID = UUID.fromString(breweriesConfig.getString(breweryName + ".owner-uuid"));
                    List<String> members = breweriesConfig.getStringList(breweryName + ".members");
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
        Set<String> breweryNames = getBreweryNames();

        for (String breweryName : breweryNames) {
            if (name.equalsIgnoreCase(breweryName)) {
                UUID ownerUUID = UUID.fromString(breweriesConfig.getString(breweryName + ".owner-uuid"));
                List<String> members = breweriesConfig.getStringList(breweryName + ".members");
                return new Brewery(getUnitedBrands(), breweryName, Bukkit.getOfflinePlayer(ownerUUID), members);
            }
        }
        return null;
    }

    public static boolean breweryExists(Brewery brewery) {
        Set<String> breweryNames = getBreweryNames();
        for (String breweryName : breweryNames) {
            if (brewery.getBreweryName().equalsIgnoreCase(breweryName)) {
                return true;
            }
        }
        return false;
    }

    @NotNull
    public static Set<String> getBreweryNames() {
        FileConfiguration breweriesConfig = getBreweriesConfig();
        return breweriesConfig.getKeys(false);
    }

    private static FileConfiguration getBreweriesConfig() {
        BreweriesFile breweriesFile = new BreweriesFile(getUnitedBrands());
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

    public static boolean hasBrewery(OfflinePlayer player) {
        return getPlayerBrewery(player) != null;
    }
}
