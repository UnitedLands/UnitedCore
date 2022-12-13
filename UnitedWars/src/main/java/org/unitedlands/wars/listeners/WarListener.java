package org.unitedlands.wars.listeners;

import com.palmergames.bukkit.towny.event.NewDayEvent;
import com.palmergames.bukkit.towny.event.damage.TownBlockPVPTestEvent;
import com.palmergames.bukkit.towny.event.statusscreen.TownStatusScreenEvent;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.unitedlands.wars.UnitedWars;
import org.unitedlands.wars.books.TokenCostCalculator;
import org.unitedlands.wars.books.data.Declarer;
import org.unitedlands.wars.books.data.WarTarget;
import org.unitedlands.wars.events.WarDeclareEvent;
import org.unitedlands.wars.events.WarHealthChangeEvent;
import org.unitedlands.wars.war.War;
import org.unitedlands.wars.war.WarDataController;
import org.unitedlands.wars.war.WarDatabase;
import org.unitedlands.wars.war.WarUtil;
import org.unitedlands.wars.war.entities.WarringEntity;

import java.util.HashSet;
import java.util.List;

public class WarListener implements Listener {


    @EventHandler
    public void onTownBlockPVPTest(TownBlockPVPTestEvent event) {
        if (WarDatabase.getWars().isEmpty())
            return;

        Town town = event.getTown();
        if (WarDatabase.getWarringTown(town) != null
                || WarDatabase.getWarringNation(town.getNationOrNull()) != null) {
            event.setPvp(true);
        }

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

        Bukkit.getServer().getScheduler().runTask(UnitedWars.getInstance(), () -> {
            for (WarringEntity warringEntity : WarDatabase.getWarringEntities()) {
                tryEndingWar(warringEntity);
                // Add 3 lives for each resident, up to a max of 6.
                addResidentLives(warringEntity);
            }
        });
    }

    private void addResidentLives(WarringEntity warringEntity) {
        for (Resident resident: warringEntity.getWarParticipants()) {
            int currentLives = WarDataController.getResidentLives(resident);
            WarDataController.setResidentLives(resident, Math.min(6, currentLives + 3));
        }
    }

    private void tryEndingWar(WarringEntity warringEntity) {
        int current = warringEntity.getWarHealth().getValue();
        WarringEntity opposingEntity = WarUtil.getOpposingEntity(warringEntity);
        int enemyCurrent = opposingEntity.getWarHealth().getValue();

        warringEntity.getWarHealth().setHealth(Math.max(0, current - 20));
        if (warringEntity.getWarHealth().getValue() == 0) {
            WarringEntity winner;
            WarringEntity loser;
            // If the original entity had less than the enemy, then they lost the war.
            if (current < enemyCurrent) {
                winner = opposingEntity;
                loser = warringEntity;
            } else {
                winner = warringEntity;
                loser = opposingEntity;
            }
            warringEntity.getWar().endWar(winner, loser);
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

        WarringEntity declaringEntity = WarDatabase.getWarringEntity(declarer.player());
        WarringEntity targetEntity = WarDatabase.getWarringEntity(target.targetMayor().getUniqueId());

        notifyDeclaration(targetEntity, declaringEntity);
    }


    private void notifyDeclaration(WarringEntity target, WarringEntity declarer) {
        Title declarationTitle = getTitle("<dark_red><bold>War Declaration!", "<yellow>" + declarer.name() + "</yellow><red> has declared a war on <yellow>" + target.name() + "</yellow>!");

        notifyResidents(target.getOnlinePlayers(), declarationTitle);
        notifyResidents(declarer.getOnlinePlayers(), declarationTitle);
    }

    private void notifyResidents(HashSet<Player> players, Title title) {
        for (Player player : players) {
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
