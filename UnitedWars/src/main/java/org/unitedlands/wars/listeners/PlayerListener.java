package org.unitedlands.wars.listeners;

import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.jail.Jail;
import com.palmergames.bukkit.towny.utils.SpawnUtil;
import de.jeff_media.angelchest.AngelChest;
import de.jeff_media.angelchest.AngelChestPlugin;
import de.jeff_media.angelchest.events.AngelChestSpawnEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.player.*;
import org.bukkit.projectiles.ProjectileSource;
import org.jetbrains.annotations.NotNull;
import org.unitedlands.wars.UnitedWars;
import org.unitedlands.wars.Utils;
import org.unitedlands.wars.war.War;
import org.unitedlands.wars.war.WarDataController;
import org.unitedlands.wars.war.WarDatabase;
import org.unitedlands.wars.war.entities.WarringEntity;

import java.util.Optional;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.minimessage.tag.resolver.Placeholder.component;
import static org.unitedlands.wars.Utils.*;
import static org.unitedlands.wars.war.WarUtil.hasSameWar;

public class PlayerListener implements Listener {
    private final FileConfiguration config;

    public PlayerListener(UnitedWars unitedWars) {
        config = unitedWars.getConfig();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        Town town = getPlayerTown(player);
        if (town == null)
            return;
        if (!town.hasActiveWar()) {
            Resident resident = getTownyResident(player);
            if (!resident.isKing())
                return;
            notifyAllyWar(resident);
            return;
        }

        if (isBannedWorld(player.getWorld().getName()))
            teleportPlayerToSpawn(player);

        for (String command : config.getStringList("commands-on-login"))
            player.performCommand(command);
    }

    private void notifyAllyWar(Resident resident) {
        Nation nation = resident.getNationOrNull();
        for (Nation ally: nation.getAllies())
            if (WarDatabase.hasNation(ally))
                resident.getPlayer().sendMessage(getMessage("ally-has-war", Placeholder.component("ally", text(ally.getFormattedName()))));
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        Resident resident = getTownyResident(player);
        if (resident == null)
            return;
        Town town = resident.getTownOrNull();
        if (town == null)
            return;
        if (!town.hasActiveWar())
            return;
        PlayerTeleportEvent.TeleportCause cause = event.getCause();
        if (!event.hasChangedBlock())
            return;
        if (cause == PlayerTeleportEvent.TeleportCause.CHORUS_FRUIT || cause == PlayerTeleportEvent.TeleportCause.ENDER_PEARL)
            return;
        // they can bypass.
        if (player.hasPermission("united.wars.bypass.tp"))
            return;

        War war = WarDatabase.getWar(town);
        if (war == null)
            return;
        // Allow teleportation during war prep time.
        if (war.hasActiveTimer())
            return;
        if (event.getFrom().getWorld().equals(event.getTo().getWorld())) {
            double distance = event.getFrom().distance(event.getTo());
            // Too small, don't bother.
            if (distance <= 50)
                return;
        }
        event.setCancelled(true);
        player.sendMessage(Utils.getMessage("teleport-cancelled"));
        player.playSound(player, Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 1, 0.6F);

    }

    @EventHandler
    public void onFlight(PlayerToggleFlightEvent event) {
        if (!WarDatabase.hasWar(event.getPlayer()))
            return;
        if (event.getPlayer().hasPermission("united.wars.bypass.fly"))
            return;
        event.setCancelled(true);
        event.getPlayer().setFlying(false);
        event.getPlayer().setAllowFlight(false);
    }
    @EventHandler
    public void onCommandExecute(PlayerCommandPreprocessEvent event) {
        if (!config.getStringList("banned-commands").contains(event.getMessage())) return;
        Player player = event.getPlayer();
        Town town = getPlayerTown(player);
        if (town == null) return;
        if (town.hasActiveWar()) {
            player.sendMessage(Utils.getMessage("banned-command"));
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onGraveCreation(AngelChestSpawnEvent event) {
        Resident resident = getTownyResident(event.getAngelChest().getPlayer().getUniqueId());
        if (resident == null) return;
        if (resident.hasTown()) {
            if (resident.getTownOrNull().hasActiveWar()) {
                event.getAngelChest().setProtected(false);
            }
        }
    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        LivingEntity dead = event.getEntity();

        if (!(dead instanceof Player))
            return;

        Optional<Player> foundKiller = findKiller(dead);
        if (foundKiller.isEmpty()) {
            runRegularDeathProccess((Player) dead);
            return;
        }

        Resident killer = getTownyResident(foundKiller.get());
        Resident victim = getTownyResident((Player) dead);

        if (!hasSameWar(killer, victim))
            return;

        // Killer doesn't have lives, return
        if (!WarDataController.hasResidentLives(killer))
            return;

        if (!WarDataController.hasResidentLives(victim))
            return;

        WarringEntity warringEntity = WarDatabase.getWarringEntity(victim.getPlayer());
        if (warringEntity.getWar().hasActiveTimer())
            return;

        decreaseHealth(victim, warringEntity);
        playSounds(warringEntity);
        if (WarDataController.getResidentLives(victim) == 0) {
            notifyWarKick(victim.getPlayer(), warringEntity);
            jailResident(victim, warringEntity);
            return;
        }
        Component message = getPlayerDeathMessage(warringEntity, killer, victim);
        warringEntity.getWar().broadcast(message);

    }

    private void jailResident(Resident victim, WarringEntity warringEntity) {
        Jail jail = getJail(warringEntity);
        if (jail == null)
            return;
        victim.setJail(jail);
        victim.setJailCell(0);
        victim.setJailHours(3);
        victim.save();
        TownyUniverse.getInstance().getJailedResidentMap().add(victim);
        SpawnUtil.jailTeleport(victim);
        victim.getPlayer().sendMessage(getMessage("you-were-jailed"));
    }

    private Jail getJail(WarringEntity warringEntity) {
        if (warringEntity.getGovernment() instanceof Town town)
            if (town.hasJails())
                return town.getPrimaryJail();
        else {
            Nation nation = (Nation) warringEntity.getGovernment();
            if (nation.getCapital().hasJails())
                return nation.getCapital().getPrimaryJail();
        }
        return null;
    }

    private void runRegularDeathProccess(Player dead) {
        Resident resident = getTownyResident(dead);
        if (!WarDatabase.hasWar(dead))
            return;
        if (!WarDataController.hasResidentLives(resident))
            return;
        WarringEntity warringEntity = WarDatabase.getWarringEntity(dead);
        if (warringEntity.getWar().hasActiveTimer())
            return;
        // Decrease the health by 3
        warringEntity.getWarHealth().decreaseHealth(3);
        warringEntity.getWarHealth().flash();
        warringEntity.getWar().broadcast(getMessage("regular-death",
                component("victim",
                        text(dead.getName())),
                component("victim-warrer",
                        text(warringEntity.name()))));
        playSounds(warringEntity);

    }

    private void decreaseHealth(Resident victim, WarringEntity warringEntity) {
        warringEntity.getWarHealth().decreaseHealth(5);
        warringEntity.getWarHealth().decreaseMaxHealth(5);
        warringEntity.getWarHealth().flash();
        WarDataController.decrementResidentLives(victim);
    }

    private void playSounds(WarringEntity warringEntity) {
        warringEntity.getOnlinePlayers().forEach(player -> player.playSound(player, Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 1, 1));
        WarringEntity enemy = warringEntity.getEnemy();
        enemy.getOnlinePlayers().forEach(player -> player.playSound(player, Sound.BLOCK_NOTE_BLOCK_BIT, 1f, 1f));
    }

    private void notifyWarKick(Player player, WarringEntity warringEntity) {
        Title title = getTitle("<dark_red><bold>OUT OF LIVES!", "<red>You've lost all your lives!");
        player.showTitle(title);
        player.playSound(player, Sound.ITEM_GOAT_HORN_SOUND_7, 1f, 1f);

        Component message = getMessage("removed-from-war",
                component("victim", text(player.getName())),
                component("jailer", text(warringEntity.getEnemy().name())));

        warringEntity.getWar().broadcast(message);
    }

    @NotNull
    private Component getPlayerDeathMessage(WarringEntity entity, Resident killer, Resident victim) {
        String message = "player-killed";
        if (killer == victim)
            message = "player-killed-self";
        return Utils.getMessage(message,
                component("victim",
                        text(victim.getName())),
                component("killer",
                        text(killer.getName())),
                component("victim-warrer",
                        text(entity.name())));
    }

    @EventHandler
    public void onTotemPop(EntityResurrectEvent event) {
        if (event.isCancelled())
            return;

        LivingEntity entity = event.getEntity();
        if (entity instanceof Player victim) {
            WarringEntity warringEntity = WarDatabase.getWarringEntity(victim);
            if (warringEntity == null)
                return;
            warringEntity.getWarHealth().decreaseHealth(1);

            Component message = Utils.getMessage("totem-pop",
                    component("victim", text(victim.getName())),
                    component("victim-warrer", text(warringEntity.name())));

            warringEntity.getWar().broadcast(message);
        }
    }

    @EventHandler
    public void onGraveInteract(PlayerInteractEvent event) {
        if (!event.hasBlock()) return;
        if (event.getClickedBlock().getType() != Material.SOUL_CAMPFIRE) return;

        AngelChestPlugin plugin = (AngelChestPlugin) Bukkit.getPluginManager().getPlugin("AngelChest");
        AngelChest chest = plugin.getAngelChestAtBlock(event.getClickedBlock());
        if (chest == null) return;

        Resident openingResident = getTownyResident(event.getPlayer());
        Resident graveResident = getTownyResident(chest.getPlayer().getUniqueId());
        if (hasSameWar(openingResident, graveResident)) {
            chest.setProtected(false);
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        if (!WarDatabase.hasWar(player))
            return;
        if (isBannedWorld(event.getRespawnLocation().getWorld().getName()))
            Utils.teleportPlayerToSpawn(player);
    }

    public Optional<Player> findKiller(Entity dead) {
        EntityDamageEvent entityDamageEvent = dead.getLastDamageCause();
        if (!(entityDamageEvent instanceof EntityDamageByEntityEvent)) {
            // Not damaged by entity, can't be a player
            return Optional.empty();
        }

        Entity killer = ((EntityDamageByEntityEvent) entityDamageEvent).getDamager();
        if (killer instanceof Player) return Optional.of((Player) killer);
        if (killer instanceof Tameable) return getOwner((Tameable) killer);
        if (killer instanceof Projectile) return getShooter((Projectile) killer);
        if (killer instanceof EnderCrystal) return findKiller(killer); // Recursive call

        return Optional.empty();
    }

    private Optional<Player> getShooter(Projectile projectile) {
        ProjectileSource source = projectile.getShooter();
        if (source instanceof Player) {
            return Optional.of((Player) source);
        }

        return Optional.empty();
    }

    private Optional<Player> getOwner(Tameable tameable) {
        if (!tameable.isTamed()) {
            return Optional.empty();
        }

        AnimalTamer owner = tameable.getOwner();
        if (owner instanceof Player) {
            return Optional.of((Player) owner);
        }

        return Optional.empty();
    }
}
