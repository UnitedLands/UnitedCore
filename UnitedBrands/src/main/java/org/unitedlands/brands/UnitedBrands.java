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
    public UnitedBrands() {
        plugin = this;
    }
    private final BreweriesFile breweriesFile = new BreweriesFile();
    private final PlayerStatsFile playersFile = new PlayerStatsFile();
    private final FileConfiguration playersConfig = playersFile.getStatsConfig();
    private final FileConfiguration breweriesConfig = breweriesFile.getBreweriesConfig();

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
        getCommand("breweryadmin").setExecutor(new BreweryAdminCommand(breweriesFile));
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
