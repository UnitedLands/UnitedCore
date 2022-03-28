package org.unitedlands.alcohol;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.unitedlands.alcohol.brand.Brand;
import org.unitedlands.alcohol.brand.BrandsFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class Util {

    public static String getMessage(String message) {
        return color(getUnitedBrands().getConfig().getString("messages." + message));
    }

    public static String getMessage(String message, String brandName) {
        return getMessage(message).replace("<brand>", brandName);
    }

    public static String getMessage(String message, Player player) {
        return getMessage(message).replace("<player>", player.getName());
    }

    public static String getMessage(String message, String brandName, Player player) {
        return getMessage(message, brandName).replace("<player>", player.getName());
    }

    private static String color(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    private static UnitedBrands getUnitedBrands() {
        return (UnitedBrands) Bukkit.getPluginManager().getPlugin("UnitedBrands");
    }

    public static Brand getPlayerBrand(Player player) {
        FileConfiguration brandsConfig = getBrandsConfig();
        String uuid = player.getUniqueId().toString();
        Set<String> keys = brandsConfig.getConfigurationSection("brands").getKeys(true);

        for (String key : keys) {
            if (key.contains("owner-uuid") || key.contains("members")) {
                if (uuid.equals(brandsConfig.getString("brands." + key)) ||
                        brandsConfig.getStringList("brands." + key).contains(uuid)) {
                    // MyBrand.owner-uuid -> [MyBrand, owner-uuid] -> MyBrand. Fuck this
                    String brandName = key.split("\\.")[0];
                    UUID ownerUUID = UUID.fromString(brandsConfig.getString("brands." + brandName + ".owner-uuid"));
                    List<String> members = brandsConfig.getStringList("brands." + brandName + ".members");
                    try {
                        return new Brand(getUnitedBrands(), brandName, Bukkit.getOfflinePlayer(ownerUUID), members);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return null;
    }

    public static Brand getBrandFromName(String name) {
        FileConfiguration brandsConfig = getBrandsConfig();
        ConfigurationSection brandsSection = brandsConfig.getConfigurationSection("brands");
        Set<String> brands = getBrandNames();

        for (String brandName : brands) {
            if (name.equals(brandName)) {
                UUID ownerUUID = UUID.fromString(brandsSection.getString(brandName + ".owner-uuid"));
                List<String> members = brandsSection.getStringList(brandName + ".members");
                return new Brand(getUnitedBrands(), brandName, Bukkit.getOfflinePlayer(ownerUUID), members);
            }
        }
        return null;
    }

    public static boolean brandExists(Brand brand) {
        Set<String> brandNames = getBrandNames();
        for (String brandName : brandNames) {
            if (brand.getBrandName().equals(brandName)) {
                return true;
            }
        }
        return false;
    }

    @NotNull
    public static Set<String> getBrandNames() {
        FileConfiguration brandsConfig = getBrandsConfig();
        return brandsConfig.getConfigurationSection("brands").getKeys(false);
    }

    private static FileConfiguration getBrandsConfig() {
        BrandsFile brandsFile = new BrandsFile(getUnitedBrands());;
        return brandsFile.getBrandsConfig();
    }

    public static ArrayList<Brand> getAllBrands() {
        Set<String> brandNames = Util.getBrandNames();
        ArrayList<Brand> brands = new ArrayList<>();
        for (String brandName : brandNames) {
            Brand brand = Util.getBrandFromName(brandName);
            brands.add(brand);
        }
        return brands;
    }

    public static boolean hasBrand(Player player) {
        return getPlayerBrand(player) != null;
    }
}
