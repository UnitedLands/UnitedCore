package org.unitedlands.pvp.listeners;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.event.NewDayEvent;
import com.palmergames.bukkit.towny.event.PlayerEnterTownEvent;
import com.palmergames.bukkit.towny.event.PlayerLeaveTownEvent;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.unitedlands.pvp.UnitedPvP;
import org.unitedlands.pvp.player.PvpPlayer;

import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class TownyListener implements Listener {
    private final TownyAPI towny = TownyAPI.getInstance();
    private final UnitedPvP unitedPvP;
    public TownyListener(UnitedPvP unitedPvP) {
        this.unitedPvP = unitedPvP;
    }


    @EventHandler
    public void onNewTownyDay(NewDayEvent event) {
        updatePlayerHostilities();
    }

    @EventHandler
    public void onTownEnter(PlayerEnterTownEvent event) {
        Player player = event.getPlayer();
        Resident outlaw = towny.getResident(player);
        Town town = event.getEnteredtown();

        if (town.hasOutlaw(outlaw)) {
            player.showTitle(getOutlawWarningTitle(town));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 0.4F);
            player.showBossBar(getOutlawedBossbar(town));
        }
    }

    @EventHandler
    public void onTownLeave(PlayerLeaveTownEvent event) {
        Player player = event.getPlayer();
        Resident outlaw = towny.getResident(player);
        Town town = event.getLefttown();

        if (town.hasOutlaw(outlaw)) {
            player.hideBossBar(getOutlawedBossbar(town));
        }
    }

    private void updatePlayerHostilities() {
        List<PvpPlayer> pvpPlayers = getAllPvPPlayers();
        for (PvpPlayer pvpPlayer : pvpPlayers) {
            pvpPlayer.updatePlayerHostility();
        }
    }

    private List<PvpPlayer> getAllPvPPlayers() {
        File dataFolder = new File(unitedPvP.getDataFolder(), File.separator + "players" + File.separator);
        List<PvpPlayer> pvpPlayers = new ArrayList<>();
        for (File file : dataFolder.listFiles()) {
            PvpPlayer pvpPlayer = new PvpPlayer(file);
            pvpPlayers.add(pvpPlayer);
        }
        return pvpPlayers;
    }


    private Title getOutlawWarningTitle(Town town) {
        Title.Times times = Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(3000), Duration.ofMillis(1000));

        final Component mainTitle = Component.text("Warning!", NamedTextColor.DARK_RED, TextDecoration.BOLD);
        final Component subtitle = Component
                .text("You are outlawed in ", NamedTextColor.RED)
                .append(Component.text(town.getName(), NamedTextColor.YELLOW))
                .append(Component.text("!", NamedTextColor.RED));
        return Title.title(mainTitle, subtitle, times);
    }

    private BossBar getOutlawedBossbar(Town town) {
        final Component header = Component.text("DANGER ZONE: ", NamedTextColor.DARK_RED, TextDecoration.BOLD);
        final Component townName = Component.text("You're an outlaw in " + town.getName(), NamedTextColor.RED)
                .decoration(TextDecoration.BOLD, false);

        return BossBar.bossBar(header.append(townName), 1, BossBar.Color.RED, BossBar.Overlay.NOTCHED_12);
    }
}
