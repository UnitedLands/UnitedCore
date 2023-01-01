package org.unitedlands.transport.listeners;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class MiscListener implements Listener {

    @EventHandler
    public void onPlayerTouchArk(PlayerMoveEvent event) {
        if (!event.hasChangedBlock())
            return;
        if (!event.getTo().getWorld().getName().equalsIgnoreCase("world_earth"))
            return;
        Location to = event.getTo();
        if (to.getBlockY() < 90)
            return;
        if (to.getBlockX() != 7220)
            return;
        if (to.getBlockZ() != -6638)
            return;

        Player player = event.getPlayer();
        to.getWorld().spawnEntity(to, EntityType.LIGHTNING);

        player.playSound(to, Sound.ITEM_TRIDENT_THUNDER, 1f, 1f);
        player.playSound(to, Sound.BLOCK_END_PORTAL_SPAWN, 1f, 1f);
        player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 200, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 200, 1));
        Vector vector = to.toVector().subtract(event.getFrom().toVector()).add(new Vector(0, 30, 0));
        player.setVelocity(vector.multiply(-10));
    }
}
