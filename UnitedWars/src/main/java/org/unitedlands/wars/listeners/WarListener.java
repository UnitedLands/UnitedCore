package org.unitedlands.wars.listeners;

import com.palmergames.bukkit.towny.event.NewDayEvent;
import com.palmergames.bukkit.towny.event.statusscreen.NationStatusScreenEvent;
import com.palmergames.bukkit.towny.event.statusscreen.TownStatusScreenEvent;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.economy.BankAccount;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import org.unitedlands.wars.UnitedWars;
import org.unitedlands.wars.books.TokenCostCalculator;
import org.unitedlands.wars.books.data.Declarer;
import org.unitedlands.wars.books.data.WarTarget;
import org.unitedlands.wars.events.WarDeclareEvent;
import org.unitedlands.wars.events.WarEndEvent;
import org.unitedlands.wars.war.War;
import org.unitedlands.wars.war.WarDataController;
import org.unitedlands.wars.war.WarDatabase;
import org.unitedlands.wars.war.WarType;
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
            event.getStatusScreen().addComponentOf("dailyWarTokens", "§2Daily War Tokens: §a" + costCalculator.calculateTokenIncome());
        }
    }

    @EventHandler
    public void onNationStatus(NationStatusScreenEvent event) {
        Nation nation = event.getNation();
        if (!nation.isNeutral()) {
            TokenCostCalculator costCalculator = new TokenCostCalculator(nation);
            event.getStatusScreen().addComponentOf("dailyWarTokens", "§2Daily War Tokens: §a" + costCalculator.calculateTokenIncome());
        }
    }

    @EventHandler
    public void onTownWarEnd(WarEndEvent event) {
        War war = event.getWar();
        if (!war.getWarType().equals(WarType.TOWNWAR))
            return;

        WarringTown winner = (WarringTown) event.getWinner();
        WarringTown loser = (WarringTown) event.getLoser();

        giveWarEarnings(winner.getTown(), loser.getTown());

        // Remove from tracker
        WarDatabase.removeWarringEntity(loser);
        WarDatabase.removeWarringEntity(winner);
    }

    @EventHandler
    public void onNationWarEnd(WarEndEvent event) {
        War war = event.getWar();
        if (!war.getWarType().equals(WarType.NATIONWAR))
            return;

        // We know it's going to be a nation, since it's a nation war
        WarringNation winner = (WarringNation) event.getWinner();
        WarringNation loser = (WarringNation) event.getLoser();

        for (Town losingTown : loser.getNation().getTowns()) {
            giveWarEarnings(winner.getNation().getCapital(), losingTown);
        }

        giveAdditionalNationEarnings(winner.getNation(), loser.getNation());

        // Remove from tracker
        WarDatabase.removeWarringEntity(loser);
        WarDatabase.removeWarringEntity(winner);
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

    private void giveWarEarnings(Town winningTown, Town losingTown) {
        BankAccount winnerAccount = winningTown.getAccount();
        double amount = winnerAccount.getHoldingBalance() * 0.5;
        losingTown.getAccount().withdraw(amount, "Lost a war");
        winnerAccount.deposit(amount, "Won a war");
        giveBonusClaims(winningTown, losingTown);
    }

    private void giveAdditionalNationEarnings(Nation winningNation, Nation losingNation) {
        double amount = losingNation.getAccount().getHoldingBalance() * 0.5;
        losingNation.getAccount().withdraw(amount, "Lost a war");
        winningNation.getAccount().deposit(amount, "Won a war");
    }

    private void giveBonusClaims(Town winner, Town loser) {
        double bonusClaims = loser.getNumTownBlocks() * 0.25;
        winner.addBonusBlocks((int) bonusClaims);
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
        Title declarationTitle =  getTitle("<red><bold>War Declaration!", "<yellow>" + declaringTown.getFormattedName() + "<red>has declared war on you");
        notifyResidents(targetResidents, declarationTitle);

        List<Resident> warringResidents = declaringTown.getResidents();
        Title warringTitle = getTitle("<red><bold>War Declaration!", "<red>Your town has declared war on <yellow>" + town.getFormattedName());
        notifyResidents(warringResidents, warringTitle);

    }

    private void notifyResidents(List<Resident> residents, Title title) {
        for (Resident resident: residents) {
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
