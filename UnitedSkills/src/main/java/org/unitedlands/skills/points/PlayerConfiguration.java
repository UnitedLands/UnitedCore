package org.unitedlands.skills.points;

import com.gamingmesh.jobs.Jobs;
import com.gamingmesh.jobs.container.JobProgression;
import com.gamingmesh.jobs.container.JobsPlayer;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.unitedlands.skills.UnitedSkills;

import java.io.File;
import java.io.IOException;

public class PlayerConfiguration {
    private final UnitedSkills unitedSkills;
    private final @NotNull OfflinePlayer player;
    private FileConfiguration fileConfiguration;

    public PlayerConfiguration(UnitedSkills unitedSkills, @NotNull OfflinePlayer player) {
        this.unitedSkills = unitedSkills;
        this.player = player;
    }

    public void createFile() {
        File playerDataFile = getFile();
        if (!playerDataFile.exists()) {
            playerDataFile.getParentFile().mkdirs();
            if (!playerDataFile.exists()) {
                try {
                    playerDataFile.createNewFile();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        fileConfiguration = new YamlConfiguration();
        try {
            fileConfiguration.load(playerDataFile);
            fileConfiguration.set("name", player.getName());
            fileConfiguration.set("brewer-points", getJobsLevel("Brewer"));
            fileConfiguration.set("digger-points", getJobsLevel("Digger"));
            fileConfiguration.set("farmer-points", getJobsLevel("Farmer"));
            fileConfiguration.set("fisherman-points", getJobsLevel("Fisherman"));
            fileConfiguration.set("hunter-points", getJobsLevel("Hunter"));
            fileConfiguration.set("miner-points", getJobsLevel("Miner"));
            fileConfiguration.set("woodcutter-points", getJobsLevel("Woodcutter"));
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
            fileConfig.save(getFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public FileConfiguration getFileConfiguration() {
        File playerStatsFile = getFile();
        fileConfiguration = new YamlConfiguration();
        try {
            fileConfiguration.load(playerStatsFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
        return fileConfiguration;
    }

    @NotNull
    public File getFile() {
        return new File(unitedSkills.getDataFolder(), getFilePath());
    }

    public void increaseJobPoints(String jobName, int amount) {
        FileConfiguration configuration = getFileConfiguration();
        int currentPoints = configuration.getInt(jobName.toLowerCase() + "-points");
        configuration.set(jobName.toLowerCase() + "-points", currentPoints + amount);
        saveConfig(configuration);
    }

    public void decreaseJobPoints(String jobName, int amount) {
        FileConfiguration configuration = getFileConfiguration();
        int newPoints = configuration.getInt(jobName.toLowerCase() + "-points") - amount;
        if (newPoints < 0) {
            return;
        }
        configuration.set(jobName.toLowerCase() + "-points", newPoints);
        saveConfig(configuration);
    }

    private int getJobsLevel(String jobName) {
        JobsPlayer jobsPlayer = Jobs.getPlayerManager().getJobsPlayer(player.getPlayer());
        if (jobsPlayer == null) {
            return 0;
        }
        for (JobProgression job : jobsPlayer.getJobProgression()) {
            if (job.getJob().getName().equals(jobName)) {
                return job.getLevel();
            }
        }
        return 0;
    }
}

