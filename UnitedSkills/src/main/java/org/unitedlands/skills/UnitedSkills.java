package org.unitedlands.skills;

import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.unitedlands.skills.brewer.BlendingGui;
import org.unitedlands.skills.brewer.BrewerListener;
import org.unitedlands.skills.commands.BlendCommand;
import org.unitedlands.skills.farmer.FarmerListener;

public final class UnitedSkills extends JavaPlugin {
    @Override
    public void onEnable() {
        getCommand("blend").setExecutor(new BlendCommand(new BlendingGui(this)));
        registerListeners();
        saveDefaultConfig();
    }

    private void registerListeners() {
        final BrewerListener brewerListener = new BrewerListener(this);
        final FarmerListener farmerListener = new FarmerListener(this);
        final PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(brewerListener, this);
        pluginManager.registerEvents(farmerListener, this);
    }
}
