package org.unitedlands.brands;

import org.bukkit.entity.Player;

public record InviteRequest(Player sender, Player receiver) {
}
