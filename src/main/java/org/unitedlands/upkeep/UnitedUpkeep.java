package org.unitedlands.upkeep;

import org.bukkit.plugin.java.JavaPlugin;
import org.unitedlands.upkeep.listeners.CalculationListener;
import org.unitedlands.upkeep.listeners.StatusScreenListener;

public class UnitedUpkeep extends JavaPlugin {

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        this.getServer().getPluginManager().registerEvents(new CalculationListener(this), this);
        this.getServer().getPluginManager().registerEvents(new StatusScreenListener(this), this);

    }

}
