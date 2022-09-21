package org.unitedlands.war.listeners;

import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import de.jeff_media.angelchest.events.AngelChestSpawnEvent;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.unitedlands.war.UnitedWars;
import org.unitedlands.war.Utils;

import static org.unitedlands.war.Utils.*;

public class PlayerListener implements Listener {
    private final UnitedWars unitedWars;
    private final FileConfiguration config;

    public PlayerListener(UnitedWars unitedWars) {
        this.unitedWars = unitedWars;
        config = unitedWars.getConfig();
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTeleport(PlayerTeleportEvent event) {
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
        Resident resident =  getTownyResident(event.getAngelChest().getPlayer().getUniqueId());
        if (resident == null) return;
        if (resident.hasTown()) {
            if (resident.getTownOrNull().hasActiveWar()) {
                event.getAngelChest().setProtected(false);
            }
        }
    }

}
