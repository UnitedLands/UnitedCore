package org.unitedlands;

import org.bukkit.plugin.java.JavaPlugin;
import org.unitedlands.listeners.CalculationListener;
import org.unitedlands.listeners.StatusScreenListener;

public class UnitedLandsUpkeep extends JavaPlugin {

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        this.getServer().getPluginManager().registerEvents(new CalculationListener(this), this);
        this.getServer().getPluginManager().registerEvents(new StatusScreenListener(this), this);

    }

}
