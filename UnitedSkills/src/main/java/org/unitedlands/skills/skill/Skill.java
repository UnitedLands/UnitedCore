package org.unitedlands.skills.skill;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.unitedlands.skills.UnitedSkills;

public class Skill {
    private final Player player;
    private final SkillType type;

    public Skill(Player player, SkillType type) {
        this.player = player;
        this.type = type;
    }

    public Player getPlayer() {
        return player;
    }

    public String getName() {
        return type.getName();
    }

    public String getFormattedName() {
        return WordUtils.capitalize(getName().replace("-", " "));
    }

    public boolean isMaxLevel() {
        return type.getMaxLevel() == getLevel();
    }
    public SkillType getType() {
        return type;
    }

    public boolean isSuccessful() {
        int level = getLevel();
        if (level == 0) {
            return false;
        }
        FileConfiguration configuration = getUnitedSkills().getConfig();
        int baseChance = configuration.getInt("base-chances." + getName() + "." + level);
        double randomPercentage = Math.random() * 100;
        return randomPercentage < baseChance;
    }

    public void notifyActivation() {
        player.playSound(player, Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f);
        player.sendActionBar(Component.text(getFormattedName() + " activated!", NamedTextColor.GOLD)
                .decoration(TextDecoration.BOLD, true));
    }

    public int getLevel() {
        String name = getName();
        if (player.hasPermission("united.skills." + name + ".3") && type.getMaxLevel() == 3) {
            return 3;
        }
        if (player.hasPermission("united.skills." + name + ".2")) {
            return 2;
        }
        if (player.hasPermission("united.skills." + name + ".1")) {
            return 1;
        }
        return 0;
    }

    static UnitedSkills getUnitedSkills() {
        return (UnitedSkills) Bukkit.getPluginManager().getPlugin("UnitedSkills");
    }

    static FileConfiguration getConfig() {
        return getUnitedSkills().getConfig();
    }
}
