package me.obito.chromiumchat;

import me.obito.chromiumchat.commands.ClearChatCmd;
import me.obito.chromiumchat.commands.GradientCmd;
import me.obito.chromiumchat.player.ChatListener;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import com.palmergames.bukkit.towny.TownyAPI;

import java.io.File;

public class ChromiumChat extends JavaPlugin {

    File configFile;
    public static FileConfiguration Config;

    @Override
    public void onEnable(){
        Bukkit.getPluginManager().getPlugin("ChromiumChat").saveDefaultConfig();
        Config = Bukkit.getPluginManager().getPlugin("ChromiumChat").getConfig();
        Bukkit.getServer().getPluginManager().registerEvents(new ChatListener(), this);
        this.getCommand("gradient").setExecutor(new GradientCmd());
        this.getCommand("cc").setExecutor(new ClearChatCmd());
    }

    public static FileConfiguration getConfigur(){
        return Config;
    }

    @Override
    public void onDisable(){

    }
}
