package org.unitedlands.transport;

import org.bukkit.plugin.java.JavaPlugin;
import org.unitedlands.transport.listeners.HorseListener;
import org.unitedlands.transport.listeners.MiscListener;

public final class UnitedTransport extends JavaPlugin {
    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new HorseListener(this), this);
        getServer().getPluginManager().registerEvents(new MiscListener(this), this);
        saveDefaultConfig();
    }
}
