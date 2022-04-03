package org.unitedlands.brands;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.unitedlands.brands.brewery.BreweriesFile;
import org.unitedlands.brands.commands.BreweryAdminCommand;
import org.unitedlands.brands.commands.BreweryCommand;
import org.unitedlands.brands.hooks.Placeholders;
import org.unitedlands.brands.listeners.PlayerListener;

public final class UnitedBrands extends JavaPlugin {
    BreweriesFile breweriesFile = new BreweriesFile(this);

    @Override
    public void onEnable() {
        generateFiles();
        registerCommands();
        registerPlaceholderExpansion();
        registerListener();
    }

    private void registerListener() {
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
    }

    private void registerPlaceholderExpansion() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new Placeholders(this).register();
        }
    }

    private void registerCommands() {
        getCommand("brewery").setExecutor(new BreweryCommand(this));
        getCommand("breweryadmin").setExecutor(new BreweryAdminCommand(this, breweriesFile));
    }

    private void generateFiles() {
        breweriesFile.createBreweriesFile();
        saveDefaultConfig();
    }

}
