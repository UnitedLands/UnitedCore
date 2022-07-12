package org.unitedlands.skills;

import de.Linus122.SafariNet.API.SafariNet;
import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.unitedlands.skills.commands.BlendCommand;
import org.unitedlands.skills.commands.PointsCommand;
import org.unitedlands.skills.commands.UnitedSkillsComand;
import org.unitedlands.skills.guis.BiomeKit;
import org.unitedlands.skills.hooks.UnitedSkillsPlaceholders;
import org.unitedlands.skills.jobs.BrewerAbilities;
import org.unitedlands.skills.jobs.DiggerAbilities;
import org.unitedlands.skills.jobs.FarmerAbilities;
import org.unitedlands.skills.jobs.FishermanAbilities;
import org.unitedlands.skills.jobs.HunterAbilities;
import org.unitedlands.skills.jobs.MasterworkAbilities;
import org.unitedlands.skills.jobs.MinerAbilities;
import org.unitedlands.skills.jobs.WoodcutterAbilities;
import org.unitedlands.skills.points.JobsListener;
import org.unitedlands.skills.safarinets.SafariNetListener;
import org.unitedlands.skills.skill.SkillFile;

public final class UnitedSkills extends JavaPlugin {
    @Override
    public void onEnable() {
        registerCommands();
        registerListeners();
        registerPlaceholderExpansion();
        saveDefaultConfig();
        SkillFile skillFile = new SkillFile(this);
        skillFile.createSkillsFile();
    }

    private void registerCommands() {
        getCommand("blend").setExecutor(new BlendCommand(this));
        getCommand("unitedskills").setExecutor(new UnitedSkillsComand(this));
        getCommand("points").setExecutor(new PointsCommand(this));
    }

    private void registerListeners() {
        final Listener[] listeners = {
                new JobsListener(this),
                new BrewerAbilities(this),
                new FarmerAbilities(this),
                new HunterAbilities(this),
                new DiggerAbilities(this, getCoreProtect()),
                new WoodcutterAbilities(this, getCoreProtect()),
                new FishermanAbilities(this),
                new MinerAbilities(this, getCoreProtect()),
                new BiomeKit(this),
                new MasterworkAbilities(this)
        };

        registerEvents(listeners);

        SafariNet.addListener(new SafariNetListener(this));

        final HunterAbilities hunterAbilities = new HunterAbilities(this);
        hunterAbilities.damageBleedingEntities();

        final MasterworkAbilities masterworkAbilities = new MasterworkAbilities(this);
        masterworkAbilities.runHealthIncrease();
    }

    private void registerPlaceholderExpansion() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new UnitedSkillsPlaceholders(this).register();
        }
    }

    private void registerEvents(Listener[] listeners) {
        final PluginManager pluginManager = getServer().getPluginManager();
        for (Listener listener : listeners) {
            pluginManager.registerEvents(listener, this);
        }
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
