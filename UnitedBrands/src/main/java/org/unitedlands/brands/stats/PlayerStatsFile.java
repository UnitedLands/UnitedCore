package org.unitedlands.brands.stats;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.unitedlands.brands.UnitedBrands;

import java.io.File;
import java.io.IOException;
public class PlayerStatsFile {
    private FileConfiguration statsConfig;
    private static final UnitedBrands PLUGIN = UnitedBrands.getInstance();

    public void createStatsFile() {
        File playerDataFile = getStatsFile();
        if (!playerDataFile.exists()) {
            playerDataFile.getParentFile().mkdirs();
            PLUGIN.saveResource("player-stats.yml", false);
        }
        statsConfig = new YamlConfiguration();
        try {
            statsConfig.load(playerDataFile);
            statsConfig.createSection("players");
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    public void saveConfig(FileConfiguration statsConfig) {
        try {
            statsConfig.save(getStatsFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public FileConfiguration getStatsConfig() {
        File statsFile = getStatsFile();
        statsConfig = new YamlConfiguration();
        try {
            statsConfig.load(statsFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
        return statsConfig;
    }

    @NotNull
    private File getStatsFile() {
        return new File(PLUGIN.getDataFolder(), "player-stats.yml");
    }
}
