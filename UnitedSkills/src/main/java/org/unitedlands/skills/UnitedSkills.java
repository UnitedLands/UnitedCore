package org.unitedlands.skills;

import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.unitedlands.skills.brewer.BrewerListener;
import org.unitedlands.skills.commands.BlendCommand;
import org.unitedlands.skills.farmer.FarmerListener;
import org.unitedlands.skills.fisherman.FishermanListener;
import org.unitedlands.skills.hunter.HunterListener;
import org.unitedlands.skills.miner.MinerListener;
import org.unitedlands.skills.woodcutter.WoodcutterListener;

public final class UnitedSkills extends JavaPlugin {
    @Override
    public void onEnable() {
        getCommand("blend").setExecutor(new BlendCommand(this));
        registerListeners();
        saveDefaultConfig();
    }

    private void registerListeners() {
        final BrewerListener brewerListener = new BrewerListener(this);
        final FarmerListener farmerListener = new FarmerListener(this);
        final HunterListener hunterListener = new HunterListener(this);
        final WoodcutterListener woodcutterListener = new WoodcutterListener(this, getCoreProtect());
        final FishermanListener fishermanListener = new FishermanListener(this);
        final MinerListener minerListener = new MinerListener(this, getCoreProtect());
        final PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(brewerListener, this);
        pluginManager.registerEvents(farmerListener, this);
        pluginManager.registerEvents(minerListener, this);
        pluginManager.registerEvents(hunterListener, this);
        pluginManager.registerEvents(fishermanListener, this);
        pluginManager.registerEvents(woodcutterListener, this);
        hunterListener.damageBleedingEntities();
    }

    public CoreProtectAPI getCoreProtect() {
        Plugin plugin = getServer().getPluginManager().getPlugin("CoreProtect");

        if (!(plugin instanceof CoreProtect)) {
            return null;
        }

        CoreProtectAPI CoreProtect = ((CoreProtect) plugin).getAPI();
        if (!CoreProtect.isEnabled()) {
            return null;
        }

        if (CoreProtect.APIVersion() < 9) {
            return null;
        }

        return CoreProtect;
    }
}
