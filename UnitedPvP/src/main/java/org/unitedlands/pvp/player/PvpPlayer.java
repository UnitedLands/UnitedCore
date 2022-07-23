package org.unitedlands.pvp.player;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.unitedlands.pvp.UnitedPvP;

import java.io.File;
import java.io.IOException;

public class PvpPlayer {
    private final UnitedPvP unitedPvP = getPlugin();
    private final Player player;
    private final File file;
    private final FileConfiguration playerConfig;

    public PvpPlayer(@NotNull Player player) {
        this.player = player;
        file = null;
        playerConfig = getFileConfiguration();
    }

    public PvpPlayer(File file) {
        player = Bukkit.getPlayer(file.getName().replace(".yml", ""));
        this.file = file;
        playerConfig = getFileConfiguration();
    }


    public void createFile() {
        File playerDataFile = getPlayerFile();
        if (!playerDataFile.exists()) {
            playerDataFile.getParentFile().mkdirs();
            try {
                playerDataFile.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        FileConfiguration fileConfiguration = new YamlConfiguration();
        try {
            fileConfiguration.load(playerDataFile);
            fileConfiguration.set("name", player.getName());
            fileConfiguration.set("hostility", 1);
            fileConfiguration.set("status", Status.PASSIVE.toString());
            fileConfiguration.set("can-degrade", true);
            saveConfig(fileConfiguration);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    private String getFilePath() {
        return File.separator + "players" + File.separator + player.getUniqueId() + ".yml";
    }

    public void saveConfig(FileConfiguration fileConfig) {
        try {
            fileConfig.save(getPlayerFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public FileConfiguration getFileConfiguration() {
        File playerStatsFile = getPlayerFile();
        FileConfiguration fileConfiguration = new YamlConfiguration();
        try {
            fileConfiguration.load(playerStatsFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
        return fileConfiguration;
    }

    public void updatePlayerHostility() {
        if (isPassive() || isHostile() || isVulnerable()) {
            setStatus(getStatus());
            return;
        }
        if (isAggressive() && isDegradable()) {
            if (getHostility() > 2) {
                setHostility(getHostility() - 1);
                setStatus(getStatus());
            } else {
                setHostility(0);
                setStatus(Status.PASSIVE);
            }
        }
    }
    public int getHostility() {
        return playerConfig.getInt("hostility");
    }

    public void setHostility(int value) {
        if (value >= 21) {
            playerConfig.set("hostility", 21);
            saveConfig(playerConfig);
            return;
        }
        playerConfig.set("hostility", value);
        saveConfig(playerConfig);
    }

    public void setStatus(Status status) {
        playerConfig.set("status", status.toString());
        saveConfig(playerConfig);
    }

    public boolean isHostile() {
        return getStatus().equals(Status.HOSTILE);
    }
    public boolean isAggressive() {
        return getStatus().equals(Status.AGGRESSIVE);
    }
    public boolean isPassive() {
        return getStatus().equals(Status.PASSIVE);
    }
    public boolean isVulnerable() {
        return getStatus().equals(Status.VULNERABLE);
    }
    public Status getStatus() {
        int hostility = getHostility();
        if (hostility == Status.PASSIVE.getStartingValue()) {
            return Status.PASSIVE;
        } else if (hostility == Status.VULNERABLE.getStartingValue()) {
            return Status.VULNERABLE;
        } else if (hostility >= Status.HOSTILE.getStartingValue()) {
            return Status.HOSTILE;
        } else {
            return Status.AGGRESSIVE;
        }
    }

    public String getIconHex(int hostility) {
        FileConfiguration config = unitedPvP.getConfig();
        return config.getString("hostility-color-stages." + hostility);
    }

    public boolean isDegradable() {
        return playerConfig.getBoolean("can-degrade");
    }

    public void setDegradable(boolean value) {
        playerConfig.set("can-degrade", value);
        saveConfig(playerConfig);
    }
    public static UnitedPvP getPlugin() {
        return (UnitedPvP) Bukkit.getPluginManager().getPlugin("UnitedPvP");
    }

    @NotNull
    public File getPlayerFile() {
        if (this.file != null) {
            return file;
        }
        return new File(unitedPvP.getDataFolder(), getFilePath());
    }
}
