package org.unitedlands.wars.listeners;

import com.palmergames.bukkit.towny.event.player.PlayerKilledPlayerEvent;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import de.jeff_media.angelchest.AngelChest;
import de.jeff_media.angelchest.AngelChestPlugin;
import de.jeff_media.angelchest.events.AngelChestSpawnEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;
import org.unitedlands.wars.UnitedWars;
import org.unitedlands.wars.Utils;
import org.unitedlands.wars.war.WarDataController;
import org.unitedlands.wars.war.WarDatabase;
import org.unitedlands.wars.war.entities.WarringEntity;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.minimessage.tag.resolver.Placeholder.component;
import static org.unitedlands.wars.Utils.*;
import static org.unitedlands.wars.war.WarUtil.hasSameWar;

public class PlayerListener implements Listener {
    private final UnitedWars unitedWars;
    private final FileConfiguration config;

    public PlayerListener(UnitedWars unitedWars) {
        this.unitedWars = unitedWars;
        config = unitedWars.getConfig();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        Town town = getPlayerTown(player);
        if (town == null) return;
        if (!town.hasActiveWar()) return;

        if (isBannedWorld(player.getWorld().getName()))
            teleportPlayerToSpawn(player);

        for (String command : config.getStringList("commands-on-login"))
            player.performCommand(command);
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBannedWorldTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        Town town = getPlayerTown(player);
        if (town == null) return;
        if (!town.hasActiveWar()) return;

        if (isBannedWorld(event.getTo().getWorld().getName())) {
            event.setCancelled(true);
            player.sendMessage(Utils.getMessage("teleport-cancelled"));
            player.playSound(player, Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 1, 0.6F);
        }

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
    public void onPlayerKillPlayer(PlayerKilledPlayerEvent event) {
        Resident killer = getTownyResident(event.getKiller());
        Resident victim = getTownyResident(event.getVictim());

        if (hasSameWar(killer, victim)) {
            // Killer doesn't have lives, return
            if (!WarDataController.hasResidentLives(killer))
                return;

            if (WarDataController.hasResidentLives(victim)) {
                WarDataController.decrementResidentLives(victim);
                WarringEntity warringEntity = WarDatabase.getWarringEntity(victim.getPlayer());
                warringEntity.getWarHealth().decreaseHealth(5);
                warringEntity.getWarHealth().decreaseMaxHealth(5);

                Component message = getPlayerDeathMessage(warringEntity, killer, victim);

                if (WarDataController.getResidentLives(victim) == 0) {
                    notifyWarKick(victim.getPlayer(), warringEntity);
                    return;
                }
                for (Resident resident : warringEntity.getWar().getResidents()) {
                    if (resident.getPlayer() != null) {
                        resident.getPlayer().sendMessage(message);
                    }
                }
            }
        }
    }

    private void notifyWarKick(Player player, WarringEntity warringEntity) {
        Title title = getTitle("<dark_red><bold>OUT OF LIVES!", "<red>You've lost all your <yellow>3</yellow> lives!");
        player.showTitle(title);
        // player.playSound(player, Sound.ITEM_GOAT_HORN_SOUND_7, 1f, 1f);

        Component message = getMessage("removed-from-war",
                component("victim-warrer", text(warringEntity.name())));
        warringEntity.getWar().getResidents().forEach(resident -> {
            if (resident.isOnline()) {
                resident.getPlayer().sendMessage(message);
            }
        });


    }

    @NotNull
    private Component getPlayerDeathMessage(WarringEntity entity, Resident killer, Resident victim) {
        return Utils.getMessage("player-killed",
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

            for (Resident resident : warringEntity.getWar().getResidents()) {
                if (resident.getPlayer() != null) {
                    resident.getPlayer().sendMessage(message);
                }
            }
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
}
