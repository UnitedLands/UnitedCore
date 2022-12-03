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

    public WarHealthChangeEvent(WarHealth health, int previousHealth, int previousMax, int newHealth, int newMax) {
        this.health = health;
        this.previousHealth = previousHealth;
        this.newHealth = newHealth;
        this.previousMax = previousMax;
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

    public int getHealthDifference() {
        return Math.abs(this.newHealth - previousHealth);
    }

    public int getMaxDifference() {
        return Math.abs(this.newMax - previousMax);
    }


    public boolean isZeroHealth() {
        return newHealth == 0 || newMax == 0;
    }
    public boolean maxHealthDecreased() {
        return previousMax < newMax;
    }

    public boolean healthIncreased() {
        return newHealth > previousHealth;
    }

    public boolean healthDecreased() {
        return previousHealth < newHealth;
    }
}