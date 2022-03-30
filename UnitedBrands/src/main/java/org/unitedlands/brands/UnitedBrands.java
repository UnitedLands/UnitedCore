package org.unitedlands.brands;

import org.bukkit.plugin.java.JavaPlugin;
import org.unitedlands.brands.brewery.BreweriesFile;
import org.unitedlands.brands.commands.BreweryAdminCommand;
import org.unitedlands.brands.commands.BreweryCommand;
import org.unitedlands.brands.listeners.PlayerListener;

public final class UnitedBrands extends JavaPlugin {

    @Override
    public void onEnable() {
        BreweriesFile breweriesFile = new BreweriesFile(this);
        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        breweriesFile.createBreweriesFile();
        saveDefaultConfig();
        getCommand("brewery").setExecutor(new BreweryCommand(this));
        getCommand("breweryadmin").setExecutor(new BreweryAdminCommand(this, breweriesFile));
    }

}
