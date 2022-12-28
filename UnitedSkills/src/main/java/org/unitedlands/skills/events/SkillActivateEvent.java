package org.unitedlands.skills.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.unitedlands.skills.skill.ActiveSkill;

public class SkillActivateEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final ActiveSkill skill;
    private boolean canceled = false;

    public SkillActivateEvent(Player player, ActiveSkill skill) {
        this.player = player;
        this.skill = skill;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public ActiveSkill getSkill() {
        return skill;
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public boolean isCancelled() {
        return canceled;
    }

    @Override
    public void setCancelled(boolean b) {
        this.canceled = b;
    }
}
