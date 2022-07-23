package org.unitedlands.pvp.listeners;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.event.NewDayEvent;
import com.palmergames.bukkit.towny.event.NewTownEvent;
import com.palmergames.bukkit.towny.event.PlayerEnterTownEvent;
import com.palmergames.bukkit.towny.event.PlayerLeaveTownEvent;
import com.palmergames.bukkit.towny.event.town.TownReclaimedEvent;
import com.palmergames.bukkit.towny.event.town.TownRuinedEvent;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.WorldCoord;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.unitedlands.pvp.UnitedPvP;
import org.unitedlands.pvp.player.PvpPlayer;
import org.unitedlands.pvp.player.Status;
import org.unitedlands.pvp.util.OldTownBlock;
import org.unitedlands.pvp.util.Utils;

import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class TownyListener implements Listener {
    private final TownyAPI towny = TownyAPI.getInstance();
    private final UnitedPvP unitedPvP;
    private Player player;
    public TownyListener(UnitedPvP unitedPvP) {
        this.unitedPvP = unitedPvP;
    }


    @EventHandler
    public void onTownRuinEvent(TownRuinedEvent event) {
        Town town = event.getTown();
        if (!town.hasHomeBlock()) return;
        WorldCoord coord = town.getHomeBlockOrNull().getWorldCoord();
        int expiryTime = calculateExpiryTime(town);
        // The town's too small to have an expiry time, so just disregard it.
        if (expiryTime == 0) {
            return;
        }
        unitedPvP.getTownBlocksList().add(new OldTownBlock(coord, expiryTime + 2, calculateSearchRadius(town), town.getName()));
    }

    @EventHandler
    public void onTownReclaim(TownReclaimedEvent event) {
        Town town = event.getTown();
        WorldCoord coord = town.getHomeBlockOrNull().getWorldCoord();
        OldTownBlock oldTownBlock = new OldTownBlock(coord);
        unitedPvP.getTownBlocksList().remove(oldTownBlock);
    }

    @EventHandler
    public void onNewTownCreation(NewTownEvent event) {
        Town town = event.getTown();
        WorldCoord newTownBlockCoord = town.getHomeBlockOrNull().getWorldCoord();
        OldTownBlock oldTownBlock = Utils.getNearbyOldTownBlock(newTownBlockCoord, 150);
        // If there's an old town homeblock 150 blocks near this new one, remove the old one.
        if (oldTownBlock != null) {
            unitedPvP.getTownBlocksList().remove(oldTownBlock);
        }
    }

    @EventHandler
    public void onRuinedTownInteract(PlayerInteractEvent event) {
        player = event.getPlayer();
        if (!isInRuinedTown())
            return;

        PvpPlayer pvpPlayer = new PvpPlayer(player);
        if (pvpPlayer.isPassive() || pvpPlayer.isVulnerable()) {
            player.sendMessage(Utils.getMessage("cannot-interact-while-passive"));
            event.setCancelled(true);
        }

    }

    @EventHandler
    public void onNewTownyDay(NewDayEvent event) {
        updatePlayerHostilities();
        reduceExpiryTime();
    }

    @EventHandler
    public void onTownEnter(PlayerEnterTownEvent event) {
        Player player = event.getPlayer();
        Resident outlaw = towny.getResident(player);
        Town town = event.getEnteredtown();

        if (town.hasOutlaw(outlaw)) {
            PvpPlayer pvpPlayer = new PvpPlayer(player);
            if (pvpPlayer.isPassive()) {
                pvpPlayer.setStatus(Status.VULNERABLE);
                pvpPlayer.setHostility(1);
            }
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
            PvpPlayer pvpPlayer = new PvpPlayer(player);
            if (pvpPlayer.isVulnerable()) {
                pvpPlayer.setStatus(Status.PASSIVE);
                pvpPlayer.setHostility(0);
                player.hideBossBar(getOutlawedBossbar(town));
                showSafetyBossBar();
            }
        }
    }

    // oh lawd
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        player = event.getPlayer();
        // disregard any movements that don't actually change the block.
        if (!event.hasChangedBlock()) {
            return;
        }
        Location locFrom = event.getFrom();
        Location locTo = event.getTo();
        // We (usually) don't really care if the chunk hasn't changed either.
        if (locFrom.getChunk().equals(locTo.getChunk())) {
            return;
        }
        // This means that there *may* be ruins close to the town, in which case the working town takes priority.
        if (!isInRuinedTown()) {
            return;
        }

        WorldCoord coord = WorldCoord.parseWorldCoord(locTo);
        String isInDangerZoneKey = "is-in-danger-zone";
        PvpPlayer pvpPlayer = new PvpPlayer(event.getPlayer());
        OldTownBlock oldTownBlock = Utils.getNearbyOldTownBlock(coord, 150);

        if (oldTownBlock != null && !player.hasMetadata(isInDangerZoneKey)) {
            showOldRuinsBossbar(oldTownBlock);
            player.setMetadata(isInDangerZoneKey, new FixedMetadataValue(unitedPvP, true));
            player.playSound(player, Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 1, 0.5F);
            // If a player is passive, they should become 'vulnerable'
            if (pvpPlayer.isPassive()) {
                pvpPlayer.setStatus(Status.VULNERABLE);
                pvpPlayer.setHostility(1);
            }
        }

        // The player is no longer close to the ruins, but still has data attached saying they are.
        // Remove the old bar, show a new one if the player is passive, and remove vulnerability.
        if (!Utils.isCloseToOldRuins(coord, 150) && player.hasMetadata(isInDangerZoneKey)) {
            if (pvpPlayer.isVulnerable()) {
                showSafetyBossBar();
                pvpPlayer.setStatus(Status.PASSIVE);
                pvpPlayer.setHostility(0);
                player.playSound(player, Sound.BLOCK_NOTE_BLOCK_PLING, 1 , 1);
            }
            player.removeMetadata(isInDangerZoneKey, unitedPvP);
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

    private void reduceExpiryTime() {
        ArrayList<OldTownBlock> reducedTownBlocks = new ArrayList<>();
        for (OldTownBlock oldTownBlock : unitedPvP.getTownBlocksList()) {
            if (oldTownBlock.getExpiryTime() == 1) {
                unitedPvP.getTownBlocksList().remove(oldTownBlock);
            }
            oldTownBlock.setExpiryTime(oldTownBlock.getExpiryTime() - 1);
            reducedTownBlocks.add(oldTownBlock);
        }
        unitedPvP.setOldTownBlocks(reducedTownBlocks);
    }

    private Title getOutlawWarningTitle(Town town) {
        Title.Times times = Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(3000), Duration.ofMillis(1000));

        final Component mainTitle = Component.text("You are vulnerable!", NamedTextColor.DARK_RED, TextDecoration.BOLD);
        final Component subtitle = Component
                .text("You are outlawed in ", NamedTextColor.RED)
                .append(Component.text(town.getName(), NamedTextColor.YELLOW))
                .append(Component.text("!", NamedTextColor.RED));
        return Title.title(mainTitle, subtitle, times);
    }

    private void showSafetyBossBar() {
        final Component title = Component.text("Passiveness Restored!", NamedTextColor.DARK_GREEN);
        BossBar bossBar = BossBar.bossBar(title, 1, BossBar.Color.GREEN, BossBar.Overlay.PROGRESS);
        player.showBossBar(bossBar);
        hideBossBarAfter(bossBar, 4);
    }

    private void showOldRuinsBossbar(OldTownBlock oldTownBlock) {
        final Component header = Component.text("DANGER ZONE: ", NamedTextColor.DARK_RED, TextDecoration.BOLD);
        final Component townName = Component.text("Old Ruins of " + oldTownBlock.getTownName(), NamedTextColor.RED)
                .decoration(TextDecoration.BOLD, false);

        BossBar bossBar = BossBar.bossBar(header.append(townName), 1, BossBar.Color.RED, BossBar.Overlay.NOTCHED_12);
        player.showBossBar(bossBar);
        hideBossBarAfter(bossBar, 15);
    }

    private BossBar getOutlawedBossbar(Town town) {
        final Component header = Component.text("DANGER ZONE: ", NamedTextColor.DARK_RED, TextDecoration.BOLD);
        final Component townName = Component.text("You're an outlaw in " + town.getName(), NamedTextColor.RED)
                .decoration(TextDecoration.BOLD, false);

        return BossBar.bossBar(header.append(townName), 1, BossBar.Color.RED, BossBar.Overlay.NOTCHED_12);
    }

    private void hideBossBarAfter(BossBar bossBar, int timeInSecs) {
        if (timeInSecs == 0) {
            player.hideBossBar(bossBar);
            return;
        }
        double timeDecrease =  (double) 1 / timeInSecs;
        unitedPvP.getServer().getScheduler().runTaskTimer(unitedPvP, task -> {
            if (bossBar.progress() <= 0.0) {
                task.cancel();
                player.hideBossBar(bossBar);
                return;
            }
            bossBar.progress((float) Math.max(0.0, bossBar.progress() - timeDecrease));
        }, 0, 20L);
    }

    private int calculateSearchRadius(Town town) {
        FileConfiguration configuration = unitedPvP.getConfig();
        int riseStep = configuration.getInt("town-blocks.distance-rise-step");
        int riseAt = configuration.getInt("town-blocks.distance-rise-at");
        int townBlocks = town.getTotalBlocks();
        return (townBlocks / riseAt) * riseStep;
    }

    private int calculateExpiryTime(Town town) {
        FileConfiguration configuration = unitedPvP.getConfig();
        int riseStep = configuration.getInt("town-blocks.expiry-rise-step");
        int riseAt = configuration.getInt("town-blocks.expiry-rise-at");
        int townBlocks = town.getTotalBlocks();
        if (townBlocks < configuration.getInt("minimum-size")) {
            return 0;
        }
        return (townBlocks / riseAt) * riseStep;
    }

    private boolean isInRuinedTown() {
        TownBlock townBlock = towny.getTownBlock(player.getLocation());
        if (townBlock == null)
            return false;

        Town town = townBlock.getTownOrNull();
        if (town == null)
            return false;

        return town.isRuined();
    }
}
