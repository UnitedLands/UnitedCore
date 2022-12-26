package org.unitedlands.wars.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.unitedlands.wars.books.data.Declarer;
import org.unitedlands.wars.books.data.WarTarget;
import org.unitedlands.wars.books.declaration.DeclarationWarBook;
import org.unitedlands.wars.war.WarDatabase;
import org.unitedlands.wars.war.entities.WarringEntity;

public class WarDeclareEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final DeclarationWarBook declarationWarBook;
    private final Declarer declarer;
    private final WarTarget target;
    public WarDeclareEvent(DeclarationWarBook declarationWarBook) {
        this.declarationWarBook = declarationWarBook;
        this.declarer = declarationWarBook.getDeclarer();
        this.target = declarationWarBook.getWarTarget();
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public DeclarationWarBook getDeclarationWarBook() {
        return declarationWarBook;
    }

    public WarTarget getTarget() {
        return target;
    }

    public Declarer getDeclarer() {
        return declarer;
    }

    public WarringEntity getDeclaringEntity() {
        return WarDatabase.getWarringEntity(declarer.player());
    }

    public WarringEntity getTargetEntity() {
        return WarDatabase.getWarringEntity(target.targetMayor().getUniqueId());
    }
}
