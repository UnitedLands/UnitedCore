package org.unitedlands.brands.stats;

import com.google.common.base.Charsets;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.unitedlands.brands.UnitedBrands;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class PlayerStatsFile {
    private final UnitedBrands ub;
    private FileConfiguration statsConfig;

    public PlayerStatsFile(UnitedBrands ub) {
        this.ub = ub;
    }

    public void createPlayerSection(Player player) {
        String playerUUID = player.getUniqueId().toString();
        FileConfiguration statsConfig = getStatsConfig();
        statsConfig.createSection("players." + playerUUID);
        ConfigurationSection playerSection = statsConfig.getConfigurationSection("players." + playerUUID);
        playerSection.set("name", player.getName());
        playerSection.set("brews-made", 0);
        playerSection.set("brews-drunk", 0);
        playerSection.set("total-stars", 0);
        playerSection.set("average-stars", 0);
        saveConfig(statsConfig);
    }

    public void createStatsFile() {
        File playerDataFile = getStatsFile();
        if (!playerDataFile.exists()) {
            playerDataFile.getParentFile().mkdirs();
            ub.saveResource("player-stats.yml", false);
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
        File breweryDataFile = getStatsFile();
        statsConfig = new YamlConfiguration();
        try {
            statsConfig.load(breweryDataFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
        return statsConfig;
    }

    public int getPlayerStat(OfflinePlayer player, String statName) {
        return getStatsConfig().getInt("players." + player.getUniqueId().toString() + "." + statName);
    }

    public void updateAverageStars(OfflinePlayer player) {
        FileConfiguration statsConfig = getStatsConfig();
        int totalStars = getPlayerStat(player, "total-stars");
        int brewsDrunk = getPlayerStat(player, "brews-drunk");
        double newAverage = (double) totalStars / brewsDrunk;
        statsConfig.set("players." + player.getUniqueId().toString() + ".average-stars", Math.round(newAverage));
        saveConfig(statsConfig);
    }

    public void increaseStat(Player player, String statName, int increment) {
        FileConfiguration statsConfig = getStatsConfig();
        String statPath = "players." + player.getUniqueId().toString() + "." + statName;
        if (statsConfig.get(statPath) == null) {
            createPlayerSection(player);
        }
        int currentAmount = statsConfig.getInt(statPath);
        statsConfig.set(statPath, currentAmount + increment);
        saveConfig(statsConfig);
    }

    public void reloadConfig() {
        FileConfiguration newConfig = YamlConfiguration.loadConfiguration(getStatsFile());

        final InputStream defConfigStream = ub.getResource("player-stats.yml");
        if (defConfigStream == null) {
            return;
        }

        newConfig.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream, Charsets.UTF_8)));
    }

    @NotNull
    private File getStatsFile() {
        return new File(ub.getDataFolder(), "player-stats.yml");
    }
}
