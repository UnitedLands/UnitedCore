package org.unitedlands.wars.listeners;

import com.palmergames.bukkit.towny.event.NewDayEvent;
import com.palmergames.bukkit.towny.event.statusscreen.NationStatusScreenEvent;
import com.palmergames.bukkit.towny.event.statusscreen.TownStatusScreenEvent;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;
import org.unitedlands.wars.UnitedWars;
import org.unitedlands.wars.books.TokenCostCalculator;
import org.unitedlands.wars.books.data.Declarer;
import org.unitedlands.wars.books.data.WarTarget;
import org.unitedlands.wars.events.WarDeclareEvent;
import org.unitedlands.wars.events.WarHealthChangeEvent;
import org.unitedlands.wars.war.*;
import org.unitedlands.wars.war.entities.WarringEntity;
import org.unitedlands.wars.war.entities.WarringNation;
import org.unitedlands.wars.war.entities.WarringTown;

import java.util.List;

public class WarListener implements Listener {
    private final @NotNull FileConfiguration config;

    public WarListener(UnitedWars unitedWars) {
        config = unitedWars.getConfig();
    }


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        War war = WarDatabase.getWar(player);
        if (war == null)
            return;

        if (war.hasActiveTimer()) {
            war.getWarTimer().addViewer(player);
            return;
        }

        WarringEntity warringEntity = WarDatabase.getWarringEntity(player);
        if (warringEntity == null)
            return;

        warringEntity.getWarHealth().show(player);
    }

    @EventHandler
    public void onNewTownyDay(NewDayEvent event) {
        List<Town> towns = UnitedWars.TOWNY_API.getTowns();
        for (Town town : towns) {
            if (town.isBankrupt() || town.isRuined() || town.isNeutral())
                continue;
            TokenCostCalculator costCalculator = new TokenCostCalculator(town);
            int earnedTokens = costCalculator.calculateTokenIncome();
            int currentTokens = WarDataController.getWarTokens(town);
            WarDataController.setTokens(town, currentTokens + earnedTokens);
        }

        for (WarringTown warringTown : WarDatabase.getWarringTowns()) {
            warringTown.getWarHealth().decrementHealth(20);
            if (warringTown.getWarHealth().getValue() == 0) {
                WarDatabase.removeWarringTown(warringTown.getTown());
            }
        }
        for (WarringNation warringNation : WarDatabase.getWarringNations()) {
            warringNation.getWarHealth().decrementHealth(20);
            if (warringNation.getWarHealth().getValue() == 0) {
                WarDatabase.removeWarringNation(warringNation.getNation());
            }
        }
    }

    @EventHandler
    public void onTownStatus(TownStatusScreenEvent event) {
        Town town = event.getTown();
        if (!town.isNeutral()) {
            TokenCostCalculator costCalculator = new TokenCostCalculator(town);
            event.getStatusScreen().addComponentOf("warTokens", "§2War Tokens: §a" + WarDataController.getWarTokens(town));
            event.getStatusScreen().addComponentOf("dailyWarTokens", "§2Daily War Tokens: §a" + costCalculator.calculateTokenIncome());
        }
    }

    @EventHandler
    public void onZeroHealth(WarHealthChangeEvent event) {
        if (event.isZeroHealth()) {
            WarringEntity warringEntity = WarDatabase.getWarringEntity(event.getHealth());
            if (warringEntity == null)
                return;

            War war = warringEntity.getWar();
            WarringEntity enemy = WarUtil.getOpposingEntity(warringEntity);

            war.endWar(enemy, warringEntity);
        }

    }

    @EventHandler
    public void onWarDeclaration(WarDeclareEvent event) {
        Declarer declarer = event.getDeclarer();
        WarTarget target = event.getTarget();

        if (event.getDeclarationWarBook().getType() == WarType.TOWNWAR) {
            Town declaringTown = declarer.town();
            Town targetTown = target.town();
            notifyDeclaration(targetTown, declaringTown);
        }

        if (event.getDeclarationWarBook().getType() == WarType.NATIONWAR) {
            Nation declaringNation = declarer.nation();
            Nation targetNation = target.nation();
            notifyDeclaration(targetNation, declaringNation);
        }
    }

    private void giveBonusClaims(Nation winner, Nation loser) {
        double bonusClaims = loser.getNumTownblocks() * 0.25;
        winner.getCapital().addBonusBlocks((int) bonusClaims);
    }

    private void notifyDeclaration(Nation targetNation, Nation declaringNation) {
        List<Resident> targetResidents = targetNation.getResidents();
        Title declarationTitle = getTitle("<dark_red><bold>War Declaration!", "<yellow>" + declaringNation.getFormattedName() + "<red> has declared war on your nation");
        notifyResidents(targetResidents, declarationTitle);

        List<Resident> warringResidents = targetNation.getResidents();
        Title warringTitle = getTitle("<red><bold>War Declaration!", "<red>Your nation has declared war on <yellow>" + targetNation.getFormattedName());
        notifyResidents(warringResidents, warringTitle);
    }

    private void notifyDeclaration(Town town, Town declaringTown) {
        List<Resident> targetResidents = town.getResidents();
        Title declarationTitle = getTitle("<red><bold>War Declaration!", "<yellow>" + declaringTown.getFormattedName() + "<red>has declared war on you");
        notifyResidents(targetResidents, declarationTitle);

        List<Resident> warringResidents = declaringTown.getResidents();
        Title warringTitle = getTitle("<red><bold>War Declaration!", "<red>Your town has declared war on <yellow>" + town.getFormattedName());
        notifyResidents(warringResidents, warringTitle);

    }

    private void notifyResidents(List<Resident> residents, Title title) {
        for (Resident resident : residents) {
            Player player = resident.getPlayer();
            if (player == null)
                continue;
            player.showTitle(title);
            player.playSound(player, Sound.EVENT_RAID_HORN, 75, 1);
        }
    }

    private Title getTitle(String main, String sub) {
        Component mainTitle = UnitedWars.MINI_MESSAGE.deserialize(main);
        Component subTitle = UnitedWars.MINI_MESSAGE.deserialize(sub);
        return Title.title(mainTitle, subTitle);
    }
}
