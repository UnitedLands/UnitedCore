package org.unitedlands.brands.brewery;

import com.google.common.base.Charsets;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.unitedlands.brands.UnitedBrands;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class BreweriesFile {
    private static final UnitedBrands PLUGIN = UnitedBrands.getInstance();
    private FileConfiguration breweryConfig;

    public void createBreweriesFile() {
        File breweryDataFile = getBreweriesFile();
        if (!breweryDataFile.exists()) {
            breweryDataFile.getParentFile().mkdirs();
            PLUGIN.saveResource("breweries.yml", false);
        }
        breweryConfig = new YamlConfiguration();
        try {
            breweryConfig.load(breweryDataFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    public void saveConfig(FileConfiguration breweriesConfig) {
        try {
            breweriesConfig.save(getBreweriesFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public FileConfiguration getBreweriesConfig() {
        File breweryDataFile = getBreweriesFile();
        breweryConfig = new YamlConfiguration();
        try {
            breweryConfig.load(breweryDataFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
        return breweryConfig;
    }

    public void reloadConfig() {
        FileConfiguration newConfig = YamlConfiguration.loadConfiguration(getBreweriesFile());

        final InputStream defConfigStream = PLUGIN.getResource("breweries.yml");
        if (defConfigStream == null) {
            return;
        }

        newConfig.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream, Charsets.UTF_8)));
    }

    @NotNull
    private File getBreweriesFile() {
        return new File(PLUGIN.getDataFolder(), "breweries.yml");
    }
}
