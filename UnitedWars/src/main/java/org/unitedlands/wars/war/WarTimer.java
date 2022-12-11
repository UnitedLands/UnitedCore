package org.unitedlands.wars.war;

import com.palmergames.bukkit.towny.object.Resident;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.unitedlands.wars.UnitedWars;

import java.time.Duration;
import java.util.HashSet;
import java.util.UUID;

public class WarTimer {
    private static final UnitedWars PLUGIN = UnitedWars.getInstance();
    private final long startTime;
    private final BossBar bossBar;
    private final War war;
    private HashSet<UUID> viewers;

    public WarTimer(War war) {
        this.war = war;
        startTime = System.currentTimeMillis() + getDelay() * 1000L;
        bossBar = getBossBar();
        viewers = getViewers();
    }

    public void startCountdown() {
        int delay = getDelay();
        float decrease = (float) 1 / delay;
        if (viewers == null) return;

        PLUGIN.getServer().getScheduler().runTaskTimer(PLUGIN, task -> {
            for (UUID viewerUUID : viewers) {
                Player viewer = Bukkit.getPlayer(viewerUUID);
                if (viewer == null) continue;

                if (getRemainingSeconds() <= 5) {
                    if (getRemainingSeconds() <= 0) {
                        viewer.playSound(viewer, Sound.EVENT_RAID_HORN, 75, 1);
                        viewer.showTitle(getTimeTitle(0));
                        viewer.hideBossBar(bossBar);
                        viewers.clear();
                        war.endWarTimer(); // Tell the war that the timer has ended.
                        task.cancel();
                        break;
                    } else {
                        viewer.playSound(viewer, Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 1F, 0.6F);
                        viewer.showTitle(getTimeTitle(getRemainingSeconds()));
                    }

                }
            }
            Component name = Component.text("WAR STARTS IN: ", NamedTextColor.DARK_RED, TextDecoration.BOLD);
            bossBar.name(name.append(Component.text(getFormattedTime(), NamedTextColor.RED).decoration(TextDecoration.BOLD, false)));
            bossBar.progress(Math.max(0.0F, bossBar.progress() - decrease));

        }, 0, 20L);
    }

    private @NotNull Title getTimeTitle(int remainingSeconds) {
        Component main = Component.text(remainingSeconds).color(NamedTextColor.RED).decoration(TextDecoration.BOLD, true);
        if (remainingSeconds == 0) {
            main = Component.text("WAR START!").color(NamedTextColor.DARK_RED).decoration(TextDecoration.BOLD, true);
        }
        return Title.title(main, Component.text(""));
    }

    private String getFormattedTime() {
        Duration duration = Duration.ofSeconds(getRemainingSeconds());
        return String.format("%02d:%02d", duration.toMinutes(), duration.toSecondsPart());
    }

    public int getRemainingSeconds() {
        long currentTimeStamp = System.currentTimeMillis();
        return (int) (Math.max(0, Math.floor(startTime - currentTimeStamp) / 1000));
    }

    private int getDelay() {
        String path = war.getWarType().name().toLowerCase() + ".delay";
        return PLUGIN.getConfig().getInt(path);
    }

    private HashSet<UUID> getViewers() {
        HashSet<Resident> residents = war.getWarParticipants();
        viewers = new HashSet<>();
        for (Resident resident : residents) {
            Player player = resident.getPlayer();
            addViewer(player);
        }
        return viewers;
    }

    @NotNull
    public BossBar getBossBar() {
        Component time = Component.text(getRemainingSeconds() + "s", NamedTextColor.RED).decoration(TextDecoration.BOLD, false);
        final Component name = Component.text("WAR STARTS IN: ", NamedTextColor.DARK_RED, TextDecoration.BOLD);
        return BossBar.bossBar(name.append(time), 1, BossBar.Color.RED, BossBar.Overlay.NOTCHED_6);
    }


    public void addViewer(Player player) {
        if (player != null) {
            viewers.add(player.getUniqueId());
            player.showBossBar(bossBar);
        }
    }

    public void removeViewer(Player player) {
        viewers.remove(player.getUniqueId());
        player.hideBossBar(bossBar);
    }
}
