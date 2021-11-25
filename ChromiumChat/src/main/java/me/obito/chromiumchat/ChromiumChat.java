package me.obito.chromiumchat;

import me.obito.chromiumchat.commands.ClearChatCmd;
import me.obito.chromiumchat.commands.GradientCmd;
import me.obito.chromiumchat.gradient.GradientPresets;
import me.obito.chromiumchat.player.PlayerListener;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class ChromiumChat extends JavaPlugin {

    File customConfigFile;
    FileConfiguration customConfig;
    public static FileConfiguration Config1;
    int i = 1;

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

                List<String> motd = new ArrayList<>();

                motd.add("&aWelcome");
                motd.add("&bto");
                motd.add("&cUnitedLands");

                customConfig.set("Motd", motd);
                customConfig.set("NoPerm", "&cYou don't have permission.");
                customConfig.set("ChatCleared", "&bGlobal Chat Cleared.");
                customConfig.set("ConfError", "&cError with configuration.");
                customConfig.set("GradientChanged", "&eGradient changed.");
                customConfig.set("GradientOn", "&eGradient enabled.");
                customConfig.set("GradientOff", "&eGradient disabled.");
                customConfig.set("GradientUnknownPreset", "&eGradient preset not recognized.");
                customConfig.set("InCombat", "&cYou can't use that command while in combat.");
                customConfig.set("PvPDisabled", "&ePvP disabled.");
                customConfig.set("PvPEnabled", "&ePvP enabled.");
                customConfig.set("PvPEnabledOp", "&ePvP enabled for player.");
                customConfig.set("PvPDisabledOp", "&ePvP disabled for player.");
                customConfig.set("PvPEnabledByAdmin", "&ePvP enabled by admin.");
                customConfig.set("PvPDisabledByAdmin", "&ePvP disabled by admin.");
                customConfig.set("PvPAlreadyOn", "&cPvP already enabled.");
                customConfig.set("PvPAlreadyOff", "&cPvP already disabled.");
                customConfig.set("PvPStatusOn", "&ePvP status: ON");
                customConfig.set("PvPStatusOff", "&cPvP status: OFF");
                customConfig.set("PlayerNotRecognized", "&cCan't recognize player.");
                customConfig.set("FFTownDisabled", "&6Friendly Fire in town disabled.");
                customConfig.set("FFTownEnabled", "&6Friendly Fire in town enabled.");
                customConfig.set("ReceivedSapling", "&eYou have received a sapling.");
                customConfig.set("InvalidTree", "&cInvalid tree.");
                customConfig.save(customConfigFile);

            } catch (Exception e1){
                System.out.println("EXCEPTION: CANT CREATE NEW FILE OR LOAD IT");
            }



            //Bukkit.getPluginManager().getPlugin("ChromiumCore").saveResource(e.getPlayer().getUniqueId() + ".yml", false);
        }
        int seconds = Config1.getInt("TimeInSeconds");
        BukkitScheduler scheduler = getServer().getScheduler();
        scheduler.scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {

                try {
                    Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', Config1.getString("Message " + i)));
                    i++;
                } catch (Exception e1){
                    i = 2;
                    Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', Config1.getString("Message 1")));
                }

            }
        }, 0L, 20L*seconds);

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

    public static List<String> getList(String s){
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

        return customConfig.getStringList(s);

    }



    @Override
    public void onDisable(){

    }
}
