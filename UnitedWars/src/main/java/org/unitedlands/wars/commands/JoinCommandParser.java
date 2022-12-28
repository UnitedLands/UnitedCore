package org.unitedlands.wars.commands;

import com.palmergames.bukkit.towny.confirmations.Confirmation;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.unitedlands.wars.UnitedWars;
import org.unitedlands.wars.war.WarDatabase;
import org.unitedlands.wars.war.entities.WarringNation;

import static org.unitedlands.wars.Utils.*;
import static org.unitedlands.wars.war.WarDatabase.hasWar;

public class JoinCommandParser {
    private final CommandSender sender;
    private final String target;

    public JoinCommandParser(CommandSender sender, String target) {
        this.sender = sender;
        this.target = target;
    }

    public void parse() {
        Player player = (Player) sender;
        Resident resident = getTownyResident(player);
        if (!resident.hasTown()) {
            player.sendMessage(getMessage("must-have-town"));
            return;
        }
        if (!resident.hasNation()) {
            player.sendMessage(getMessage("must-have-nation"));
            return;
        }
        Nation playerNation = resident.getNationOrNull();
        if (playerNation == null)
            return;
        Nation targetNation = UnitedWars.TOWNY_API.getNation(target);
        if (targetNation == null) {
            player.sendMessage(getMessage("invalid-nation"));
            return;
        }
        if (!playerNation.hasAlly(targetNation)) {
            player.sendMessage(getMessage("not-an-ally"));
            return;
        }
        if (!WarDatabase.hasNation(targetNation)) {
            player.sendMessage(getMessage("ally-has-no-war"));
            return;
        }
        if (playerNation.isNeutral()) {
            player.sendMessage(getMessage("must-not-be-neutral"));
            return;
        }
        if (hasWar(playerNation.getCapital())) {
            player.sendMessage(getMessage("must-not-have-war"));
            return;
        }
        Confirmation.runOnAccept(() -> {
            WarringNation warringNation = WarDatabase.getWarringNation(targetNation);
            if (warringNation == null)
                return;
            // Add the ally to the war.
            warringNation.addAlly(playerNation);
        }).setTitle(getMessageRaw("join-war-as-ally")
                .replaceAll("<nation>", targetNation.getFormattedName()))
                .sendTo(sender);
    }
}
