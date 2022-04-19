package org.unitedlands.skills;

import com.gamingmesh.jobs.Jobs;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Utils {
    private static final MiniMessage miniMessage = MiniMessage.miniMessage();

    @NotNull
    public static Component getMessage(String message) {
        String configMessage = getUnitedSkills().getConfig().getString("messages." + message);
        if (configMessage == null) {
            return miniMessage.deserialize("<red>Message " + message + "could not be found in the config file!");
        }
        return miniMessage.deserialize(configMessage);
    }

    private static UnitedSkills getUnitedSkills() {
        return (UnitedSkills) Bukkit.getPluginManager().getPlugin("UnitedSkills");
    }

    public static Jobs getJobs() {
        return (Jobs) Bukkit.getPluginManager().getPlugin("Jobs");
    }

}
