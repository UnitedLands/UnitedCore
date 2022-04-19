package org.unitedlands.skills;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.UUID;

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

    public void notifyActivation() {
        player.playSound(player, Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f);
        player.sendActionBar(Component.text(getFormattedName() + " Skill Activated!", NamedTextColor.GREEN)
                .decoration(TextDecoration.BOLD, true));
    }

    public void activate(HashMap<UUID, Long> cooldowns, HashMap<UUID, Long> activeDurations) {
        final UUID uuid = player.getUniqueId();
        int cooldownTime = getConfig().getInt("cooldowns." + "." + getName() + "." + getLevel());
        int durationTime = getConfig().getInt("durations." + "." + getName() + "." + getLevel());
        if (isActive(activeDurations)) {
            long secondsLeft = (activeDurations.get(uuid) - System.currentTimeMillis()) / 1000;
            player.sendActionBar(Component
                    .text("Skill is active for " + secondsLeft, NamedTextColor.RED));
            player.playSound(player, Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 1f, 1f);
            return;
        }
        if (isOnCooldown(cooldowns)) {
            long secondsLeft = (cooldowns.get(uuid) - System.currentTimeMillis()) / 1000;
            player.sendActionBar(Component
                    .text("You can activate this skill in "
                            + secondsLeft + "s", NamedTextColor.RED));
            player.playSound(player, Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 1f, 1f);
            return;
        }
        notifyActivation();
        addTime(durationTime, activeDurations);
        addTime(cooldownTime, cooldowns);
    }

    private void addTime(int cooldownTime, HashMap<UUID, Long> map) {
        @NotNull UUID uuid = player.getUniqueId();
        map.put(uuid, System.currentTimeMillis() + (cooldownTime * 1000L));
    }

    public boolean isOnCooldown(HashMap<UUID, Long> cooldowns) {
        UUID uuid = player.getUniqueId();
        return cooldowns.containsKey(uuid) && cooldowns.get(uuid) > System.currentTimeMillis();
    }

    public boolean isActive(HashMap<UUID, Long> activeDurations) {
        UUID uuid = player.getUniqueId();
        return activeDurations.containsKey(uuid) && activeDurations.get(uuid) > System.currentTimeMillis();
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

    public int getLevel() {
        String name = getName();
        if (player.hasPermission("united.skills." + name + ".2") && type.getMaxLevel() == 3) {
            return 3;
        }
        if (player.hasPermission("united.skills." + name + ".1")) {
            return 2;
        }
        if (player.hasPermission("united.skills." + name)) {
            return 1;
        }
        return 0;
    }

    private static UnitedSkills getUnitedSkills() {
        return (UnitedSkills) Bukkit.getPluginManager().getPlugin("UnitedSkills");
    }

    private static FileConfiguration getConfig() {
        return  getUnitedSkills().getConfig();
    }
}
