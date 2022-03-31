package org.unitedlands.brands.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.unitedlands.brands.InviteRequest;
import org.unitedlands.brands.UnitedBrands;
import org.unitedlands.brands.Util;
import org.unitedlands.brands.brewery.Brewery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.unitedlands.brands.Util.*;

public class BreweryCommand implements CommandExecutor {
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final UnitedBrands unitedBrands;
    private Player player;
    private int page;
    Set<InviteRequest> inviteRequests = new HashSet<>();

    public BreweryCommand(UnitedBrands unitedBrands) {
        this.unitedBrands = unitedBrands;
    }

    // TODO- Add tab completion

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {

        if (!(sender instanceof Player)) {
            return false;
        }

        player = (Player) sender;

        if (args.length < 1) {
            sendHelpMessage();
            return true;
        }

        switch (args[0]) {
            case "create" -> createBrewery(args);
            case "delete" -> deletePlayerBrewery();
            case "invite" -> {
                if (args.length == 1) {
                    player.sendMessage(getMessage("must-specify-invited-player"));
                    return true;
                }
                Player inviteReceiver = Bukkit.getPlayer(args[1]);
                invitePlayerToBrewery(inviteReceiver);
            }
            case "accept" -> acceptBreweryInvite();
            case "kick" -> {
                if (args.length == 1) {
                    player.sendMessage(getMessage("must-specify-kicked-player"));
                    return true;
                }
                kickPlayerFromBrewery(Bukkit.getPlayer(args[1]));
            }
            case "upgrade" -> {
                Brewery brewery = Util.getPlayerBrewery(player);
                upgradeBrewery(brewery);
            }
            case "leave" -> leaveBrewery();
            case "deny" -> denyRequest();
            case "slogan" -> {
                Brewery brewery = Util.getPlayerBrewery(player);
                String slogan = extractMultiWordString(args);
                changeBrewerySlogan(brewery, slogan);
            }
            case "list" -> {
                page = 0;
                if (args.length == 2) {
                    page = Integer.parseInt(args[1]);
                }
                // No negative pages
                if (page < 0) {
                    page = 0;
                }
                sendBreweriesList();
            }
            case "info" -> {
                Brewery brewery = getBreweryFromName(extractMultiWordString(args));
                // /brewery info. Returns the info for your brewery
                if (args.length == 1) {
                    brewery = getPlayerBrewery(player);
                }
                if (brewery == null) {
                    player.sendMessage(getMessage("brewery-does-not-exist"));
                    return true;
                }
                sendBreweryInfo(brewery);
            }
            default -> sendHelpMessage();
        }

        return true;
    }

    private void sendBreweryInfo(Brewery brewery) {

        if (brewery == null) {
            player.sendMessage(getMessage("brewery-does-not-exist"));
            return;
        }

        player.sendMessage(getMessage("info-header"));
        sendOwnerComponent(brewery);
        sendBreweryNameComponent(brewery);
        sendSloganComponent(brewery);
        sendMembersComponent(brewery);
        player.sendMessage(getMessage("footer"));
    }
    private void sendOwnerComponent(Brewery brewery) {
        player.sendMessage(Component
                .text("Owner: ", NamedTextColor.RED, TextDecoration.BOLD)
                .append(Component.text(brewery.getBreweryOwner().getName(), NamedTextColor.GRAY)
                        .decoration(TextDecoration.BOLD, false))
                .append(Component.newline()));
    }
    private void sendSloganComponent(Brewery brewery) {
        player.sendMessage(Component
                .text("Slogan: ", NamedTextColor.RED, TextDecoration.BOLD)
                .append(Component.text('"' + brewery.getBrewerySlogan() + '"', NamedTextColor.GRAY, TextDecoration.UNDERLINED)
                        .decoration(TextDecoration.BOLD, false))
                .append(Component.newline()));
    }
    private void sendBreweryNameComponent(Brewery brewery) {
        player.sendMessage(Component
                .text("Name: ", NamedTextColor.RED, TextDecoration.BOLD)
                .append(Component.text(brewery.getBreweryName(), NamedTextColor.GRAY)
                        .decoration(TextDecoration.BOLD, false))
                .append(Component.newline()));
    }
    private void sendMembersComponent(Brewery brewery) {
        Component membersPrefix = Component.text("Members: ", NamedTextColor.RED, TextDecoration.BOLD);
        ArrayList<String> memberNames = new ArrayList<>();
        for (String playerUUIDString : brewery.getBreweryMembers()) {
            UUID uuid = UUID.fromString(playerUUIDString);
            memberNames.add(Bukkit.getOfflinePlayer(uuid).getName());
        }
        if (!brewery.getBreweryMembers().isEmpty()) {
            String BreweryMembers = String.join("&7, &e", memberNames);
            Component membersList = LegacyComponentSerializer.legacyAmpersand()
                    .deserialize(BreweryMembers).decoration(TextDecoration.BOLD, false);
            player.sendMessage(membersPrefix
                    .append(membersList));
            return;
        }
        player.sendMessage(membersPrefix
                .append(Component.text("None", NamedTextColor.GRAY)
                .decoration(TextDecoration.BOLD, false)));
    }

    private void sendBreweriesList() {
        ArrayList<Brewery> breweries = Util.getAllBreweries();
        player.sendMessage(getMessage("list-header"));
        for (int i = 0; i < 10; i++) {
            int index = i + (9 * page);
            if (index >= breweries.size()) {
                player.sendMessage(getPreviousPageComponent().append(getPageComponent()));
                player.sendMessage(getMessage("footer"));
                return;
            }
            Brewery brewery = breweries.get(index);
            player.sendMessage(Component.text((index + 1) + ". ", NamedTextColor.RED)
                    .append(getBreweryComponent(brewery)));
        }

        player.sendMessage(
                getPreviousPageComponent()
                .append(getPageComponent())
                .append(getNextPageComponent()));
        player.sendMessage(getMessage("footer"));
    }
    private @NotNull Component getPageComponent() {
       return miniMessage.deserialize("<red>Page " + page);
    }
    private Component getPreviousPageComponent() {
        if (page == 0) {
            return Component.text("                               ");
        }

        return miniMessage.deserialize("                        <click:run_command:'/Brewery list "
                + (page - 1) +"'><b><gray><hover:show_text:'Previous Page'>« </hover></gray><b></click>   ");
    }
    private Component getNextPageComponent() {
        return miniMessage.deserialize("   <click:run_command:'/Brewery list " + page +"'><b><gray><hover:show_text:'Next Page'>»</hover></gray><b></click>");
    }
    private Component getBreweryComponent(Brewery brewery) {
        String BreweryName = brewery.getBreweryName();
        return miniMessage.deserialize("<hover:show_text:'Click for more info!'><gold>"+
                BreweryName +" - " + brewery.getBreweryMembers().size() +
                "</gold></hover>").clickEvent(ClickEvent.runCommand("/brewery info " + BreweryName));
    }

    private void sendHelpMessage() {
        List<String> helpMessage = unitedBrands.getConfig().getStringList("messages.help-command");
        for (String message : helpMessage) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
        }
    }

    private void upgradeBrewery(Brewery brewery) {
        if (brewery == null || !isBreweryOwner(brewery)) {
            player.sendMessage(getMessage("must-own-brewery"));
            return;
        }
        int level = brewery.getBreweryLevel();
        if (level == 5) {
            player.sendMessage(getMessage("max-brewery-level"));
            return;
        }
        brewery.increaseLevel();
        player.sendMessage(getMessage("brewery-upgraded", brewery.getBreweryName())
                .replace("<level>", Integer.toString(level)));
    }

    private void createBrewery(String[] args) {
        if (args.length == 1) {
            player.sendMessage(getMessage("must-specify-brewery-name", ""));
            return;
        }

        String BreweryName = String.join(" ", extractMultiWordString(args));
        Brewery brewery = new Brewery(unitedBrands, BreweryName, player, null);

        if (Util.hasBrewery(player)) {
            player.sendMessage(getMessage("in-a-brewery", Util.getPlayerBrewery(player).getBreweryName()));
            return;
        }

        if (Util.breweryExists(brewery)) {
            player.sendMessage(getMessage("brewery-already-exists", BreweryName));
            return;
        }
        brewery.createBrewery();
    }

    private void deletePlayerBrewery() {
        Brewery brewery = Util.getPlayerBrewery(player);

        if (brewery == null) {
            player.sendMessage(getMessage("must-own-brewery"));
            return;
        }

        String BreweryName = brewery.getBreweryName();

        if (Util.hasBrewery(player) && isBreweryOwner(brewery)) {
            brewery.deleteBrewery();
            player.sendMessage(getMessage("brewery-deleted", BreweryName));
            return;
        }

        player.sendMessage(getMessage("brewery-cannot-be-deleted", BreweryName));
    }

    private void invitePlayerToBrewery(Player inviteReceiver) {

        if (inviteReceiver == null) {
            player.sendMessage(getMessage("invalid-player"));
            return;
        }

        Brewery brewery = Util.getPlayerBrewery(player);

        if (brewery == null) {
            player.sendMessage(getMessage("must-own-brewery"));
            return;
        }

        String BreweryName = brewery.getBreweryName();

        if (player == inviteReceiver) {
            player.sendMessage(getMessage("cannot-invite-self", BreweryName));
            return;
        }

        if (isBreweryOwner(brewery)) {
            InviteRequest inviteRequest = new InviteRequest(player, inviteReceiver);
            inviteRequests.add(inviteRequest);
            player.sendMessage(getMessage("player-invited", BreweryName));
            inviteReceiver.sendMessage(getMessage("brewery-invite", BreweryName));
            return;
        }

        player.sendMessage(getMessage("must-own-brewery"));

    }

    private void acceptBreweryInvite() {
        InviteRequest request = getRequest(player);

        if (request == null) {
            player.sendMessage(getMessage("no-requests"));
            return;
        }

        Player receiver = request.getReceiver();
        Player sender = request.getSender();

        if (Util.getPlayerBrewery(receiver) != null) {
            player.sendMessage(getMessage("already-in-a-brewery"));
            return;
        }

        Brewery brewery = Util.getPlayerBrewery(sender);
        String BreweryName = brewery.getBreweryName();
        brewery.addMemberToBrewery(player);

        sender.sendMessage(getMessage("brewery-join-sender", player));
        receiver.sendMessage(getMessage("brewery-join", BreweryName));

        inviteRequests.remove(request);
    }

    private void kickPlayerFromBrewery(Player kickedPlayer) {

        if (kickedPlayer == null) {
            player.sendMessage(getMessage("must-specify-kicked-player", ""));
            return;
        }

        Brewery brewery = Util.getPlayerBrewery(player);
        String BreweryName = brewery.getBreweryName();

        if (!isBreweryOwner(brewery)) {
            player.sendMessage(getMessage("must-own-brewery", BreweryName));
            return;
        }

        if (kickedPlayer == player) {
            player.sendMessage(getMessage("cannot-kick-self", BreweryName));
            return;
        }

        brewery.removeMemberFromBrewery(kickedPlayer);
        kickedPlayer.sendMessage(getMessage("kicked-from-brewery", BreweryName));
        player.sendMessage(getMessage("player-kicked", BreweryName));
    }

    private void leaveBrewery() {
        Brewery brewery = Util.getPlayerBrewery(player);
        if (brewery == null) {
            player.sendMessage(getMessage("must-have-brewery", ""));
            return;
        }

        String BreweryName = brewery.getBreweryName();

        if (isBreweryOwner(brewery)) {
            player.sendMessage(getMessage("must-delete-brewery", BreweryName));
            return;
        }

        if (Util.hasBrewery(player)) {
            OfflinePlayer BreweryOwner = brewery.getBreweryOwner();
            brewery.removeMemberFromBrewery(player);
            player.sendMessage(getMessage("brewery-leave", BreweryName));
            if (BreweryOwner.isOnline()) {
                BreweryOwner.getPlayer().sendMessage(getMessage("player-left-brewery", BreweryName, player));
            }
        }

    }

    private void denyRequest() {
        InviteRequest request = getRequest(player);
        String BreweryName = Util.getPlayerBrewery(player).getBreweryName();

        if (request != null) {
            request.getSender().sendMessage(getMessage("Brewery-deny-sender", player));
            request.getReceiver().sendMessage(getMessage("Brewery-deny", BreweryName));
            inviteRequests.remove(request);
            return;
        }
        player.sendMessage(getMessage("no-requests", ""));
    }

    private void changeBrewerySlogan(Brewery brewery, String slogan) {
        if (brewery == null) {
            player.sendMessage(getMessage("must-have-brewery"));
            return;
        }

        if (!isBreweryOwner(brewery)) {
            player.sendMessage(getMessage("must-own-brewery"));
            return;
        }

        if (slogan == null) {
            player.sendMessage(getMessage("must-specify-slogan"));
        }

        brewery.setSlogan(slogan);
        player.sendMessage(getMessage("slogan-changed", brewery.getBreweryName()));
    }

    private boolean isBreweryOwner(Brewery brewery) {
        if (Util.hasBrewery(player)) {
            return brewery.getBreweryOwner().equals(player);
        }
        return false;
    }

    private InviteRequest getRequest(Player receiver) {
        for (InviteRequest request : inviteRequests) {
            if (request.getReceiver().equals(receiver)) {
                return request;
            }
        }
        return null;
    }

    private String extractMultiWordString(String[] args) {
        String[] arrayWithoutFirstArgument = Arrays.copyOfRange(args, 1, args.length);
        return String.join(" ", arrayWithoutFirstArgument);
    }

}
