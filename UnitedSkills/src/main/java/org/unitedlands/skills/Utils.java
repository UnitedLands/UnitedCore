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

    public static boolean isSuccessful(Player player, String skillName) {
        int chanceModifier = getSkillLevel(player, skillName);
        if (chanceModifier == 0) {
            return false;
        }
        FileConfiguration configuration = getUnitedSkills().getConfig();
        int baseChance = configuration.getInt("base-chances." + skillName);
        double randomPercentage = Math.random() * 100;
        return randomPercentage < baseChance * chanceModifier;
    }

    public static int getSkillLevel(Player player, String skillName) {
        if (player.hasPermission("united.skills." + skillName + ".2")) {
            return 3;
        }
        if (player.hasPermission("united.skills." + skillName + ".1")) {
            return 2;
        }
        if (player.hasPermission("united.skills." + skillName)) {
            return 1;
        }
        return 0;
    }
}
