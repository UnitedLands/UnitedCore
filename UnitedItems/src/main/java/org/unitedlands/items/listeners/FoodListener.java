package org.unitedlands.items.listeners;

import dev.lone.itemsadder.api.CustomStack;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;

public class FoodListener implements Listener {
    @EventHandler
    public void onDonutEat(PlayerItemConsumeEvent event) {
        // TODO: Cosmic donut makes players fly for 10 seconds
    }

    @EventHandler
    public void onPepperEat(PlayerItemConsumeEvent event) {
        // TODO: Give pepper effects and burn player for 2 seconds

    }


}
