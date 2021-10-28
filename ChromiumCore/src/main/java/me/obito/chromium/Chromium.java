package me.obito.chromium;

import me.obito.chromium.commands.ChromiumMainCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.LogRecord;

public final class Chromium extends JavaPlugin {

    FileConfiguration config;

    @Override
    public void onEnable() {

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
