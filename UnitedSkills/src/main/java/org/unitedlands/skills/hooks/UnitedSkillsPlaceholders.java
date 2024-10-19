package org.unitedlands.skills.hooks;

import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.unitedlands.skills.UnitedSkills;
import org.unitedlands.skills.points.PlayerConfiguration;
import org.unitedlands.skills.skill.Skill;
import org.unitedlands.skills.skill.SkillFile;
import org.unitedlands.skills.skill.SkillType;

public class UnitedSkillsPlaceholders extends PlaceholderExpansion {
    private final UnitedSkills unitedSkills;
    private OfflinePlayer player;

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
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player.getPlayer() != null) {
            this.player = player;
            params = PlaceholderAPI.setBracketPlaceholders(player, params);
            params = setBracketPlaceholders(params);
            String[] args = params.split("_");
            switch (args[0]) {
                case "points" -> {
                    return String.valueOf(getPoints(args[1]));
                }
                case "has-skill" -> {
                    Skill skill = getSkillFromArg(args[1]);
                    if (skill.getLevel() != 0) {
                        return "true";
                    } else {
                        return "false";
                    }
                }
                case "has-level" -> {
                    Skill skill = getSkillFromArg(args[1]);
                    int targetLevel = Integer.parseInt(args[2]);
                    int actualLevel = skill.getLevel();
                    return String.valueOf(targetLevel <= actualLevel);
                }
                case "gui-level-color" -> {
                    Skill skill = getSkillFromArg(args[1]);
                    int targetLevel = Integer.parseInt(args[2]);
                    int actualLevel = skill.getLevel();
                    if (targetLevel <= actualLevel) {
                        // Dark Green
                        return "#1E8909";
                    } else {
                        // Dark Red
                        return "#AD240C";
                    }
                }
                case "is-active-skill" -> {
                    FileConfiguration skillConfig = getSkillsConfig();
                    // if the cooldown is not 0, that means it exists and it's an active skill
                    return String.valueOf(skillConfig.getInt("skills." + args[1] + ".1." + "cooldown") != 0);
                }
                // %unitedskills_value_<skill>_<level>_<data-point>%
                // data-point can be cooldown, duration, price, or points
                case "value" -> {
                    FileConfiguration skillConfig = getSkillsConfig();
                    return String.valueOf(skillConfig.getInt("skills." + args[1] + "." + args[2] + "." + args[3]));
                }
            }
        }
        return null;
    }

    private String setBracketPlaceholders(String params) {
        String extractedPlaceholder = params.replace("[a-zA-Z]+\\[.*_[a-zA-Z]+", "$0");
        String parsedPlaceholder = PlaceholderAPI.setPlaceholders(player, extractedPlaceholder.replace("[", "%").replace("]", "%"));
        return params.replace(extractedPlaceholder, parsedPlaceholder);
    }

    private FileConfiguration getSkillsConfig() {
        SkillFile skillFile = new SkillFile(unitedSkills);
        return skillFile.getSkillsConfig();
    }

    private Skill getSkillFromArg(String arg) {
        String skillType = arg.toUpperCase().replace("-", "_");
        return new Skill(player.getPlayer(), SkillType.valueOf(skillType));
    }

    private int getPoints(String jobName) {
        PlayerConfiguration playerConfiguration = new PlayerConfiguration(unitedSkills, player);
        return playerConfiguration.getFileConfiguration().getInt(jobName.toLowerCase() + "-points");
    }
}
