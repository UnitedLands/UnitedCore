package org.unitedlands.unitedchat;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.unitedlands.unitedchat.commands.ClearChatCmd;
import org.unitedlands.unitedchat.commands.GradientCmd;
import org.unitedlands.unitedchat.player.PlayerListener;

import java.io.File;
import java.io.IOException;

public class UnitedChat extends JavaPlugin {

    private FileConfiguration playerDataConfig;

    public static String getMsg(String name, FileConfiguration config) {
        String message = config.getString("messages." + name);
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    @Override
    public void onEnable() {

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null) {
            getLogger().warning("[Exception] PlaceholderAPI is required!");
            Bukkit.getPluginManager().disablePlugin(this);
        }
        getServer().getPluginManager().registerEvents(new PlayerListener(this, new Formatter()), this);
        this.getCommand("gradient").setExecutor(new GradientCmd(this));
        this.getCommand("cc").setExecutor(new ClearChatCmd());
        saveDefaultConfig();

    }

    public void createPlayerFile(Player player) {
        File playerDataFile = new File(getDataFolder(), getFilePath(player));
        if (!playerDataFile.exists()) {
            playerDataFile.getParentFile().mkdirs();
            saveResource(getFilePath(player), false);
        }
        playerDataConfig = new YamlConfiguration();
        try {
            playerDataConfig.load(playerDataFile);
            playerDataConfig.set("Player Name", player.getName());
            playerDataConfig.set("GradientEnabled", false);
            playerDataConfig.set("Gradient", "#ffffff:#ffffff");
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    public FileConfiguration getPlayerConfig(Player player) {
        File playerDataFile = new File(getDataFolder(), getFilePath(player));
        playerDataConfig = new YamlConfiguration();
        if (!playerDataFile.exists()) {
            createPlayerFile(player);
            saveResource(getFilePath(player), false);
        }
        try {
            playerDataConfig.load(playerDataFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
        return playerDataConfig;
    }

    public File getPlayerFile(Player player) {
        return new File(getDataFolder(), getFilePath(player));
    }


    private String getFilePath(Player player) {
        return "players" + File.separator + player.getUniqueId().toString() + ".yml";
    }
}