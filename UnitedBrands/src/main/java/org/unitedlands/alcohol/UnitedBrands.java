package org.unitedlands.alcohol;

import org.bukkit.plugin.java.JavaPlugin;
import org.unitedlands.alcohol.brand.Brand;
import org.unitedlands.alcohol.brand.BrandsFile;
import org.unitedlands.alcohol.commands.BrandCommand;
import org.unitedlands.alcohol.listeners.PlayerListener;

public final class UnitedBrands extends JavaPlugin {

    @Override
    public void onEnable() {
        BrandsFile brandsFile = new BrandsFile(this);
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        brandsFile.createBrandsFile();
        saveDefaultConfig();
        getCommand("brand").setExecutor(new BrandCommand(this));
    }

}
