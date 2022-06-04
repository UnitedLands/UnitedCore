package org.unitedlands.skills.hooks;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.unitedlands.skills.UnitedSkills;
import org.unitedlands.skills.points.PlayerConfiguration;

public class UnitedSkillsPlaceholders extends PlaceholderExpansion {
    private final UnitedSkills unitedSkills;

    public UnitedSkillsPlaceholders(UnitedSkills unitedSkills) {
        this.unitedSkills = unitedSkills;
    }
    @Override
    public @NotNull String getIdentifier() {
        return "unitedskills";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Maroon28";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true;
    }
    @Override
    public String onRequest(OfflinePlayer player, String params) {
        if (player.getPlayer() != null) {
            String[] args = params.split("_");
            if (args[0].equals("points")) {
                return String.valueOf(getPoints(player, args[1]));
            }
        }
        return "Usage: %unitedskills_points_<job-name>%";
    }

    private int getPoints(OfflinePlayer player, String jobName) {
        PlayerConfiguration playerConfiguration = new PlayerConfiguration(unitedSkills, player);
        return playerConfiguration.getFileConfiguration().getInt(jobName.toLowerCase() + "-points");
    }
}
