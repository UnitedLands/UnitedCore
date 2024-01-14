package org.unitedlands.brands.commands;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.confirmations.Confirmation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.economy.Account;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.unitedlands.brands.BreweryDatabase;
import org.unitedlands.brands.InviteRequest;
import org.unitedlands.brands.UnitedBrands;
import org.unitedlands.brands.brewery.Brewery;

import java.util.*;

import static org.unitedlands.brands.BreweryDatabase.getPlayerBrewery;
import static org.unitedlands.brands.Util.getMessage;
import static org.unitedlands.brands.Util.getNoPrefixMessage;

public class BreweryCommand implements TabExecutor {
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private static final UnitedBrands PLUGIN = UnitedBrands.getInstance();
    Set<InviteRequest> inviteRequests = new HashSet<>();
    private Brewery brewery;
    private Player player;
    private int page;
    private static final List<String> BREWERY_TAB_COMPLETES = Arrays.asList("help", "list", "create", "slogan", "upgrade", "delete", "invite", "kick", "accept", "deny", "leave");

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        return args.length == 1 ? BREWERY_TAB_COMPLETES : Collections.emptyList();
    }

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

        switch (args[0].toLowerCase()) {
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
                brewery = getPlayerBrewery(player);
                upgradeBrewery();
            }
            case "leave" -> leaveBrewery();
            case "deny" -> denyRequest();
            case "slogan" -> {
                brewery = getPlayerBrewery(player);
                String slogan = extractMultiWordString(args);
                changeBrewerySlogan(slogan);
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
                brewery = BreweryDatabase.getBreweryFromName(extractMultiWordString(args));
                // /brewery info. Returns the info for your brewery
                if (args.length == 1) {
                    brewery = getPlayerBrewery(player);
                }
                if (brewery == null) {
                    player.sendMessage(getMessage("brewery-does-not-exist"));
                    return true;
                }
                sendBreweryInfo();
            }
            default -> sendHelpMessage();
        }

        return true;
    }

    private void sendHelpMessage() {
        List<String> helpMessage = PLUGIN.getConfig().getStringList("messages.help-command");
        MiniMessage mm = MiniMessage.miniMessage();
        for (String message : helpMessage) {
            player.sendMessage(mm.deserialize(message));
        }
    }

    private void sendBreweryInfo() {
        if (brewery == null) {
            player.sendMessage(getMessage("brewery-does-not-exist"));
            return;
        }
        player.sendMessage(getNoPrefixMessage("info-header"));
        sendOwnerComponent();
        sendBreweryNameComponent();
        sendSloganComponent();
        sendStatComponent("level");
        sendStatComponent("average-stars");
        sendStatComponent("brews-made");
        sendStatComponent("brews-drunk");
        sendMembersComponent();
        player.sendMessage(getNoPrefixMessage("footer"));
    }

    private void sendOwnerComponent() {
        player.sendMessage(Component
                .text("Owner: ", NamedTextColor.RED, TextDecoration.BOLD)
                .append(Component.text(brewery.getOwner().getName(), NamedTextColor.GRAY)
                        .decoration(TextDecoration.BOLD, false))
                .append(Component.newline()));
    }

    private void sendStatComponent(String statName) {
        String title = WordUtils.capitalize(statName.replace("-", " ")) + ": ";
        player.sendMessage(Component
                .text(title, NamedTextColor.RED, TextDecoration.BOLD)
                .append(Component.text(brewery.getBreweryStat(statName), NamedTextColor.GRAY)
                        .decoration(TextDecoration.BOLD, false))
                .append(Component.newline()));
    }

    private void sendSloganComponent() {
        String slogan = brewery.getSlogan();
        if (slogan == null) {
            slogan = "None";
        }
        player.sendMessage(Component
                .text("Slogan: ", NamedTextColor.RED, TextDecoration.BOLD)
                .append(Component.text('"' + slogan + '"', NamedTextColor.GRAY, TextDecoration.UNDERLINED)
                        .decoration(TextDecoration.BOLD, false))
                .append(Component.newline()));
    }

    private void sendBreweryNameComponent() {
        player.sendMessage(Component
                .text("Name: ", NamedTextColor.RED, TextDecoration.BOLD)
                .append(Component.text(brewery.getName(), NamedTextColor.GRAY)
                        .decoration(TextDecoration.BOLD, false))
                .append(Component.newline()));
    }

    private void sendMembersComponent() {
        Component membersPrefix = Component.text("Members: ", NamedTextColor.RED, TextDecoration.BOLD);
        ArrayList<String> memberNames = new ArrayList<>();
        for (String playerUUIDString : brewery.getMembers()) {
            UUID uuid = UUID.fromString(playerUUIDString);
            memberNames.add(Bukkit.getOfflinePlayer(uuid).getName());
        }
        if (!brewery.getMembers().isEmpty()) {
            String BreweryMembers = String.join("&7, &e", memberNames);
            Component membersList = LegacyComponentSerializer.legacyAmpersand()
                    .deserialize(BreweryMembers).decoration(TextDecoration.BOLD, false);
            player.sendMessage(membersPrefix.append(membersList));
            return;
        }
        player.sendMessage(membersPrefix
                .append(Component.text("None", NamedTextColor.GRAY)
                        .decoration(TextDecoration.BOLD, false)));
    }

    private void sendBreweriesList() {
        ArrayList<Brewery> breweries = getAllBreweries();
        player.sendMessage(getNoPrefixMessage("list-header"));
        for (int i = 0; i < 10; i++) {
            int index = i + (9 * page);
            if (index >= breweries.size()) {
                player.sendMessage(getPreviousPageComponent().append(getPageComponent()));
                player.sendMessage(getNoPrefixMessage("footer"));
                return;
            }
            brewery = breweries.get(index);
            player.sendMessage(Component.text((index + 1) + ". ", NamedTextColor.RED)
                    .append(getBreweryComponent()));
        }

        player.sendMessage(
                getPreviousPageComponent()
                        .append(getPageComponent())
                        .append(getNextPageComponent()));
        player.sendMessage(getNoPrefixMessage("footer"));
    }

    private ArrayList<Brewery> getAllBreweries() {
        return new ArrayList<>(BreweryDatabase.getBreweries());
    }

    private @NotNull Component getPageComponent() {
        return miniMessage.deserialize("<red>Page " + page);
    }

    private Component getPreviousPageComponent() {
        if (page == 0) {
            return Component.text("                               ");
        }

        return miniMessage.deserialize("                        <click:run_command:'/Brewery list "
                + (page - 1) + "'><b><gray><hover:show_text:'Previous Page'>« </hover></gray><b></click>   ");
    }

    private Component getNextPageComponent() {
        return miniMessage.deserialize("   <click:run_command:'/brewery list " + page + "'><b><gray><hover:show_text:'Next Page'>»</hover></gray><b></click>");
    }

    private Component getBreweryComponent() {
        String name = brewery.getName();
        return miniMessage.deserialize("<hover:show_text:'Click for more info!'><gold>" +
                name + " - " + (brewery.getMembers().size() + 1) +
                "</gold></hover>").clickEvent(ClickEvent.runCommand("/brewery info " + name));
    }

    private void upgradeBrewery() {
        if (brewery == null || !isBreweryOwner()) {
            player.sendMessage(getMessage("must-own-brewery"));
            return;
        }

        int level = brewery.getBreweryStat("level");
        if (level == 5) {
            player.sendMessage(getMessage("max-brewery-level"));
            return;
        }
        int price = (level + 1) * 15_000;
        Resident resident = TownyAPI.getInstance().getResident(player);
        Account account = resident.getAccount();
        if (!account.canPayFromHoldings(price)) {
            player.sendMessage(getMessage("not-enough-to-upgrade").replaceText(TextReplacementConfig.builder()
                    .match("<amount>")
                    .replacement(String.valueOf(price))
                    .build()));
            return;
        }
        Confirmation.runOnAccept(() -> {
            brewery.increaseStat("level", 1);
            player.sendMessage(getMessage("brewery-upgraded", brewery.getName())
                    .replaceText(TextReplacementConfig
                            .builder()
                            .match("<level>")
                            .replacement(String.valueOf(level + 1))
                            .build()));
            account.withdraw(price, "upgraded brewery");
        }).setTitle("§cAre you sure you want to upgrade your brewery to level " + (level + 1) + "? This upgrade will cost you §6" + price + " Gold!")
                .sendTo(player);
    }

    private void createBrewery(String[] args) {
        if (args.length == 1) {
            player.sendMessage(getMessage("must-specify-brewery-name"));
            return;
        }

        String breweryName = String.join(" ", extractMultiWordString(args));

        if (BreweryDatabase.isInBrewery(player)) {
            player.sendMessage(getMessage("in-a-brewery", getPlayerBrewery(player).getName()));
            return;
        }

        if (BreweryDatabase.hasBrewery(breweryName)) {
            player.sendMessage(getMessage("brewery-already-exists", breweryName));
            return;
        }
        Resident resident = TownyAPI.getInstance().getResident(player.getUniqueId());
        if (resident.getAccount().canPayFromHoldings(10_000)) {
            Confirmation.runOnAccept(() -> {
                BreweryDatabase.createBrewery(breweryName, player);
                resident.getAccount().withdraw(10_000, "Created Brewery");
            }
            ).setTitle("§cAre you sure you want to create a new §ebrewery§c? This will cost you §610,000 Gold!").sendTo(player);
        } else {
            player.sendMessage(getMessage("not-enough-money"));
        }
    }

    private void deletePlayerBrewery() {
        brewery = getPlayerBrewery(player);

        if (brewery == null) {
            player.sendMessage(getMessage("must-own-brewery"));
            return;
        }

        if (!(BreweryDatabase.isInBrewery(player) && isBreweryOwner())) {
            player.sendMessage(getMessage("brewery-cannot-be-deleted", brewery.getName()));
            return;
        }
        Confirmation.runOnAccept(() -> {
            BreweryDatabase.delete(brewery);
            player.sendMessage(getMessage("brewery-deleted", brewery.getName()));
        }).setTitle("§cAre you sure you want to delete §e" + brewery.getName() + "§c? This action is irreversible!")
                .sendTo(player);
    }

    private void invitePlayerToBrewery(Player inviteReceiver) {

        if (inviteReceiver == null) {
            player.sendMessage(getMessage("invalid-player"));
            return;
        }

        brewery = getPlayerBrewery(player);

        if (brewery == null) {
            player.sendMessage(getMessage("must-own-brewery"));
            return;
        }

        String breweryName = brewery.getName();

        if (player == inviteReceiver) {
            player.sendMessage(getMessage("cannot-invite-self", breweryName));
            return;
        }

        if (!isBreweryOwner()) {
            player.sendMessage(getMessage("must-own-brewery"));
            return;
        }
        if(getPlayerBrewery(inviteReceiver) != null) {
            player.sendMessage(getMessage("player-in-brewery", inviteReceiver));
            return;
        }

        InviteRequest inviteRequest = new InviteRequest(player, inviteReceiver);
        inviteRequests.add(inviteRequest);
        player.sendMessage(getMessage("player-invited", breweryName));
        inviteReceiver.sendMessage(getMessage("brewery-invite", breweryName));

    }

    private void acceptBreweryInvite() {
        InviteRequest request = getRequest(player);

        if (request == null) {
            player.sendMessage(getMessage("no-requests"));
            return;
        }

        Player receiver = request.receiver();
        Player sender = request.sender();

        if (getPlayerBrewery(receiver) != null) {
            player.sendMessage(getMessage("already-in-a-brewery"));
            return;
        }

        brewery = getPlayerBrewery(sender);
        String breweryName = brewery.getName();
        brewery.addMember(player);

        sender.sendMessage(getMessage("brewery-join-sender", player));
        receiver.sendMessage(getMessage("brewery-join", breweryName));

        inviteRequests.remove(request);
    }

    private void kickPlayerFromBrewery(Player kickedPlayer) {
        if (kickedPlayer == null) {
            player.sendMessage(getMessage("must-specify-kicked-player", ""));
            return;
        }

        brewery = getPlayerBrewery(player);
        String breweryName = brewery.getName();

        if (!isBreweryOwner()) {
            player.sendMessage(getMessage("must-own-brewery", breweryName));
            return;
        }

        if (kickedPlayer == player) {
            player.sendMessage(getMessage("cannot-kick-self", breweryName));
            return;
        }

        brewery.removeMember(kickedPlayer);
        kickedPlayer.sendMessage(getMessage("kicked-from-brewery", breweryName));
        player.sendMessage(getMessage("player-kicked", breweryName));
    }

    private void leaveBrewery() {
        brewery = getPlayerBrewery(player);
        if (brewery == null) {
            player.sendMessage(getMessage("must-have-brewery", ""));
            return;
        }

        String breweryName = brewery.getName();

        if (isBreweryOwner()) {
            player.sendMessage(getMessage("must-delete-brewery", breweryName));
            return;
        }

        if (BreweryDatabase.isInBrewery(player)) {
            OfflinePlayer BreweryOwner = brewery.getOwner();
            brewery.removeMember(player);
            player.sendMessage(getMessage("brewery-leave", breweryName));
            if (BreweryOwner.isOnline()) {
                BreweryOwner.getPlayer().sendMessage(getMessage("player-left-brewery", breweryName, player));
            }
        }

    }

    private void denyRequest() {
        InviteRequest request = getRequest(player);
        String breweryName = getPlayerBrewery(player).getName();

        if (request != null) {
            request.sender().sendMessage(getMessage("Brewery-deny-sender", player));
            request.receiver().sendMessage(getMessage("Brewery-deny", breweryName));
            inviteRequests.remove(request);
            return;
        }
        player.sendMessage(getMessage("no-requests", ""));
    }

    private void changeBrewerySlogan(String slogan) {
        if (brewery == null) {
            player.sendMessage(getMessage("must-have-brewery"));
            return;
        }

        if (!isBreweryOwner()) {
            player.sendMessage(getMessage("must-own-brewery"));
            return;
        }

        if (slogan == null) {
            player.sendMessage(getMessage("must-specify-slogan"));
        }

        brewery.setSlogan(slogan);
        player.sendMessage(getMessage("slogan-changed", brewery.getName()));
    }

    private boolean isBreweryOwner() {
        if (BreweryDatabase.isInBrewery(player)) {
            return brewery.getOwner().equals(player);
        }
        return false;
    }

    private InviteRequest getRequest(Player receiver) {
        for (InviteRequest request : inviteRequests) {
            if (request.receiver().equals(receiver)) {
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
