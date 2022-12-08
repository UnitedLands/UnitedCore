package org.unitedlands.wars.events;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.unitedlands.wars.war.health.WarHealth;

public class WarHealthChangeEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final WarHealth health;
    private final int newHealth;
    private final int newMax;

    public WarHealthChangeEvent(WarHealth health, int newHealth, int newMax) {
        super(!Bukkit.getServer().isPrimaryThread());
        this.health = health;
        this.newHealth = newHealth;
        this.newMax = newMax;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public WarHealth getHealth() {
        return health;
    }


    public boolean isZeroHealth() {
        return newHealth == 0 || newMax == 0;
    }
}