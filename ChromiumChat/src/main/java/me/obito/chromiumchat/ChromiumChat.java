package me.obito.chromiumchat;

import me.obito.chromiumchat.commands.ClearChatCmd;
import me.obito.chromiumchat.commands.GradientCmd;
import me.obito.chromiumchat.gradient.GradientPresets;
import me.obito.chromiumchat.player.PlayerListener;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class ChromiumChat extends JavaPlugin {

    File configFile;
    public static FileConfiguration Config1;

    @Override
    public void onEnable(){

        Bukkit.getPluginManager().getPlugin("ChromiumChat").saveDefaultConfig();
        Config1 = getConfig();
        Bukkit.getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        this.getCommand("gradient").setExecutor(new GradientCmd());
        this.getCommand("cc").setExecutor(new ClearChatCmd());
        GradientPresets.loadPredefinedGradients(Config1);

    }

    public static FileConfiguration getConfigur(){
        return Config1;
    }



    @Override
    public void onDisable(){

    }
}
