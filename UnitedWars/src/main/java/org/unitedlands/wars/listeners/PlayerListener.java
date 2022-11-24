package org.unitedlands.wars.listeners;

import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import de.jeff_media.angelchest.AngelChest;
import de.jeff_media.angelchest.AngelChestPlugin;
import de.jeff_media.angelchest.events.AngelChestSpawnEvent;
import io.github.townyadvanced.eventwar.util.WarUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.unitedlands.wars.UnitedWars;
import org.unitedlands.wars.Utils;

import static org.unitedlands.wars.Utils.*;

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

        for (String command: config.getStringList("commands-on-login"))
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
    public void onGraveInteract(PlayerInteractEvent event) {
        if (!event.hasBlock()) return;
        if (event.getClickedBlock().getType() != Material.SOUL_CAMPFIRE) return;

        AngelChestPlugin plugin = (AngelChestPlugin) Bukkit.getPluginManager().getPlugin("AngelChest");
        AngelChest chest = plugin.getAngelChestAtBlock(event.getClickedBlock());
        if (chest == null) return;

        Resident openingResident = getTownyResident(event.getPlayer());
        Resident graveResident = getTownyResident(chest.getPlayer().getUniqueId());
        if (WarUtil.hasSameWar(openingResident, graveResident)) {
            chest.setProtected(false);
        }
    }
}
