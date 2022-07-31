package org.unitedlands.pvp.listeners;

import com.palmergames.bukkit.towny.event.player.PlayerKilledPlayerEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.unitedlands.pvp.UnitedPvP;
import org.unitedlands.pvp.player.PvpPlayer;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;


public class PlayerListener implements Listener {
    private final UnitedPvP unitedPvP;
    HashMap<EnderCrystal, UUID> crystalMap = new HashMap<>();

    public PlayerListener(UnitedPvP unitedPvP) {
        this.unitedPvP = unitedPvP;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PvpPlayer file = new PvpPlayer(player);

        if (!player.hasPlayedBefore() || !file.getPlayerFile().exists()) {
            PvpPlayer pvpPlayer = new PvpPlayer(player);
            pvpPlayer.createFile();
        }
    }

    @EventHandler
    public void onPlayerKillPlayer(PlayerKilledPlayerEvent event) {
        Player killer = event.getKiller();
        PvpPlayer killerPvP = new PvpPlayer(killer);
        killerPvP.setHostility(killerPvP.getHostility() + 1);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;
        if (!event.getClickedBlock().getType().equals(Material.OBSIDIAN)) return;
        // The player clicked an obsidian but didn't have a crystal
        if (!event.getMaterial().equals(Material.END_CRYSTAL)) return;

        Bukkit.getScheduler().runTask(unitedPvP, () -> {
            List<Entity> entities = event.getPlayer().getNearbyEntities(4, 4, 4);
            for (Entity entity : entities) {
                // Get the nearest crystal
                if (entity instanceof EnderCrystal crystal) {
                    Block belowCrystal = crystal.getLocation().getBlock().getRelative(BlockFace.DOWN);
                    // Check if the block below the newly spawned crystal is the same as the block the player
                    // clicked in the event.
                    if (event.getClickedBlock().equals(belowCrystal)) {
                        // Save it.
                        crystalMap.put(crystal, event.getPlayer().getUniqueId());
                        break;
                    }
                }
            }
        });
    }

    @EventHandler
    public void onPlayerDeathByCrystal(PlayerDeathEvent event) {
        if (event.getEntity() instanceof EnderCrystal crystal) {
            // This crystal is not registered.
            if (!crystalMap.containsKey(crystal)) return;
            Player originalPlacer = Bukkit.getPlayer(crystalMap.get(crystal));
            // Player might be null
            if (originalPlacer == null) return;
            // Player might've killed themselves by accident, don't do anything;
            if (originalPlacer.equals(event.getPlayer())) return;

            // Increase the hostility of whoever placed the crystal.
            PvpPlayer pvpPlacer = new PvpPlayer(originalPlacer);
            pvpPlacer.setHostility(pvpPlacer.getHostility() + 1);
        }
    }

    @EventHandler
    public void onCrystalExplode(EntityCombustEvent event) {
        if (event.getEntity() instanceof EnderCrystal crystal) {
            // Remove the crystal from the map a second after it explodes, to have time to detect player deaths etc.
            unitedPvP.getServer().getScheduler().runTaskLater(unitedPvP, () -> crystalMap.remove(crystal), 20L);
        }
    }

}
