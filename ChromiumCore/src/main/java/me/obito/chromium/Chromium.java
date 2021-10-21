package me.obito.chromium;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.LogRecord;

public final class Chromium extends JavaPlugin {


    @Override
    public void onEnable() {

        Bukkit.getServer().getPluginManager().registerEvents(new EventListener(), this);
        System.out.println(ChatColor.GREEN + "--------------------");
        System.out.println(ChatColor.YELLOW + "Enabling ChromiumCore main plugin...");
        System.out.println(ChatColor.GREEN + "--------------------");
        System.out.println(ChatColor.AQUA + "Loading extensions...");

    }

    @Override
    public void onDisable() {

        System.out.println(ChatColor.GREEN + "--------------------");
        System.out.println(ChatColor.DARK_AQUA + "Disabling ChromiumCore main plugin...");
        System.out.println(ChatColor.GREEN + "--------------------");
        System.out.println(ChatColor.RED + "Disabling extensions...");

    }
}
