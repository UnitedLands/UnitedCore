package org.unitedlands.transport;

import org.bukkit.plugin.java.JavaPlugin;
import org.unitedlands.transport.listeners.HorseListener;

public final class UnitedTransport extends JavaPlugin {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new HorseListener(this), this);
        saveDefaultConfig();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
