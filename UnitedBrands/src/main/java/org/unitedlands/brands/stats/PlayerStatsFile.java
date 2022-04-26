package org.unitedlands.brands.stats;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.unitedlands.brands.UnitedBrands;

import java.io.File;
import java.io.IOException;

public class PlayerStatsFile {
    private final UnitedBrands ub;
    private FileConfiguration statsConfig;
    private final Player player;

    public PlayerStatsFile(UnitedBrands ub, Player player) {
        this.ub = ub;
        this.player = player;
    }

    public void createStatsFile() {
        File playerDataFile = getStatsFile();
        if (!playerDataFile.exists()) {
            playerDataFile.getParentFile().mkdirs();
            ub.saveResource(getFilePath(), false);
        }
        statsConfig = new YamlConfiguration();
        try {
            statsConfig.load(playerDataFile);
            statsConfig.set("name", player.getName());
            statsConfig.set("brews-made", 0);
            statsConfig.set("brews-drunk", 0);
            statsConfig.set("total-stars", 0);
            statsConfig.set("average-stars", 0);
            saveConfig(statsConfig);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    private String getFilePath() {
        return File.separator + "players" + File.separator + player.getUniqueId().toString() + ".yml";
    }

    public void saveConfig(FileConfiguration statsConfig) {
        try {
            statsConfig.save(getStatsFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public FileConfiguration getStatsConfig() {
        File playerStatsFile = getStatsFile();
        statsConfig = new YamlConfiguration();
        try {
            statsConfig.load(playerStatsFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
        return statsConfig;
    }

    public int getPlayerStat(String statName) {
        return getStatsConfig().getInt(statName);
    }

    public void updateAverageStars() {
        FileConfiguration statsConfig = getStatsConfig();
        if (statsConfig == null) {
            createStatsFile();
            statsConfig = getStatsConfig();
        }
        int totalStars = getPlayerStat("total-stars");
        int brewsDrunk = getPlayerStat("brews-drunk");
        double newAverage = (double) totalStars / brewsDrunk;
        statsConfig.set("average-stars", Math.round(newAverage));
        saveConfig(statsConfig);
    }

    public void increaseStat(String statName, int increment) {
        FileConfiguration statsConfig = getStatsConfig();
        if (statsConfig == null) {
            createStatsFile();
            statsConfig = getStatsConfig();
        }
        int currentAmount = statsConfig.getInt(statName);
        statsConfig.set(statName, currentAmount + increment);
        saveConfig(statsConfig);
    }

    @NotNull
    private File getStatsFile() {
        return new File(ub.getDataFolder(), getFilePath());
    }
}
