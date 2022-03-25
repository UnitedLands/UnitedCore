package org.unitedlands.alcohol;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Set;

public class Brand {

    private final UnitedBrands ub;
    private FileConfiguration brandConfig;

    public Brand(UnitedBrands ub) {
        this.ub = ub;
    }

    private boolean brandExists(String name) {
        FileConfiguration brandsConfig = getBrandsConfig();
        Set<String> brandNames = brandsConfig.getConfigurationSection("brands").getKeys(false);
        for (String brand : brandNames) {
            if (brand.equals(name)) {
                return true;
            }
        }
        return false;
    }

    public Player getBrandOwner(String name) {
        String ownerUUID = getBrandsConfig().getString("brands." + name + "owner-uuid");
        if (ownerUUID != null) {
            return Bukkit.getPlayer(ownerUUID);
        }
        return null;
    }

    public void deleteBrand(String name) {
        getBrandsConfig().set("brands." + name, "");
    }

    public void createBrand(String name, Player player) {
        FileConfiguration brandsConfig = getBrandsConfig();

        if (brandExists(name)) {
            player.sendMessage(Util.getMessage("brand-already-exists", name));
            return;
        }

        brandsConfig.createSection("brands." + name);
        brandsConfig.set("brands." + name + ".name", name);
        brandsConfig.set("brands." + name + ".owner-uuid", player.getUniqueId().toString());
        try {
            brandsConfig.save(getBrandFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
        player.sendMessage("Brand " + name + " created!");
    }

    public void createBrandsFile() {
        File brandDataFile = getBrandFile();
        if (!brandDataFile.exists()) {
            brandDataFile.getParentFile().mkdirs();
            ub.saveResource("brands.yml", false);
        }
        brandConfig = new YamlConfiguration();
        try {
            brandConfig.load(brandDataFile);
            brandConfig.createSection("brands");
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    public boolean hasBrand(Player player) {
        return getPlayerBrand(player) != null;
    }

    public void setSlogan(String brand, String slogan) {
        FileConfiguration brandsConfig = getBrandsConfig();
        brandsConfig.set("brands." + brand + ".slogan", slogan);
        try {
            brandsConfig.save(getBrandFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getPlayerBrand(Player player) {
        brandConfig = getBrandsConfig();
        Set<String> brands = getBrands();
        String uuid = player.getUniqueId().toString();

        for (String key : brands) {
            if (key.contains("owner-uuid") || key.contains("members")) {
                if (uuid.equals(brandConfig.getString("brands." + key))) {
                    // MyBrand.owner-uuid -> [MyBrand, owner-uuid] -> MyBrand. Fuck this
                    return key.split("\\.")[0];
                }
            }
        }
        return null;
    }

    @NotNull
    private Set<String> getBrands() {
        return brandConfig.getConfigurationSection("brands").getKeys(true);
    }

    public String getBrandSlogan(String brand) {
        return getBrandsConfig().getString("brands." + brand + ".slogan");
    }

    public FileConfiguration getBrandsConfig() {
        File brandDataFile = getBrandFile();
        brandConfig = new YamlConfiguration();
        try {
            brandConfig.load(brandDataFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
        return brandConfig;
    }

    @NotNull
    private File getBrandFile() {
        return new File(ub.getDataFolder(), "brands.yml");
    }


}
