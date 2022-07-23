package org.unitedlands.pvp;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.unitedlands.pvp.commands.PvPCmd;
import org.unitedlands.pvp.hooks.Placeholders;
import org.unitedlands.pvp.listeners.PlayerListener;
import org.unitedlands.pvp.listeners.TownyListener;
import org.unitedlands.pvp.util.Utils;

public final class UnitedPvP extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        registerListeners();
    }

    private void registerListeners() {
        Utils utils = new Utils(this);
        Bukkit.getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        Bukkit.getServer().getPluginManager().registerEvents(new TownyListener(this), this);
        getCommand("pvp").setExecutor(new PvPCmd(utils));

        // PlaceholderAPI Expansion Register
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new Placeholders(utils).register();
        }

    }


}
