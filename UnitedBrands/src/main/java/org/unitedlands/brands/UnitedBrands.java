package org.unitedlands.brands;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.unitedlands.brands.brewery.BreweriesFile;
import org.unitedlands.brands.commands.BreweryAdminCommand;
import org.unitedlands.brands.commands.BreweryCommand;
import org.unitedlands.brands.hooks.Placeholders;
import org.unitedlands.brands.listeners.PlayerListener;
import org.unitedlands.brands.stats.PlayerStatsFile;

public final class UnitedBrands extends JavaPlugin {
    private static UnitedBrands plugin;
    private final BreweriesFile breweriesFile;
    private final PlayerStatsFile playersFile;
    private final FileConfiguration playersConfig;
    private final FileConfiguration breweriesConfig;
    public UnitedBrands() {
        plugin = this;
        this.breweriesFile = new BreweriesFile();
        this.playersFile = new PlayerStatsFile();
        this.breweriesConfig = breweriesFile.getBreweriesConfig();
        this.playersConfig = playersFile.getStatsConfig();
    }

    public static UnitedBrands getInstance() {
        return plugin;
    }

    @Override
    public void onEnable() {
        generateFiles();
        registerCommands();
        registerPlaceholderExpansion();
        registerListener();
        BreweryDatabase.load();
    }

    @Override
    public void onDisable() {
        BreweryDatabase.save();
    }

    private void registerListener() {
        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
    }

    private void registerPlaceholderExpansion() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new Placeholders().register();
        }
    }

    private void registerCommands() {
        getCommand("brewery").setExecutor(new BreweryCommand());
        getCommand("breweryadmin").setExecutor(new BreweryAdminCommand());
    }

    private void generateFiles() {
        breweriesFile.createBreweriesFile();
        playersFile.createStatsFile();
        saveDefaultConfig();
    }

    public FileConfiguration getBreweriesConfig() {
        return breweriesConfig;
    }

    public FileConfiguration getPlayerStatsConfig() {
        return playersConfig;
    }

}
