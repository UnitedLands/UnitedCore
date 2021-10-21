package me.obito.chromium;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class Chromium extends JavaPlugin {

    @Override
    public void onEnable() {

        Bukkit.getServer().getPluginManager().registerEvents(new EventListener(), this);
        Bukkit.getServer().getPluginManager().registerEvents(new GradientListener(), this);

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
