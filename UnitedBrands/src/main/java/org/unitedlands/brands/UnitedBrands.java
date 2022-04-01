package org.unitedlands.brands;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.unitedlands.brands.brewery.BreweriesFile;
import org.unitedlands.brands.commands.BreweryAdminCommand;
import org.unitedlands.brands.commands.BreweryCommand;
import org.unitedlands.brands.hooks.Placeholders;
import org.unitedlands.brands.listeners.PlayerListener;
import org.unitedlands.brands.stats.PlayerStatsFile;

public final class UnitedBrands extends JavaPlugin {
    BreweriesFile breweriesFile = new BreweriesFile(this);
    PlayerStatsFile playerStatsFile = new PlayerStatsFile(this);

    @Override
    public void onEnable() {
        generateFiles();
        registerCommands();
        registerPlaceholderExpansion();
        registerListener();
    }

    private void registerListener() {
        getServer().getPluginManager().registerEvents(new PlayerListener(playerStatsFile), this);
    }

    private void registerPlaceholderExpansion() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new Placeholders(playerStatsFile).register();
        }
    }

    private void registerCommands() {
        getCommand("brewery").setExecutor(new BreweryCommand(this));
        getCommand("breweryadmin").setExecutor(new BreweryAdminCommand(this, breweriesFile, playerStatsFile));
    }

    private void generateFiles() {
        breweriesFile.createBreweriesFile();
        playerStatsFile.createStatsFile();
        saveDefaultConfig();
    }

}
