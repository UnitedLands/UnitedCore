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
import org.unitedlands.skills.jobs.MinerAbilities;
import org.unitedlands.skills.jobs.WoodcutterAbilities;
import org.unitedlands.skills.points.JobsListener;
import org.unitedlands.skills.safarinets.TraffickerListener;
import org.unitedlands.skills.safarinets.WranglerListener;
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
        final JobsListener jobsListener = new JobsListener(this);
        final BrewerAbilities brewerAbilities = new BrewerAbilities(this);
        final FarmerAbilities farmerAbilities = new FarmerAbilities(this);
        final HunterAbilities hunterAbilities = new HunterAbilities(this);
        final DiggerAbilities diggerAbilities = new DiggerAbilities(this, getCoreProtect());
        final WoodcutterAbilities woodcutterAbilities = new WoodcutterAbilities(this, getCoreProtect());
        final FishermanAbilities fishermanAbilities = new FishermanAbilities(this);
        final MinerAbilities minerAbilities = new MinerAbilities(this, getCoreProtect());
        final BiomeKit biomeKitListener = new BiomeKit(this);

        registerEvents(jobsListener, brewerAbilities, farmerAbilities, hunterAbilities,
                diggerAbilities, woodcutterAbilities, fishermanAbilities, minerAbilities, biomeKitListener);

        SafariNet.addListener(new WranglerListener(this));
        SafariNet.addListener(new TraffickerListener(this));

        hunterAbilities.damageBleedingEntities();
    }

    private void registerPlaceholderExpansion() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new UnitedSkillsPlaceholders(this).register();
        }
    }

    private void registerEvents(Listener... listeners) {
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
