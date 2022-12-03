package org.unitedlands.wars.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.unitedlands.wars.war.health.WarHealth;

public class WarHealthChangeEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final WarHealth health;
    private final int previousHealth;
    private final int newHealth;
    private final int previousMax;
    private final int newMax;

    public WarHealthChangeEvent(WarHealth health, int previousHealth,  int previousMax, int newHealth, int newMax) {
        this.health = health;
        this.previousHealth = previousHealth;
        this.newHealth = newHealth;
        this.previousMax = previousMax;
        this.newMax = newMax;
    }

    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public WarHealth getHealth() {
        return health;
    }

    public int getPreviousHealth() {
        return previousHealth;
    }

    public int getNewHealth() {
        return newHealth;
    }

    public int getPreviousMax() {
        return previousMax;
    }

    public int getNewMax() {
        return newMax;
    }
}
