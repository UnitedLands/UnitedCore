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


    @Override
    public void onEnable() {


        System.out.println(ChatColor.GREEN + "--------------------");
        System.out.println(ChatColor.YELLOW + "Enabling ChromiumCore main plugin...");
        System.out.println(ChatColor.GREEN + "--------------------");
        System.out.println(ChatColor.AQUA + "Loading extensions...");
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
