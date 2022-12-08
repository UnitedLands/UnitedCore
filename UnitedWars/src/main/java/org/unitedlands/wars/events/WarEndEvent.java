package org.unitedlands.wars.events;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.unitedlands.wars.war.War;
import org.unitedlands.wars.war.entities.WarringEntity;

public class WarEndEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final War war;
    private final WarringEntity winningEntity;
    private final WarringEntity losingEntity;

    public WarEndEvent(War war, WarringEntity winningEntity, WarringEntity losingEntity) {
        super(!Bukkit.getServer().isPrimaryThread());
        this.war = war;
        this.winningEntity = winningEntity;
        this.losingEntity = losingEntity;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public War getWar() {
        return war;
    }

    public WarringEntity getWinner() {
        return winningEntity;
    }

    public WarringEntity getLoser() {
        return losingEntity;
    }
}
