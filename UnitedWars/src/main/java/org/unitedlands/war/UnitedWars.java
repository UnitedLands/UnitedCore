package org.unitedlands.war;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.plugin.java.JavaPlugin;
import org.unitedlands.war.listeners.WarListener;

public final class UnitedWars extends JavaPlugin {
    public static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new WarListener(this), this);
        saveDefaultConfig();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
