package org.unitedlands.alcohol;

import org.bukkit.plugin.java.JavaPlugin;
import org.unitedlands.alcohol.commands.BrandCommand;
import org.unitedlands.alcohol.listeners.PlayerListener;

public final class UnitedBrands extends JavaPlugin {


    @Override
    public void onEnable() {
        Brand brand = new Brand(this);
        getServer().getPluginManager().registerEvents(new PlayerListener(brand), this);
        brand.createBrandsFile();
        getCommand("brand").setExecutor(new BrandCommand(brand));
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
