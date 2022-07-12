package org.unitedlands.pvp;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.unitedlands.pvp.commands.PvPCmd;
import org.unitedlands.pvp.hooks.Placeholders;
import org.unitedlands.pvp.listeners.PlayerListener;
import org.unitedlands.pvp.util.Utils;

import java.io.File;
import java.io.IOException;

public final class UnitedPvP extends JavaPlugin {
    private FileConfiguration playerDataConfig;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        registerListeners();
    }

    private void registerListeners() {
        Utils utils = new Utils(this);
        PlayerListener playerListener = new PlayerListener(this, utils);
        Bukkit.getServer().getPluginManager().registerEvents(playerListener, this);
        getCommand("pvp").setExecutor(new PvPCmd(utils));

        // PlaceholderAPI Expansion Register
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new Placeholders(utils).register();
        }
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
            playerDataConfig.set("PvP", false);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    public FileConfiguration getPlayerConfig(Player player) {
        File playerDataFile = new File(getDataFolder(), getFilePath(player));
        playerDataConfig = new YamlConfiguration();
        try {
            playerDataConfig.load(playerDataFile);
        } catch (IOException | InvalidConfigurationException e) {
            createPlayerFile(player);
            e.printStackTrace();
        }
        return playerDataConfig;
    }

    public File getPlayerFile(Player player) {
        return new File(getDataFolder(), getFilePath(player));
    }


    private String getFilePath(Player player) {
        return File.separator + "players" + File.separator + player.getUniqueId().toString() + ".yml";
    }

}
