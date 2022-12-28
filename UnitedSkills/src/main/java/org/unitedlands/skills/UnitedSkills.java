package org.unitedlands.skills;

import de.Linus122.SafariNet.API.SafariNet;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.unitedlands.skills.commands.BlendCommand;
import org.unitedlands.skills.commands.PointsCommand;
import org.unitedlands.skills.commands.UnitedSkillsComand;
import org.unitedlands.skills.guis.BiomeKit;
import org.unitedlands.skills.hooks.UnitedSkillsPlaceholders;
import org.unitedlands.skills.abilities.BrewerAbilities;
import org.unitedlands.skills.abilities.DiggerAbilities;
import org.unitedlands.skills.abilities.FarmerAbilities;
import org.unitedlands.skills.abilities.FishermanAbilities;
import org.unitedlands.skills.abilities.HunterAbilities;
import org.unitedlands.skills.abilities.MinerAbilities;
import org.unitedlands.skills.abilities.WoodcutterAbilities;
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
                new DiggerAbilities(this),
                new WoodcutterAbilities(this),
                new FishermanAbilities(this),
                new MinerAbilities(this),
                new BiomeKit(this),
        };

        registerEvents(listeners);

        SafariNet.addListener(new SafariNetListener(this));

        final HunterAbilities hunterAbilities = new HunterAbilities(this);
        hunterAbilities.damageBleedingEntities();
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
}
