package org.unitedlands.war;

import com.palmergames.bukkit.towny.TownyAPI;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.unitedlands.war.commands.WarCommand;
import org.unitedlands.war.listeners.BookListener;
import org.unitedlands.war.listeners.PlayerListener;
import org.unitedlands.war.listeners.WarListener;

public final class UnitedWars extends JavaPlugin {
    public static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    public static final TownyAPI TOWNY_API = TownyAPI.getInstance();

    @Override
    public void onEnable() {
        registerListener(new WarListener(this));
        registerListener(new PlayerListener(this));
        registerListener(new BookListener(this));
        getCommand("war").setExecutor(new WarCommand());
        saveDefaultConfig();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private void registerListener(Listener listener) {
        getServer().getPluginManager().registerEvents(listener, this);
    }
}
