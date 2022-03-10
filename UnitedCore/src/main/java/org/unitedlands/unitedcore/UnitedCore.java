package org.unitedlands.unitedcore;

import org.unitedlands.unitedcore.commands.UnitedCoreMainCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public final class UnitedCore extends JavaPlugin {


    @Override
    public void onEnable() {


        System.out.println(ChatColor.GREEN + "" + ChatColor.STRIKETHROUGH + "                              ");
        System.out.println(ChatColor.YELLOW + "Enabling UnitedCore main plugin...");
        System.out.println(ChatColor.GREEN + "" + ChatColor.STRIKETHROUGH + "                              ");
        System.out.println(ChatColor.AQUA + "Loading extensions...");
        Bukkit.getPluginManager().getPlugin("UnitedCore").saveDefaultConfig();
        this.getCommand("UnitedCore").setExecutor(new UnitedCoreMainCommand());


    }

    @Override
    public void onDisable() {

        System.out.println(ChatColor.GREEN + "" + ChatColor.STRIKETHROUGH + "                              ");
        System.out.println(ChatColor.DARK_AQUA + "Disabling UnitedCore main plugin...");
        System.out.println(ChatColor.GREEN + "" + ChatColor.STRIKETHROUGH + "                              ");
        System.out.println(ChatColor.RED + "Disabling extensions...");

    }

}
