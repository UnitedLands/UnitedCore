package org.unitedlands.unitedchat.player;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
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

    public void toggleChatFeature(ChatFeature feature, boolean toggle) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null)
            return;
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(UnitedChat.getPlugin(), feature.toString());
        if (toggle) {
            pdc.set(key, PersistentDataType.INTEGER, 1); // 1 == true, 0 == false
        } else {
            pdc.set(key, PersistentDataType.INTEGER, 0);
        }
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
