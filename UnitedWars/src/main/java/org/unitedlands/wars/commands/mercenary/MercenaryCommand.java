package org.unitedlands.wars.commands.mercenary;

import com.palmergames.bukkit.towny.confirmations.Confirmation;
import com.palmergames.bukkit.towny.object.Resident;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.unitedlands.wars.Utils;
import org.unitedlands.wars.war.WarDatabase;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.unitedlands.wars.Utils.getMessage;
import static org.unitedlands.wars.Utils.getMessageList;

public class MercenaryCommand implements TabExecutor {
    private static final List<String> MERCENARY_TAB_COMPLETES = List.of("hire", "accept");
    private final HashSet<MercenaryRequest> outgoingRequests = new HashSet<>();
    private Player player;

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        return args.length == 1 ? MERCENARY_TAB_COMPLETES : Collections.emptyList();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!(commandSender instanceof Player))
            return true;

        player = (Player) commandSender;
        switch (args[0]) {
            case "hire" -> parseHireCommand(player, args);
            case "accept" -> parseAcceptCommand(player);
            default ->  getMessageList("mercenary-help").forEach(player::sendMessage);
        }
        return false;
    }

    private void parseAcceptCommand(Player player) {
        MercenaryRequest request = getRequest(player);
        if (request == null) {
            player.sendMessage(getMessage("no-pending-requests-mercenary"));
            return;
        }
        Confirmation.runOnAccept(() -> {
                    request.accept();
                    outgoingRequests.remove(request);
        }).setTitle("§cAre you sure you want to accept §e" + request.getRequester().getName() + "'s§c offer to join the war as a mercenary?").sendTo(player);
    }

    private void parseHireCommand(Player requester, String[] args) {
        if (!WarDatabase.hasWar(requester)) {
            requester.sendMessage(getMessage("no-wars-found"));
            return;
        }
        Resident resident = Utils.getTownyResident(requester);
        if (!resident.isMayor()) {
            requester.sendMessage(getMessage("must-be-mayor"));
            return;
        }

        Resident mercenary = Utils.getTownyResident(Bukkit.getPlayer(args[1]));
        if (!mercenary.isOnline()) {
            requester.sendMessage(getMessage("mercenary-not-online"));
            return;
        }
        if (WarDatabase.hasWar(mercenary.getPlayer())) {
            requester.sendMessage(getMessage("mercenary-in-war"));
            return;
        }
        if (args.length == 2) {
            requester.sendMessage(getMessage("must-specify-money"));
            return;
        }
        int money = Integer.parseInt(args[2]);
        double currentBalance = resident.getAccount().getHoldingBalance();
        if (money > currentBalance) {
            requester.sendMessage(getMessage("not-enough-money"));
            return;
        }
        Confirmation.runOnAccept(() -> {
                    removeOldRequests();
                    MercenaryRequest request = new MercenaryRequest(requester.getUniqueId(), mercenary.getUUID(), money);
                    outgoingRequests.add(request);
                    requester.sendMessage(getMessage("mercenary-request-sent"));
        }).setTitle("§cAre you sure you want to send a mercenary offer to §e" + mercenary.getFormattedName() + "§c?")
                .sendTo(requester);
    }

    private void removeOldRequests() {
        MercenaryRequest sent = getSentRequest(player);
        if (sent != null)
            outgoingRequests.remove(sent);
    }


    private MercenaryRequest getRequest(Player player) {
        for (MercenaryRequest request: outgoingRequests) {
            if (request.getTarget().getUniqueId().equals(player.getUniqueId()))
                return request;
        }
        return null;
    }

    private MercenaryRequest getSentRequest(Player player) {
        for (MercenaryRequest request : outgoingRequests) {
            if (request.getRequester().getUniqueId().equals(player.getUniqueId()))
                return request;
        }
        return null;
    }
}
