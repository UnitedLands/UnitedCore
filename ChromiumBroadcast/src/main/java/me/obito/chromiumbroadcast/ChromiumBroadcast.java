package me.obito.chromiumbroadcast;

import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public final class ChromiumBroadcast extends JavaPlugin {

    int i = 1;

    private File customConfigFile;
    private FileConfiguration customConfig;

    JavaPlugin pl = this;

    @Override
    public void onEnable() {
    createCustomConfig();
    int seconds = customConfig.getInt("TimeInSeconds");
        BukkitScheduler scheduler = getServer().getScheduler();
        scheduler.scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {

                try {
                    Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', customConfig.getString("Message " + i)));
                    i++;
                } catch (Exception e1){
                    i = 2;
                    Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', customConfig.getString("Message 1")));
                }

            }
        }, 0L, 20L*seconds);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public FileConfiguration getCustomConfig() {
        return this.customConfig;
    }



    private void createCustomConfig() {
        customConfigFile = new File(getDataFolder(), "messages.yml");
        if (!customConfigFile.exists()) {
            customConfigFile.getParentFile().mkdirs();
            saveResource("messages.yml", false);
        }

        customConfig= new YamlConfiguration();




        try {
            customConfig.load(customConfigFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

}
