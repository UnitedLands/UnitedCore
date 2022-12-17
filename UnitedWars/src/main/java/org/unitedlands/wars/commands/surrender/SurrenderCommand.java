package org.unitedlands.wars.commands.surrender;

import com.palmergames.bukkit.towny.command.BaseCommand;
import com.palmergames.bukkit.towny.confirmations.Confirmation;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.unitedlands.wars.UnitedWars;
import org.unitedlands.wars.Utils;
import org.unitedlands.wars.war.War;
import org.unitedlands.wars.war.WarDatabase;
import org.unitedlands.wars.war.WarType;
import org.unitedlands.wars.war.entities.WarringEntity;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static net.kyori.adventure.text.Component.text;
import static org.unitedlands.wars.Utils.*;

public class SurrenderCommand implements TabExecutor {
    private static final List<String> surrenderOptions = Arrays.asList("accept", "money", "town", "whitepeace", "none");

    private static final HashSet<SurrenderRequest> SURRENDER_REQUESTS = new HashSet<>();
    private Player player;

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args[0].equals("town")) {
            return BaseCommand.getTownyStartingWith(args[1], "t");
        }
        return args.length == 1 ? surrenderOptions : Collections.emptyList();
    }
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(text("Must be player to use this command!").color(NamedTextColor.RED));
            return true;
        }
        player = (Player) sender;
        if (args.length == 0) {
            getMessageList("surrender-help").forEach(player::sendMessage);
            return true;
        }
        Resident resident = getTownyResident(player);
        if (!resident.hasTown()) {
            player.sendMessage(getMessage("must-have-town"));
            return true;
        }
        if (!resident.isMayor()) {
            player.sendMessage(getMessage("must-be-mayor"));
            return true;
        }
        if (!WarDatabase.hasWar(player)) {
            player.sendMessage(getMessage("no-wars-found"));
            return true;
        }
        WarringEntity enemy = WarDatabase.getWarringEntity(player).getEnemy();
        Resident leader = enemy.getLeader();
        if (!leader.isOnline()) {
            player.sendMessage(getMessage("mayor-offline",
                    Placeholder.component("name", text(leader.getName())),
                    Placeholder.component("warring-name", text(enemy.name()))));
            return true;
        }
        switch (args[0]) {
            case "accept" -> acceptPendingRequests();
            case "money" -> parseSurrenderWithMoney(args);
            case "none" -> parseSurrender();
            case "town" -> parseSurrenderWithTown(args);
            case "whitepeace" -> parseWhitePeace();
            default -> getMessageList("surrender-help").forEach(player::sendMessage);
        }
        return false;
    }

    private void acceptPendingRequests() {
        SurrenderRequest request = getPendingRequest(player);
        if (request == null) {
            player.sendMessage(getMessage("no-pending-requests"));
            return;
        }
        Confirmation
                .runOnAccept(() -> {
                    request.accept();
                    War war = WarDatabase.getWar(player);
                    WarringEntity winner = WarDatabase.getWarringEntity(player);
                    war.surrenderWar(winner, winner.getEnemy());
                    SURRENDER_REQUESTS.remove(request);
                })
                .setTitle(request.getTitle())
                .sendTo(player);

    }

    private void parseSurrender() {
        Confirmation.runOnAccept(() -> {
            War war = WarDatabase.getWar(player);
            WarringEntity loser = WarDatabase.getWarringEntity(player);
            if (loser.getWarHealth().getValue() <= 65) {
                player.sendMessage(getMessage("health-too-high-to-surrender"));
                return;
            }
            war.endWar(loser.getEnemy(), loser);
                }).setTitle("§cAre you sure you want to surrender without an offer? Confirming will end the war immediately.")
                .sendTo(player);
    }

    private void parseSurrenderWithMoney(@NotNull String[] args) {
        if (args.length < 2) {
            player.sendMessage(getMessage("not-enough-arguments", Placeholder.component("args", text("the amount of money!"))));
            return;
        }

        int amount = Integer.parseInt(args[1]);
        Resident resident = getTownyResident(player);
        double currentBalance = resident.getAccount().getHoldingBalance();
        if (amount > currentBalance) {
            player.sendMessage(getMessage("not-enough-money"));
            return;
        }
        WarringEntity enemy = WarDatabase.getWarringEntity(player).getEnemy();
        Resident leader = enemy.getLeader();

        Confirmation.runOnAccept(() -> {
            removeOldRequests();
            SurrenderRequest request = new SurrenderRequest(player.getUniqueId(), leader.getUUID(), SurrenderRequest.SurrenderType.MONEY, amount);
            SURRENDER_REQUESTS.add(request);
            player.sendMessage(getMessage("request-sent"));
        }).setTitle(getSurrenderTitle(enemy))
                .sendTo(player);
    }

    private void removeOldRequests() {
        SurrenderRequest sent = getSentRequest(player);
        if (sent != null)
            SURRENDER_REQUESTS.remove(sent);
    }

    private void parseSurrenderWithTown(@NotNull String[] args) {
        if (args.length < 2) {
            player.sendMessage(getMessage("not-enough-arguments", Placeholder.component("args", text("the town to offer!"))));
            return;
        }
        War war = WarDatabase.getWar(player);
        if (war.getWarType() != WarType.NATIONWAR) {
            player.sendMessage(getMessage("must-be-nationwar"));
            return;
        }
        Town town = UnitedWars.TOWNY_API.getTown(args[1]);
        if (town == null) {
            player.sendMessage(getMessage("invalid-town-name"));
            return;
        }
        Nation nation = getTownyResident(player).getNationOrNull();
        if (!town.getNationOrNull().equals(nation)) {
            player.sendMessage(getMessage("town-not-in-nation"));
            return;
        }

        WarringEntity enemy = WarDatabase.getWarringEntity(player).getEnemy();
        Resident leader = enemy.getLeader();

        Confirmation.runOnAccept(() -> {
            removeOldRequests();
            SurrenderRequest request = new SurrenderRequest(player.getUniqueId(), leader.getUUID(), SurrenderRequest.SurrenderType.TOWN, town);
            SURRENDER_REQUESTS.add(request);
            player.sendMessage(getMessage("request-sent"));
        }).setTitle(getSurrenderTitle(enemy)).sendTo(player);
    }

    private void parseWhitePeace() {
        WarringEntity enemy = WarDatabase.getWarringEntity(player).getEnemy();
        Resident leader = enemy.getLeader();

        Confirmation.runOnAccept(() -> {
            removeOldRequests();
            SurrenderRequest request = new SurrenderRequest(player.getUniqueId(), leader.getUUID(), SurrenderRequest.SurrenderType.WHITEPEACE);
            SURRENDER_REQUESTS.add(request);
            player.sendMessage(getMessage("request-sent"));
        }).setTitle(getSurrenderTitle(enemy)).sendTo(player);
    }

    @NotNull
    private String getSurrenderTitle(WarringEntity enemy) {
        return getMessageRaw("surrender-confirm").replace("<warring-name>", enemy.name());
    }

    private SurrenderRequest getPendingRequest(Player player) {
        for (SurrenderRequest request : SURRENDER_REQUESTS) {
            if (request.getTarget().getUniqueId().equals(player.getUniqueId()))
                return request;
        }
        return null;
    }

    private SurrenderRequest getSentRequest(Player player) {
        for (SurrenderRequest request : SURRENDER_REQUESTS) {
            if (request.getRequester().getUniqueId().equals(player.getUniqueId())) {
                return request;
            }
        }
        return null;
    }

}
