package me.obito.chromium;

import me.obito.chromium.commands.ChromiumMainCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.logging.LogRecord;

public final class Chromium extends JavaPlugin {

    FileConfiguration config;
    File customMsgFile;
    FileConfiguration customConfig;

    @Override
    public void onEnable() {


        customMsgFile = new File(Bukkit.getPluginManager().getPlugin("ChromiumFinal").getDataFolder(), "messages.yml");
        if (!customMsgFile.exists()) {
            customMsgFile.getParentFile().mkdirs();
            try {
                customMsgFile.createNewFile();
                customConfig = new YamlConfiguration();
                customConfig.load(customMsgFile);
                saveResource("messages.yml", false);
                customConfig.save(customMsgFile);

            } catch (Exception e1){
                System.out.println("EXCEPTION: CANT CREATE NEW FILE OR LOAD IT");
            }
            //Bukkit.getPluginManager().getPlugin("ChromiumCore").saveResource(e.getPlayer().getUniqueId() + ".yml", false);
        }

        System.out.println(ChatColor.GREEN + "--------------------");
        System.out.println(ChatColor.YELLOW + "Enabling ChromiumCore main plugin...");
        System.out.println(ChatColor.GREEN + "--------------------");
        System.out.println(ChatColor.AQUA + "Loading extensions...");
        config = Bukkit.getPluginManager().getPlugin("ChromiumBroadcast").getConfig();
        Bukkit.getPluginManager().getPlugin("ChromiumFinal").saveDefaultConfig();
        this.getCommand("chromium").setExecutor(new ChromiumMainCommand());

    }

    @Override
    public void onDisable() {

        System.out.println(ChatColor.GREEN + "--------------------");
        System.out.println(ChatColor.DARK_AQUA + "Disabling ChromiumCore main plugin...");
        System.out.println(ChatColor.GREEN + "--------------------");
        System.out.println(ChatColor.RED + "Disabling extensions...");

    }

}
