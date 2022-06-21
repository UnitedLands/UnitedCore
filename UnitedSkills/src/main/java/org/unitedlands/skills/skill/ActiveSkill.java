package org.unitedlands.skills.skill;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.UUID;

public class ActiveSkill extends Skill {
    Player player = getPlayer();
    private HashMap<UUID, Long> cooldowns = null;
    private HashMap<UUID, Long> activeDurations = null;

    public ActiveSkill(Player player, SkillType type) {
        super(player, type);
    }

    public ActiveSkill(Player player, SkillType type, HashMap<UUID, Long> cooldowns, HashMap<UUID, Long> activeDurations) {
        this(player, type);
        this.cooldowns = cooldowns;
        this.activeDurations = activeDurations;
    }

    /**
     * Attempts to activate a skill with a cooldown and duration
     */
    public void activate() {
        int cooldownTime = getCooldown();
        int durationTime = getDuration();
        if (isActive()) {
            player.sendActionBar(Component
                    .text(getFormattedName() + " is active for " + getSecondsLeft() + "s", NamedTextColor.RED));
            player.playSound(player, Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 1f, 1f);
            return;
        }
        if (isOnCooldown()) {
            notifyOnCooldown();
            return;
        }
        notifyActivation();
        addTime(durationTime, activeDurations);
        addTime(cooldownTime, cooldowns);
        sendBossBar();
    }

    private void sendBossBar() {
        final Component name = Component.text(getFormattedName() + ": ", NamedTextColor.YELLOW);
        Component time = Component.text(getSecondsLeft() + "s", NamedTextColor.GOLD);
        BossBar bossBar = BossBar.bossBar(name.append(time), 1, BossBar.Color.GREEN, BossBar.Overlay.PROGRESS);
        player.showBossBar(bossBar);
        Bukkit.getScheduler().runTaskTimer(getUnitedSkills(), task -> {
            if (bossBar.progress() <= 0.0) {
                task.cancel();
                player.hideBossBar(bossBar);
                return;
            }
            bossBar.name(name.append(Component.text(getSecondsLeft() + "s", NamedTextColor.GOLD)));
            bossBar.progress((float) getSecondsLeft() / getDuration());
        }, 0, 20L);
    }

    public int getCooldown() {
        return getConfig().getInt("skills." + getName() + "." + getLevel() + "." + "cooldown");
    }

    public int getDuration() {
        return getConfig().getInt("skills." + getName() + "." + getLevel() + "." + "duration");
    }

    private void notifyOnCooldown() {
        player.sendActionBar(Component
                .text(getFormattedName() + " can be re-activated in " +
                        +getRemainingCooldownTime() + "s", NamedTextColor.RED));
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

}
