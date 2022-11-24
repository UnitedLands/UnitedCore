package org.unitedlands.wars;

import com.palmergames.bukkit.towny.object.Resident;
import io.github.townyadvanced.eventwar.instance.War;
import io.github.townyadvanced.eventwar.settings.EventWarSettings;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

public class WarBossBar {
    private static final UnitedWars UNITED_WARS = Utils.getPlugin();
    private final long startTime;
    private HashSet<UUID> viewers;
    private final BossBar bossBar;

    private final War war;

    public WarBossBar(War war) {
        this.war = war;
        startTime = System.currentTimeMillis() + getDelay() * 1000L;
        bossBar = getBossBar();
        viewers = getViewers();
    }

    public void startCountdown() {
        int delay = getDelay();
        double decrease = (double) 1 / delay;
        if (viewers == null) return;

        UNITED_WARS.getServer().getScheduler().runTaskTimer(UNITED_WARS, task -> {
            for (UUID viewerUUID: viewers) {
                Player viewer = Bukkit.getPlayer(viewerUUID);
                if (viewer == null) continue;

                if (getRemainingSeconds() <= 5) {
                    if (getRemainingSeconds() <= 0) {
                        viewer.playSound(viewer, Sound.EVENT_RAID_HORN, 75, 1);
                        viewer.showTitle(getTimeTitle(0));
                        viewer.hideBossBar(bossBar);
                        task.cancel();
                    } else {
                        viewer.playSound(viewer, Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 1F, 0.6F);
                        viewer.showTitle(getTimeTitle(getRemainingSeconds()));
                    }

                }
            }
            Component name = Component.text( "WAR STARTS IN: " , NamedTextColor.DARK_RED, TextDecoration.BOLD);
            bossBar.name(name.append(Component.text(getRemainingSeconds() + "s", NamedTextColor.RED).decoration(TextDecoration.BOLD, false)));
            bossBar.progress((float) Math.max(0.0F, bossBar.progress() - decrease));

        }, 0, 20L);
    }

    private @NotNull Title getTimeTitle(int remainingSeconds) {
        Component main = Component.text(remainingSeconds).color(NamedTextColor.RED).decoration(TextDecoration.BOLD, true);
        if (remainingSeconds == 0) {
            main = Component.text("WAR START!").color(NamedTextColor.DARK_RED).decoration(TextDecoration.BOLD, true);
        }
        return Title.title(main, Component.text(""));
    }

    public int getRemainingSeconds() {
        long currentTimeStamp = System.currentTimeMillis();
        return (int) (Math.max(0, Math.floor(startTime - currentTimeStamp) / 1000));
    }

    private int getDelay() {
        return switch (war.getWarType().name()) {
            case "TOWNWAR" -> EventWarSettings.townWarDelay();
            case "NATIONWAR" -> EventWarSettings.nationWarDelay();
            case "CIVILWAR" -> EventWarSettings.civilWarDelay();
            case "WORLDWAR" -> EventWarSettings.worldWarDelay();
            case "RIOT" -> EventWarSettings.riotDelay();
            default -> 30;
        };
    }

    private HashSet<UUID> getViewers() {
        List<Resident> residents = war.getWarParticipants().getResidents();
        viewers = new HashSet<>();
        for (Resident resident: residents) {
            Player player = resident.getPlayer();
            addViewer(player);
        }
        return viewers;
    }

    @NotNull
    public BossBar getBossBar() {
        Component time = Component.text(getRemainingSeconds() + "s", NamedTextColor.RED).decoration(TextDecoration.BOLD, false);
        final Component name = Component.text( "WAR STARTS IN: " , NamedTextColor.DARK_RED, TextDecoration.BOLD);
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
