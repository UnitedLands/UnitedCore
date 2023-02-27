package org.unitedlands.items.listeners;

import dev.lone.itemsadder.api.CustomStack;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.unitedlands.items.UnitedItems;

public class FoodListener implements Listener {
    private final UnitedItems plugin;

    public FoodListener(UnitedItems plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDonutEat(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();
        ItemStack cosmicDonut = CustomStack.getInstance("unitedlands:cosmic_donut").getItemStack();
        if (!item.equals(cosmicDonut))
            return; // The item is not a cosmic donut, return and end the code here.

        Player player = event.getPlayer();
        // Give night vision effect
        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 2 * 60 * 20, 0));
        // Player is not allowed to fly, so make them fly.
        if (!player.getAllowFlight()) {
            player.setAllowFlight(true);
            // Remove flight after 25 seconds
            Bukkit.getServer().getScheduler().runTaskLater(plugin, () -> player.setAllowFlight(false), 25 * 20);
        }
    }

    @EventHandler
    public void onPepperEat(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();
        ItemStack hotPepper = CustomStack.getInstance("unitedlands:hot_pepper").getItemStack();
        if (!item.equals(hotPepper))
            return; // The item is not a hot pepper, return and end the code here.
        Player player = event.getPlayer();
        player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 5 * 20, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 5 * 20, 1));
        player.setFireTicks(2 * 20);

    }


}