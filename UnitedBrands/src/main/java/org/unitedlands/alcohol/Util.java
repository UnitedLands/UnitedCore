package org.unitedlands.alcohol;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.unitedlands.alcohol.brand.Brand;
import org.unitedlands.alcohol.brand.BrandsFile;

import java.util.Set;
import java.util.UUID;

public class Util {

    public static String getMessage(String message, String brandName) {
        return color(getUnitedBrands().getConfig().getString("messages." + message).replace("<brand>", brandName));
    }

    private static String color(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    private static UnitedBrands getUnitedBrands() {
       return (UnitedBrands) Bukkit.getPluginManager().getPlugin("UnitedBrands");
    }

    public static Brand getPlayerBrand(Player player) {
        BrandsFile brandsFile = getBrandsFile();
        FileConfiguration brandsConfig = brandsFile.getBrandsConfig();
        String uuid = player.getUniqueId().toString();
        Set<String> keys = brandsConfig.getConfigurationSection("brands").getKeys(true);

        for (String key : keys) {
            if (key.contains("owner-uuid") || key.contains("members")) {
                if (uuid.equals(brandsConfig.getString("brands." + key)) ||
                        brandsConfig.getStringList("brands." + key).contains(uuid)) {
                    // MyBrand.owner-uuid -> [MyBrand, owner-uuid] -> MyBrand. Fuck this
                    String brandName = key.split("\\.")[0];
                    UUID ownerUUID = UUID.fromString(brandsConfig.getString("brands." + brandName + ".owner-uuid"));
                    try {
                        return new Brand(getUnitedBrands(), brandName, Bukkit.getPlayer(ownerUUID), null);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return null;
    }

    public static boolean brandExists(Brand brand) {
        BrandsFile brandsFile = getBrandsFile();
        FileConfiguration brandsConfig = brandsFile.getBrandsConfig();
        Set<String> brandNames = brandsConfig.getConfigurationSection("brands").getKeys(false);
        for (String brandName : brandNames) {
            if (brand.getBrandName().equals(brandName)) {
                return true;
            }
        }
        return false;
    }

    @NotNull
    private static BrandsFile getBrandsFile() {
        return new BrandsFile(getUnitedBrands());
    }

    public static boolean hasBrand(Player player) {
        return getPlayerBrand(player) != null;
    }
}
