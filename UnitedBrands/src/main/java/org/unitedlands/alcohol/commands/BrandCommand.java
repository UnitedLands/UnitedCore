package org.unitedlands.alcohol.commands;

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
import org.unitedlands.alcohol.InviteRequest;
import org.unitedlands.alcohol.UnitedBrands;
import org.unitedlands.alcohol.Util;
import org.unitedlands.alcohol.brand.Brand;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.unitedlands.alcohol.Util.*;

public class BrandCommand implements CommandExecutor {
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final UnitedBrands unitedBrands;
    private Player player;
    private int page;
    Set<InviteRequest> inviteRequests = new HashSet<>();

    public BrandCommand(UnitedBrands unitedBrands) {
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
            case "create" -> createBrand(args);
            case "delete" -> deletePlayerBrand();
            case "invite" -> {
                if (args.length == 1) {
                    player.sendMessage(getMessage("must-specify-invited-player"));
                    return true;
                }
                Player inviteReceiver = Bukkit.getPlayer(args[1]);
                invitePlayerToBrand(inviteReceiver);
            }
            case "accept" -> acceptBrandInvite();
            case "kick" -> {
                if (args.length == 1) {
                    player.sendMessage(getMessage("must-specify-kicked-player"));
                    return true;
                }
                kickPlayerFromBrand(Bukkit.getPlayer(args[1]));
            }
            case "leave" -> leaveBrand();
            case "deny" -> denyRequest();
            case "slogan" -> {
                Brand brand = Util.getPlayerBrand(player);
                String slogan = extractMultiWordString(args);
                changeBrandSlogan(brand, slogan);
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
                sendBrandsList();
            }
            case "info" -> {
                Brand brand = getBrandFromName(extractMultiWordString(args));
                // /brand info. Returns the info for your brand
                if (args.length == 1) {
                    brand = getPlayerBrand(player);
                }
                if (brand == null) {
                    player.sendMessage(getMessage("brand-does-not-exist"));
                    return true;
                }
                sendBrandInfo(brand);
            }
            default -> sendHelpMessage();
        }

        return true;
    }

    private void sendBrandInfo(Brand brand) {

        if (brand == null) {
            player.sendMessage(getMessage("brand-does-not-exist"));
            return;
        }

        player.sendMessage(getMessage("info-header"));
        sendOwnerComponent(brand);
        sendBrandNameComponent(brand);
        sendSloganComponent(brand);
        sendMembersComponent(brand);
        player.sendMessage(getMessage("footer"));
    }
    private void sendOwnerComponent(Brand brand) {
        player.sendMessage(Component
                .text("Owner: ", NamedTextColor.RED, TextDecoration.BOLD)
                .append(Component.text(brand.getBrandOwner().getName(), NamedTextColor.GRAY)
                        .decoration(TextDecoration.BOLD, false))
                .append(Component.newline()));
    }
    private void sendSloganComponent(Brand brand) {
        player.sendMessage(Component
                .text("Slogan: ", NamedTextColor.RED, TextDecoration.BOLD)
                .append(Component.text('"' + brand.getBrandSlogan() + '"', NamedTextColor.GRAY, TextDecoration.UNDERLINED)
                        .decoration(TextDecoration.BOLD, false))
                .append(Component.newline()));
    }
    private void sendBrandNameComponent(Brand brand) {
        player.sendMessage(Component
                .text("Name: ", NamedTextColor.RED, TextDecoration.BOLD)
                .append(Component.text(brand.getBrandName(), NamedTextColor.GRAY)
                        .decoration(TextDecoration.BOLD, false))
                .append(Component.newline()));
    }
    private void sendMembersComponent(Brand brand) {
        Component membersPrefix = Component.text("Members: ", NamedTextColor.RED, TextDecoration.BOLD);
        ArrayList<String> memberNames = new ArrayList<>();
        for (String playerUUIDString : brand.getMembers()) {
            UUID uuid = UUID.fromString(playerUUIDString);
            memberNames.add(Bukkit.getOfflinePlayer(uuid).getName());
        }
        if (!brand.getMembers().isEmpty()) {
            String brandMembers = String.join("&7, &e", memberNames);
            Component membersList = LegacyComponentSerializer.legacyAmpersand()
                    .deserialize(brandMembers).decoration(TextDecoration.BOLD, false);
            player.sendMessage(membersPrefix
                    .append(membersList));
            return;
        }
        player.sendMessage(membersPrefix
                .append(Component.text("None", NamedTextColor.GRAY)
                .decoration(TextDecoration.BOLD, false)));
    }

    private void sendBrandsList() {
        ArrayList<Brand> brands = Util.getAllBrands();
        player.sendMessage(getMessage("list-header"));
        for (int i = 0; i < 10; i++) {
            int index = i + (9 * page);
            if (index >= brands.size()) {
                player.sendMessage(getPreviousPageComponent().append(getPageComponent()));
                player.sendMessage(getMessage("footer"));
                return;
            }
            Brand brand = brands.get(index);
            player.sendMessage(Component.text((index + 1) + ". ", NamedTextColor.RED)
                    .append(getBrandComponent(brand)));
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

        return miniMessage.deserialize("                        <click:run_command:'/brand list "
                + (page - 1) +"'><b><gray><hover:show_text:'Previous Page'>« </hover></gray><b></click>   ");
    }
    private Component getNextPageComponent() {
        return miniMessage.deserialize("   <click:run_command:'/brand list " + page +"'><b><gray><hover:show_text:'Next Page'>»</hover></gray><b></click>");
    }
    private Component getBrandComponent(Brand brand) {
        String brandName = brand.getBrandName();
        return miniMessage.deserialize("<hover:show_text:'Click for more info!'><gold>"+
                brandName +" - " + brand.getMembers().size() +
                "</gold></hover>").clickEvent(ClickEvent.runCommand("/brand info " + brandName));
    }

    private void sendHelpMessage() {
        List<String> helpMessage = unitedBrands.getConfig().getStringList("messages.help-command");
        for (String message : helpMessage) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
        }
    }

    private void createBrand(String[] args) {
        if (args.length == 1) {
            player.sendMessage(getMessage("must-specify-brand-name", ""));
            return;
        }

        String brandName = String.join(" ", extractMultiWordString(args));
        Brand brand = new Brand(unitedBrands, brandName, player, null);

        if (Util.hasBrand(player)) {
            player.sendMessage(getMessage("in-a-brand", Util.getPlayerBrand(player).getBrandName()));
            return;
        }

        if (Util.brandExists(brand)) {
            player.sendMessage(getMessage("brand-already-exists", brandName));
            return;
        }
        brand.createBrand();
    }

    private void deletePlayerBrand() {
        Brand brand = Util.getPlayerBrand(player);

        if (brand == null) {
            player.sendMessage(getMessage("must-own-brand"));
            return;
        }

        String brandName = brand.getBrandName();

        if (Util.hasBrand(player) && isBrandOwner(brand)) {
            brand.deleteBrand();
            player.sendMessage(getMessage("brand-deleted", brandName));
            return;
        }

        player.sendMessage(getMessage("brand-cannot-be-deleted", brandName));
    }

    private void invitePlayerToBrand(Player inviteReceiver) {

        if (inviteReceiver == null) {
            player.sendMessage(getMessage("invalid-player"));
            return;
        }

        Brand brand = Util.getPlayerBrand(player);

        if (brand == null) {
            player.sendMessage(getMessage("must-own-brand"));
            return;
        }

        String brandName = brand.getBrandName();

        if (player == inviteReceiver) {
            player.sendMessage(getMessage("cannot-invite-self", brandName));
            return;
        }

        if (isBrandOwner(brand)) {
            InviteRequest inviteRequest = new InviteRequest(player, inviteReceiver);
            inviteRequests.add(inviteRequest);
            player.sendMessage(getMessage("player-invited", brandName));
            inviteReceiver.sendMessage(getMessage("brand-invite", brandName));
            return;
        }

        player.sendMessage(getMessage("must-own-brand"));

    }

    private void acceptBrandInvite() {
        InviteRequest request = getRequest(player);

        if (request == null) {
            player.sendMessage(getMessage("no-requests"));
            return;
        }

        Player receiver = request.getReceiver();
        Player sender = request.getSender();

        if (Util.getPlayerBrand(receiver) != null) {
            player.sendMessage(getMessage("already-in-a-brand"));
            return;
        }

        Brand brand = Util.getPlayerBrand(sender);
        String brandName = brand.getBrandName();
        brand.addMember(player);

        sender.sendMessage(getMessage("brand-join-sender", player));
        receiver.sendMessage(getMessage("brand-join", brandName));

        inviteRequests.remove(request);
    }

    private void kickPlayerFromBrand(Player kickedPlayer) {

        if (kickedPlayer == null) {
            player.sendMessage(getMessage("must-specify-kicked-player", ""));
            return;
        }

        Brand brand = Util.getPlayerBrand(player);
        String brandName = brand.getBrandName();

        if (!isBrandOwner(brand)) {
            player.sendMessage(getMessage("must-own-brand", brandName));
            return;
        }

        if (kickedPlayer == player) {
            player.sendMessage(getMessage("cannot-kick-self", brandName));
            return;
        }

        brand.removeMember(kickedPlayer);
        kickedPlayer.sendMessage(getMessage("kicked-from-brand", brandName));
        player.sendMessage(getMessage("player-kicked", brandName));
    }

    private void leaveBrand() {
        Brand brand = Util.getPlayerBrand(player);
        if (brand == null) {
            player.sendMessage(getMessage("must-have-brand", ""));
            return;
        }

        String brandName = brand.getBrandName();

        if (isBrandOwner(brand)) {
            player.sendMessage(getMessage("must-delete-brand", brandName));
            return;
        }

        if (Util.hasBrand(player)) {
            OfflinePlayer brandOwner = brand.getBrandOwner();
            brand.removeMember(player);
            player.sendMessage(getMessage("brand-leave", brandName));
            if (brandOwner.isOnline()) {
                brandOwner.getPlayer().sendMessage(getMessage("player-left-brand", brandName, player));
            }
        }

    }

    private void denyRequest() {
        InviteRequest request = getRequest(player);
        String brandName = Util.getPlayerBrand(player).getBrandName();

        if (request != null) {
            request.getSender().sendMessage(getMessage("brand-deny-sender", player));
            request.getReceiver().sendMessage(getMessage("brand-deny", brandName));
            inviteRequests.remove(request);
            return;
        }
        player.sendMessage(getMessage("no-requests", ""));
    }

    private void changeBrandSlogan(Brand brand, String slogan) {
        if (brand == null) {
            player.sendMessage(getMessage("must-have-brand"));
            return;
        }

        if (!isBrandOwner(brand)) {
            player.sendMessage(getMessage("must-own-brand"));
            return;
        }

        if (slogan == null) {
            player.sendMessage(getMessage("must-specify-slogan"));
        }

        brand.setSlogan(slogan);
        player.sendMessage(getMessage("slogan-changed", brand.getBrandName()));
    }

    private boolean isBrandOwner(Brand brand) {
        if (Util.hasBrand(player)) {
            return brand.getBrandOwner().equals(player);
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
