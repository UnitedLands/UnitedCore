package org.unitedlands.alcohol;

import org.bukkit.entity.Player;

public class InviteRequest {
    private final Player sender;
    private final Player receiver;

    public InviteRequest(Player sender, Player receiver) {
        this.sender = sender;
        this.receiver = receiver;
    }

    public Player getReceiver() {
        return receiver;
    }

    public Player getSender() {
        return sender;
    }
}
