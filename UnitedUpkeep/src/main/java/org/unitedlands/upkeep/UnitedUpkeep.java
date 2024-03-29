package org.unitedlands.upkeep;

import com.palmergames.bukkit.towny.TownyCommandAddonAPI;
import com.palmergames.bukkit.towny.TownyCommandAddonAPI.CommandType;
import org.bukkit.plugin.java.JavaPlugin;
import org.unitedlands.upkeep.commands.OfficialNationCommand;
import org.unitedlands.upkeep.listeners.CalculationListener;
import org.unitedlands.upkeep.listeners.StatusScreenListener;

public class UnitedUpkeep extends JavaPlugin {
    public UnitedUpkeep() {
    }

    public void onEnable() {
        this.saveDefaultConfig();
        this.getServer().getPluginManager().registerEvents(new CalculationListener(this), this);
        this.getServer().getPluginManager().registerEvents(new StatusScreenListener(this), this);
        new OfficialNationCommand();
    }

    public void onDisable() {
        TownyCommandAddonAPI.removeSubCommand(CommandType.TOWNYADMIN, "officialnation");
    }
}
