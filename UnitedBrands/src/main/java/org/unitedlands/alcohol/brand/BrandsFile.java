package org.unitedlands.alcohol.brand;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.unitedlands.alcohol.UnitedBrands;

import java.io.File;
import java.io.IOException;

public class BrandsFile {
    private FileConfiguration brandConfig;
    private final UnitedBrands ub;

    public BrandsFile(UnitedBrands ub) {
        this.ub = ub;
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

    public void saveConfig(FileConfiguration brandsConfig) {
        try {
            brandsConfig.save(getBrandFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
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
