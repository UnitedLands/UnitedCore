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
    private HashMap<UUID, Long> cooldowns = null;
    private HashMap<UUID, Long> activeDurations = null;

    public Skill(Player player, SkillType type, HashMap<UUID, Long> cooldowns, HashMap<UUID, Long> activeDurations) {
        this(player, type);
        this.cooldowns = cooldowns;
        this.activeDurations = activeDurations;
    }

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

    /**
     * Attempts to activate a skill with a cooldown and duration
     * @return true if the skill is activated successfully, false if its already active or is on a cooldown.
     */
    public boolean activate() {
        int cooldownTime = getCooldown();
        int durationTime = getDuration();
        if (isActive()) {
            player.sendActionBar(Component
                    .text(getFormattedName() + " is active for" + getSecondsLeft() + "s", NamedTextColor.RED));
            player.playSound(player, Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 1f, 1f);
            return false;
        }
        if (isOnCooldown()) {
            notifyOnCooldown();
            return false;
        }
        notifyActivation();
        addTime(durationTime, activeDurations);
        addTime(cooldownTime, cooldowns);
        Bukkit.getScheduler().runTaskLater(getUnitedSkills(), this::notifyEnded, durationTime);
        return true;
    }

    private void notifyEnded() {
        player.sendActionBar(Component
                .text(getFormattedName() + " deactivated!", NamedTextColor.RED)
                .decorate(TextDecoration.BOLD));
        player.playSound(player, Sound.BLOCK_ENDER_CHEST_CLOSE, 2f, 0.7f);
    }
    public int getCooldown() {
        return getConfig().getInt("cooldowns." + "." + getName() + "." + getLevel());
    }

    public int getDuration() {
        return getConfig().getInt("durations." + "." + getName() + "." + getLevel());
    }

    public void notifyActivation() {
        player.playSound(player, Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f);
        player.sendActionBar(Component.text(getFormattedName() + " activated!", NamedTextColor.GOLD)
                .decoration(TextDecoration.BOLD, true));
    }

    private void notifyOnCooldown() {
        player.sendActionBar(Component
                .text(getFormattedName() + " can be re-activated in " +
                        + getRemainingCooldownTime() + "s", NamedTextColor.RED));
        player.playSound(player, Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 1f, 1f);
    }

    private void addTime(int time, HashMap<UUID, Long> map) {
        @NotNull UUID uuid = player.getUniqueId();
        map.put(uuid, System.currentTimeMillis() + (time * 1000L));
    }

    private long getRemainingCooldownTime() {
        UUID uuid = player.getUniqueId();
        return (cooldowns.get(uuid) - System.currentTimeMillis()) / 1000;
    }

    public long getSecondsLeft() {
        UUID uuid = player.getUniqueId();
        return (activeDurations.get(uuid) - System.currentTimeMillis()) / 1000;
    }

    public boolean isOnCooldown() {
        UUID uuid = player.getUniqueId();
        return cooldowns.containsKey(uuid) && cooldowns.get(uuid) > System.currentTimeMillis();
    }

    public boolean isActive() {
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
