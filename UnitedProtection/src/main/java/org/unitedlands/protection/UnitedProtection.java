package org.unitedlands.protection;

import org.bukkit.plugin.java.JavaPlugin;
import org.unitedlands.protection.listeners.PlayerListener;

public final class UnitedProtection extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
    }

}
