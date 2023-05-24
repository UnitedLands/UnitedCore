package org.unitedlands.wars.listeners;

import com.palmergames.bukkit.towny.event.NewDayEvent;
import com.palmergames.bukkit.towny.event.damage.TownyPlayerDamagePlayerEvent;
import com.palmergames.bukkit.towny.event.statusscreen.TownStatusScreenEvent;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent.Cause;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.unitedlands.wars.UnitedWars;
import org.unitedlands.wars.Utils;
import org.unitedlands.wars.books.TokenCostCalculator;
import org.unitedlands.wars.events.WarDeclareEvent;
import org.unitedlands.wars.events.WarHealthChangeEvent;
import org.unitedlands.wars.war.War;
import org.unitedlands.wars.war.WarDataController;
import org.unitedlands.wars.war.WarDatabase;
import org.unitedlands.wars.war.WarUtil;
import org.unitedlands.wars.war.entities.WarringEntity;
import org.unitedlands.wars.war.health.WarHealth;

import java.util.HashSet;
import java.util.List;

import static net.kyori.adventure.text.Component.text;
import static org.unitedlands.wars.Utils.*;

public class WarListener implements Listener {


    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDamagePlayer(TownyPlayerDamagePlayerEvent event) {
        if (WarDatabase.getWars().isEmpty())
            return;

        if (event.getAttackingResident() == null || event.getVictimResident() == null)
            return;
        if (WarUtil.hasSameWar(event.getAttackingResident(), event.getVictimResident()))
            event.setCancelled(false);

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

        if (player.hasPotionEffect(PotionEffectType.INVISIBILITY))
            return;
        // No lives, don't bother.
        if (!WarDataController.hasResidentLives(Utils.getTownyResident(player)))
            return;
        warringEntity.getWarHealth().addHealingPlayer(player.getUniqueId());

    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        WarringEntity warringEntity = WarDatabase.getWarringEntity(player);
        if (warringEntity == null)
            return;
        WarHealth warHealth = warringEntity.getWarHealth();
        warHealth.removeHealingPlayer(player.getUniqueId());
    }
    @EventHandler
    public void onPlayerTurnInvisible(EntityPotionEffectEvent event) {
        if (!(event.getEntity() instanceof Player player))
            return;
        if (!WarDatabase.hasWar(player))
            return;
        PotionEffect effect = event.getNewEffect();
        if (effect == null)
            return;
        if (event.getAction() != EntityPotionEffectEvent.Action.ADDED)
            return;
        if (!effect.getType().equals(PotionEffectType.INVISIBILITY))
            return;
        if (player.isInvisible())
            return; // Already invisible, don't check again
        WarringEntity entity = WarDatabase.getWarringEntity(player);
        entity.getWarHealth().removeHealingPlayer(player.getUniqueId());
    }

    @EventHandler
    public void onPlayerTurnVisible(EntityPotionEffectEvent event) {
        if (!(event.getEntity() instanceof Player player))
            return;
        if (!WarDatabase.hasWar(player))
            return;
        PotionEffect effect = event.getOldEffect();
        if (effect == null)
            return;
        if (!effect.getType().equals(PotionEffectType.INVISIBILITY))
            return;
        EntityPotionEffectEvent.Action action = event.getAction();
        if (action == EntityPotionEffectEvent.Action.REMOVED || action == EntityPotionEffectEvent.Action.CLEARED) {
            WarringEntity entity = WarDatabase.getWarringEntity(player);
            entity.getWarHealth().addHealingPlayer(player.getUniqueId());
        }
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
            WarDataController.setResidentLives(resident, Math.min(3, currentLives + 3));
        }
    }

    private void tryEndingWar(WarringEntity warringEntity) {
        int current = warringEntity.getWarHealth().getValue();
        WarringEntity enemy = warringEntity.getEnemy();
        int enemyCurrent = enemy.getWarHealth().getValue();

        warringEntity.getWarHealth().decreaseHealth(calculateDailyHealthDecrease(warringEntity));
        if (warringEntity.getWarHealth().getValue() != 0)
            return;

        War war = warringEntity.getWar();
        // They both reached 0 with no differences.
        if (current == enemyCurrent)
            war.tieWar(warringEntity, enemy);
        // If the original entity had less than the enemy, then they lost the war.
        else if (current < enemyCurrent)
            war.endWar(enemy, warringEntity);
        else
            war.endWar(warringEntity, enemy);
    }

    private int calculateDailyHealthDecrease(WarringEntity entity) {
        War war = entity.getWar();
        // Get the current timestamp
        long currentTimestamp = System.currentTimeMillis();

        // Calculate the number of milliseconds elapsed
        long millisecondsPassed = currentTimestamp - war.getStartTime();

        // Convert milliseconds to days
        long daysPassed = (long) Math.floor(millisecondsPassed / (1000.0 * 60 * 60 * 24));

        int damageAmount = Math.toIntExact(10 + (daysPassed * 5));
        return Math.min(80, damageAmount);
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
            war.endWar(warringEntity.getEnemy(), warringEntity);
        }

    }

    @EventHandler
    public void onWarDeclaration(WarDeclareEvent event) {
        WarringEntity declaringEntity = event.getDeclaringEntity();
        WarringEntity targetEntity = event.getTargetEntity();

        notifyDeclaration(targetEntity, declaringEntity);
        // Global broadcast.
        Bukkit.broadcast(getMessage("war-start-broadcast",
                Placeholder.component("declarer", text(declaringEntity.name())),
                Placeholder.component("victim", text(targetEntity.name()))));
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
}
