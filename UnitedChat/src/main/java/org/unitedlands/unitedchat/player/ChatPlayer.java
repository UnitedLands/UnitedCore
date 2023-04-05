package org.unitedlands.unitedchat.player;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.unitedlands.unitedchat.UnitedChat;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class ChatPlayer {
    private final UUID uuid;
    private final File dataFolder = UnitedChat.getPlugin().getDataFolder();
    private FileConfiguration playerDataConfig;

    public ChatPlayer(UUID uuid) {
        this.uuid = uuid;
    }

    public String getGradient() {
        return getPlayerConfig().getString("gradient");
    }

    public void setGradient(String gradient) {
        FileConfiguration playerConfig = getPlayerConfig();
        File file = getPlayerFile();
        playerConfig.set("gardient", gradient);
        try {
            playerConfig.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isGradientEnabled() {
        if (getGradient() == null)
            return false;
        return getPlayerConfig().getBoolean("gradient-enabled");
    }

    public void setGradientEnabled(boolean toggle) {
        FileConfiguration playerConfig = getPlayerConfig();
        File file = getPlayerFile();
        playerConfig.set("gradient-enabled", toggle);
        try {
            playerConfig.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createPlayerFile() {
        File playerDataFile = new File(dataFolder, getFilePath());
        if (!playerDataFile.exists()) {
            playerDataFile.getParentFile().mkdirs();
            try {
                playerDataFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        playerDataConfig = new YamlConfiguration();
        try {
            playerDataConfig.load(playerDataFile);
            playerDataConfig.set("gradient-enabled", false);
            playerDataConfig.set("gradient", "#ffffff:#ffffff");
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    public FileConfiguration getPlayerConfig() {
        File playerDataFile = new File(dataFolder, getFilePath());
        playerDataConfig = new YamlConfiguration();
        if (!playerDataFile.exists()) {
            return null;
        }
        try {
            playerDataConfig.load(playerDataFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
        return playerDataConfig;
    }

    public File getPlayerFile() {
        return new File(dataFolder, getFilePath());
    }


    private String getFilePath() {
        return "players" + File.separator + uuid.toString() + ".yml";
    }
}
