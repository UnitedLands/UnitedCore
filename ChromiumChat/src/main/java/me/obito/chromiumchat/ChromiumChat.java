package me.obito.chromiumchat;

import me.obito.chromiumchat.commands.ClearChatCmd;
import me.obito.chromiumchat.commands.GradientCmd;
import me.obito.chromiumchat.gradient.GradientPresets;
import me.obito.chromiumchat.player.PlayerListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class ChromiumChat extends JavaPlugin {

    File customConfigFile;
    FileConfiguration customConfig;
    public static FileConfiguration Config1;

    @Override
    public void onEnable(){

        Bukkit.getPluginManager().getPlugin("ChromiumChat").saveDefaultConfig();
        Config1 = getConfig();
        Bukkit.getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        this.getCommand("gradient").setExecutor(new GradientCmd());
        this.getCommand("cc").setExecutor(new ClearChatCmd());
        GradientPresets.loadPredefinedGradients(Config1);


        customConfigFile = new File(Bukkit.getPluginManager().getPlugin("ChromiumFinal").getDataFolder(), "messages.yml");
        if (!customConfigFile.exists()) {
            customConfigFile.getParentFile().mkdirs();

            try {

                customConfigFile.createNewFile();
                customConfig = new YamlConfiguration();
                customConfig.load(customConfigFile);
                customConfig.set("NoPerm", "&cYou don't have permission.");
                customConfig.set("ChatCleared", "&bGlobal Chat Cleared.");
                customConfig.set("ConfError", "&cError with configuration.");
                customConfig.set("GradientChanged", "&eGradient changed.");
                customConfig.set("GradientOn", "&eGradient enabled.");
                customConfig.set("GradientOff", "&eGradient disabled.");
                customConfig.set("GradientUnknownPreset", "&eGradient preset not recognized.");
                customConfig.set("InCombat", "&cYou can't use that command while in combat.");
                customConfig.set("PvPDisabled", "&ePvP Disabled.");
                customConfig.set("PvPEnabled", "&ePvP Enabled.");
                customConfig.save(customConfigFile);

            } catch (Exception e1){
                System.out.println("EXCEPTION: CANT CREATE NEW FILE OR LOAD IT");
            }
            //Bukkit.getPluginManager().getPlugin("ChromiumCore").saveResource(e.getPlayer().getUniqueId() + ".yml", false);
        }


    }

    public static FileConfiguration getConfigur(){
        return Config1;
    }

    public static String getMsg(String s){
        File customConfigFile;
        customConfigFile = new File(Bukkit.getPluginManager().getPlugin("ChromiumFinal").getDataFolder(),
                "messages.yml");
        FileConfiguration customConfig;
        customConfig = new YamlConfiguration();
        try{
            customConfig.load(customConfigFile);
        } catch (Exception e2){
            System.out.println("Error with loading messages.");
        }

            return customConfig.getString(s);

    }



    @Override
    public void onDisable(){

    }
}
