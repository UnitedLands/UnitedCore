package me.obito.chromiumchat;

import me.obito.chromiumchat.commands.ClearChatCmd;
import me.obito.chromiumchat.commands.GradientCmd;
import me.obito.chromiumchat.gradient.GradientPresets;
import me.obito.chromiumchat.player.PlayerListener;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class ChromiumChat extends JavaPlugin {

    File configFile;
    public static FileConfiguration Config1;
    public static FileConfiguration customConfig;
    public static File customMsgFile;

    @Override
    public void onEnable(){

        customMsgFile = new File(Bukkit.getPluginManager().getPlugin("ChromiumFinal").getDataFolder(), "messages.yml");

        try {
            customConfig = new YamlConfiguration();
            customConfig.load(customMsgFile);

        } catch (Exception e1){
            System.out.println("EXCEPTION: CANT CREATE NEW FILE OR LOAD IT");
        }
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


    public static FileConfiguration getMessages(){
        try {
            customConfig.load(customMsgFile);
        } catch (Exception e1){
            System.out.println("EXCEPTION: CANT CREATE NEW FILE OR LOAD IT");
        }
        return customConfig;
    }



    @Override
    public void onDisable(){

    }
}
