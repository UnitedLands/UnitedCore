package org.unitedlands.war.listeners;

import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.economy.BankAccount;
import io.github.townyadvanced.eventwar.events.EventWarDeclarationEvent;
import io.github.townyadvanced.eventwar.events.EventWarEndEvent;
import io.github.townyadvanced.eventwar.events.EventWarStartEvent;
import io.github.townyadvanced.eventwar.events.TownScoredEvent;
import io.github.townyadvanced.eventwar.instance.War;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import org.unitedlands.war.UnitedWars;
import org.unitedlands.war.WarBossBar;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static org.unitedlands.war.Utils.*;

public class WarListener implements Listener {
    private final UnitedWars unitedWars;
    private final HashMap<Town, WarBossBar> activeBossbars = new HashMap<>();
    private final HashMap<UUID, Integer> townScores = new HashMap<>();
    private final @NotNull FileConfiguration config;

    public WarListener(UnitedWars unitedWars) {
        this.unitedWars = unitedWars;
        config = unitedWars.getConfig();
    }


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (hasActiveWarBossbar(player)) {
            WarBossBar warBossBar = getActiveWarBossbar(player);
            warBossBar.addViewer(player);
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (hasActiveWarBossbar(player)) {
            WarBossBar warBossBar = getActiveWarBossbar(player);
            warBossBar.removeViewer(player);
        }
    }

    @EventHandler
    public void onWarStart(EventWarStartEvent event) {
        War war = event.getWar();
        WarBossBar warBossBar = new WarBossBar(war);
        for (Town town: event.getWarringTowns()) {
            activeBossbars.put(town, warBossBar);
            townScores.put(town.getUUID(), 0);
        }
        warBossBar.startCountdown();

        for (Player player : war.getWarParticipants().getOnlineWarriors()) {
            if (isBannedWorld(player.getWorld().getName()))
                teleportPlayerToSpawn(player);

            for (String command : config.getStringList("commands-on-war-start"))
                player.performCommand(command);
        }

    }

    @EventHandler
    public void onScore(TownScoredEvent event) {
        UUID townUUID = event.getTown().getUUID();
        if (townScores.containsKey(townUUID)) {
            int previousScore = townScores.get(townUUID);
            townScores.replace(townUUID, previousScore + event.getScore());
        }
    }

    @EventHandler
    public void onTownWarEnd(EventWarEndEvent event) {
        War war = event.getWar();
        if (!war.getWarType().isTownWar()) return;

        Town winner = event.getWinningTown();
        Town loser = event.getWarringTowns().get(1);
        // Winner may have been the initial target, if so then get the other.
        if (winner.equals(loser)) {
            loser = event.getWarringTowns().get(0);
        }
        giveWarEarnings(winner.getAccount(), loser.getAccount());
        giveBonusClaims(winner);

        untrackScores(winner);
        untrackScores(loser);
    }

    @EventHandler
    public void onNationWarEnd(EventWarEndEvent event) {
        War war = event.getWar();
        if (!war.getWarType().isNationWar()) return;

        Nation winner = event.getWinningTown().getNationOrNull();
        Nation loser = war.getWarParticipants().getNations().get(1);
        // Winner may have been the initial target, if so then get the other.
        if (winner.equals(loser)) {
            loser = war.getWarParticipants().getNations().get(0);
        }

        BankAccount winnerAccount = winner.getAccount();
        for (Town losingTown: loser.getTowns()) {
            giveWarEarnings(winnerAccount, losingTown.getAccount());
            untrackScores(losingTown);
        }

        giveBonusClaims(winner.getCapital());
        untrackScores(winner.getCapital());
    }

    private void untrackScores(Town town) {
        townScores.remove(town.getUUID());
    }

    private void giveWarEarnings(BankAccount winner, BankAccount loser) {
        double amount = loser.getHoldingBalance() * 0.5;
        loser.withdraw(amount, "Lost a war");
        winner.deposit(amount, "Won a war");
    }

    private void giveBonusClaims(Town winner) {
        int totalScore = townScores.get(winner.getUUID());
        double bonusClaims = Math.round((double) totalScore / 10);
        winner.addBonusBlocks((int) bonusClaims);
    }

    @EventHandler
    public void onTownWarDeclaration(EventWarDeclarationEvent event) {
        // Only handle town wars here, nation wars in another listener to keep things clean.
        if (!event.getDeclarationOfWar().getType().isTownWar()) return;

        Town declaringTown = event.getDeclaringTown();
        // The target town is found at index 1, as per EventWar's codebase.
        Town targetTown = event.getWarringTowns().get(1);

        notifyDeclaration(targetTown, declaringTown);
    }

    @EventHandler
    public void onNationWarDeclaration(EventWarDeclarationEvent event) {
        if (!event.getDeclarationOfWar().getType().isNationWar()) return;

        Town declaringTown = event.getDeclaringTown();
        // The target nation is found at index 1, as per EventWar's codebase.
        Nation targetNation = event.getWarringNations().get(1);

        notifyDeclaration(targetNation, declaringTown.getNationOrNull());
    }

    private void notifyDeclaration(Nation targetNation, Nation declaringNation) {
        List<Resident> targetResidents = targetNation.getResidents();
        Title declarationTitle =  getTitle("<dark_red><bold>War Declaration!", "<yellow>" + declaringNation.getFormattedName() + "<red> has declared war on your nation");
        notifyResidents(targetResidents, declarationTitle);

        List<Resident> warringResidents = targetNation.getResidents();
        Title warringTitle =  getTitle("<red><bold>War Declaration!", "<red>Your nation has declared war on <yellow>" + targetNation.getFormattedName());
        notifyResidents(warringResidents, warringTitle);
    }

    private void notifyDeclaration(Town town, Town declaringTown) {
        List<Resident> targetResidents = town.getResidents();
        Title declarationTitle =  getTitle("<red><bold>War Declaration!", "<yellow>" + declaringTown.getFormattedName() + "<red>has declared war on you");
        notifyResidents(targetResidents, declarationTitle);

        List<Resident> warringResidents = declaringTown.getResidents();
        Title warringTitle = getTitle("<red><bold>War Declaration!", "<red>Your town has declared war on <yellow>" + town.getFormattedName());
        notifyResidents(warringResidents, warringTitle);

    }

    private void notifyResidents(List<Resident> residents, Title title) {
        for (Resident resident: residents) {
            Player player = resident.getPlayer();
            if (player == null) continue;
            player.showTitle(title);
            player.playSound(player, Sound.EVENT_RAID_HORN, 75, 1);
        }
    }

    private Title getTitle(String main, String sub) {
        Component mainTitle = UnitedWars.MINI_MESSAGE.deserialize(main);
        Component subTitle = UnitedWars.MINI_MESSAGE.deserialize(sub);
        return Title.title(mainTitle, subTitle);
    }

    private boolean hasActiveWarBossbar(Player player) {
        Town town = getPlayerTown(player);
        if (town == null) return false;
        if (activeBossbars.containsKey(town)) {
            WarBossBar warBossBar = activeBossbars.get(town);
            if (warBossBar.getRemainingSeconds() > 0) {
                return true;
            } else {
                activeBossbars.remove(town);
                return false;
            }
        }
        return false;
    }

    private WarBossBar getActiveWarBossbar(Player player) {
        Town town = getPlayerTown(player);
        return activeBossbars.get(town);
    }

}
